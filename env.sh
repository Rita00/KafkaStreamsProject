# Shared settings for the build and start scripts.
# WildFly 26 and Kafka 2.8 run on Java 11 or 17; pick a JDK 17 if the default java is newer.
if [ -z "$KAFKA_DEMO_JAVA_HOME" ]; then
  for j in /usr/lib/jvm/java-17-openjdk-amd64 /usr/lib/jvm/java-17-openjdk-arm64 /usr/lib/jvm/java-17-openjdk; do
    [ -x "$j/bin/java" ] && KAFKA_DEMO_JAVA_HOME=$j && break
  done
  [ -z "$KAFKA_DEMO_JAVA_HOME" ] && [ -x /usr/libexec/java_home ] && KAFKA_DEMO_JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || true)
fi
[ -n "$KAFKA_DEMO_JAVA_HOME" ] || { echo "JDK 17 is missing: apt install openjdk-17-jdk-headless"; exit 1; }
export JAVA_HOME="$KAFKA_DEMO_JAVA_HOME"
export PATH="$JAVA_HOME/bin:$PATH"
case "$LC_ALL$LC_CTYPE$LANG" in *UTF-8*|*utf8*) ;; *) export LC_ALL=C.UTF-8 ;; esac
# Kafka keeps the original ports (the programs have localhost:9092 built in).
PG_PORT=5435          # PostgreSQL, database "school"
PORT_OFFSET=13        # WildFly on 8093 (http) and 10003 (management)
CONNECT_PORT=8094     # Kafka Connect REST interface
export REST_URL="http://localhost:$((8080 + PORT_OFFSET))/restws/rest/RestOperations"
