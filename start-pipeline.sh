#!/bin/bash
# Starts the whole pipeline in the foreground, the way the original ran it in Docker:
# PostgreSQL ("school"), ZooKeeper, Kafka, WildFly with the REST service, the demo
# managers, clients and currencies (added through the REST service), Kafka Connect
# with the JDBC source and sink connectors, and the Streams application.
# Everything stops when this script stops (Ctrl-C).
cd "$(dirname "$0")"
. ./env.sh
for f in vendor/kafka vendor/wildfly vendor/pgsql Streams/target/Streams-1-jar-with-dependencies.jar REST/target/restws.war; do
  [ -e "$f" ] || { echo "Not built yet. Run ./build.sh first."; exit 1; }
done
up() { (echo > /dev/tcp/127.0.0.1/$1) 2>/dev/null; }
wait_port() { local i=0; until up $1; do sleep 0.2; i=$((i+1)); [ $i -ge 300 ] && { echo "  $2 didn't start. See demos/kafka-streams/run/"; exit 1; }; done; }
step() { printf '%-52s' "$1"; }

./stop-pipeline.sh
for p in $PG_PORT 2181 9092 $((8080 + PORT_OFFSET)) $((9990 + PORT_OFFSET)) $CONNECT_PORT; do
  up $p && { echo "Port $p is in use by another program. Stop it and try again."; exit 1; }
done
rm -rf run && mkdir -p run/logs
R=$(pwd)/run
pids=()
cleanup() { trap - EXIT INT TERM; echo; echo "Stopping the pipeline..."; ./stop-pipeline.sh; exit 0; }
trap cleanup EXIT INT TERM

step "PostgreSQL (database school, port $PG_PORT)"
PG=vendor/pgsql/bin
$PG/initdb -D run/pg -U postgres --auth=trust -E UTF8 --locale=C > run/logs/pg-init.log 2>&1
echo "CREATE DATABASE school" | $PG/postgres --single -D run/pg postgres >> run/logs/pg-init.log 2>&1
$PG/postgres -D run/pg -p $PG_PORT -k "$R" -c listen_addresses=127.0.0.1 > run/logs/postgres.log 2>&1 &
pids+=($!); echo $! > run/postgres.pid
wait_port $PG_PORT PostgreSQL; echo "ok"

export LOG_DIR=$R/logs KAFKA_HEAP_OPTS="-Xmx512m -Xms128m"
step "ZooKeeper (port 2181)"
printf 'dataDir=%s/zookeeper\nclientPort=2181\nclientPortAddress=127.0.0.1\nadmin.enableServer=false\nmaxClientCnxns=0\n' "$R" > run/zookeeper.properties
vendor/kafka/bin/zookeeper-server-start.sh run/zookeeper.properties > run/logs/zookeeper.out 2>&1 &
pids+=($!); echo $! > run/zookeeper.pid
wait_port 2181 ZooKeeper; echo "ok"

step "Kafka broker (port 9092)"
sed -e "s#^log.dirs=.*#log.dirs=$R/kafka-logs#" -e "s#^zookeeper.connect=.*#zookeeper.connect=127.0.0.1:2181#" \
    vendor/kafka/config/server.properties > run/server.properties
printf 'listeners=PLAINTEXT://127.0.0.1:9092\nauto.create.topics.enable=true\n' >> run/server.properties
vendor/kafka/bin/kafka-server-start.sh run/server.properties > run/logs/kafka.out 2>&1 &
pids+=($!); echo $! > run/kafka.pid
wait_port 9092 Kafka; echo "ok"

step "WildFly with the REST service (port $((8080 + PORT_OFFSET)))"
W=vendor/wildfly/standalone
rm -rf $W/deployments/restws.war* $W/data $W/tmp $W/log
cp REST/target/restws.war $W/deployments/
vendor/wildfly/bin/standalone.sh -Djboss.socket.binding.port-offset=$PORT_OFFSET > run/logs/wildfly.out 2>&1 &
pids+=($!); echo $! > run/wildfly.pid
i=0; until [ -f $W/deployments/restws.war.deployed ] || [ -f $W/deployments/restws.war.failed ] || [ $i -ge 900 ]; do sleep 0.1; i=$((i+1)); done
[ -f $W/deployments/restws.war.deployed ] || { echo "the REST service failed to deploy. See demos/kafka-streams/run/logs/wildfly.out"; exit 1; }
echo "ok"

step "Demo managers, clients and currencies"
./seed.sh > run/logs/seed.log 2>&1 || { echo "failed. See demos/kafka-streams/run/logs/seed.log"; exit 1; }
echo "ok ($(grep -c 'added successfully' run/logs/seed.log) added through REST)"

step "Kafka Connect, JDBC source and sinks (port $CONNECT_PORT)"
sed -e "s#^offset.storage.file.filename=.*#offset.storage.file.filename=$R/connect.offsets#" vendor/kafka/config/connect-standalone.properties > run/connect.properties
printf 'listeners=http://127.0.0.1:%s\n' $CONNECT_PORT >> run/connect.properties
# The same connectors as Commands/updatedCommands.txt, pointed at the local database
# instead of the Docker Compose host "database".
C=run/connectors; mkdir -p $C
# The source connectors republish the tables every 15 s rather than 60 s, so the Clients
# program (which reads from the latest offset) sees new clients and currencies sooner.
for f in Connectors/*.properties; do
  sed -e "s#//database:5432/#//127.0.0.1:$PG_PORT/#" -e "s#^poll.interval.ms=60000#poll.interval.ms=15000#" "$f" > $C/$(basename "$f")
done
vendor/kafka/bin/connect-standalone.sh run/connect.properties $C/get-clients.properties $C/sink-paymentsPerClients.properties \
  $C/get-currencies.properties $C/sink-creditsPerClients.properties $C/sink-balancePerClients.properties \
  $C/sink-windowedCreditPerClient.properties $C/sink-totalResults.properties $C/sink-mostDebt.properties \
  $C/sink-bestRevenue.properties $C/sink-noPayments.properties > run/logs/connect.out 2>&1 &
pids+=($!); echo $! > run/connect.pid
wait_port $CONNECT_PORT "Kafka Connect"; echo "ok"

step "Streams application"
# Kafka Streams stops if its input topics don't exist yet, so create them first
# (originally they appeared once the Clients program had produced something).
for t in Credits Payments DBInfoTopics allActions; do
  vendor/kafka/bin/kafka-topics.sh --bootstrap-server 127.0.0.1:9092 --create --if-not-exists --topic $t --partitions 1 --replication-factor 1 >> run/logs/topics.log 2>&1
done
rm -rf /tmp/kafka-streams/exercises-application   # its local state belongs to the previous run's broker
java -jar Streams/target/Streams-1-jar-with-dependencies.jar > run/logs/streams.out 2>&1 &
pids+=($!); echo $! > run/streams.pid
sleep 2; kill -0 $! 2>/dev/null || { echo "it stopped. See demos/kafka-streams/run/logs/streams.out"; exit 1; }
echo "ok"

echo
echo "The pipeline is running. Start the Clients pane to produce credits and payments,"
echo "then use the admin CLI. Totals from the database, as the REST service reports them:"
echo
while true; do
  for s in "${pids[@]}"; do kill -0 $s 2>/dev/null || { echo "A component stopped unexpectedly. See demos/kafka-streams/run/logs/"; exit 1; }; done
  c=$(curl -s "$REST_URL/listTotalCredits"); p=$(curl -s "$REST_URL/listTotalPayments"); b=$(curl -s "$REST_URL/listTotalBalances")
  printf '%s  credits %8.2f €  payments %8.2f €  balance %8.2f €\n' "$(date +%T)" "${c:-0}" "${p:-0}" "${b:-0}"
  sleep 10
done
