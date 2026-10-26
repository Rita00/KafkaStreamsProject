#!/bin/sh
# Builds the four programs and downloads what the original ran in Docker into vendor/:
# Kafka 2.8.1 (ZooKeeper, broker and Connect), Confluent's JDBC connector,
# WildFly 26.1 for the REST service, and a portable PostgreSQL.
# Needs Maven and JDK 17 (Debian/Ubuntu: apt install maven openjdk-17-jdk-headless).
set -e
cd "$(dirname "$0")"
. ./env.sh
M=https://repo.maven.apache.org/maven2
mkdir -p vendor
if [ ! -d vendor/kafka ]; then
  curl -fsSL https://archive.apache.org/dist/kafka/2.8.1/kafka_2.13-2.8.1.tgz -o vendor/kafka.tgz
  tar xzf vendor/kafka.tgz -C vendor && mv vendor/kafka_2.13-2.8.1 vendor/kafka && rm vendor/kafka.tgz
  # As in the original Dockerfile: the JDBC connector and the PostgreSQL driver go in Kafka's libs.
  curl -fsSL https://packages.confluent.io/maven/io/confluent/kafka-connect-jdbc/10.2.5/kafka-connect-jdbc-10.2.5.jar -o vendor/kafka/libs/kafka-connect-jdbc-10.2.5.jar
  curl -fsSL "$M/org/postgresql/postgresql/42.7.14/postgresql-42.7.14.jar" -o vendor/kafka/libs/postgresql-42.7.14.jar
fi
if [ ! -d vendor/pgsql ]; then
  curl -fsSL "$M/io/zonky/test/postgres/embedded-postgres-binaries-linux-amd64/16.9.0/embedded-postgres-binaries-linux-amd64-16.9.0.jar" -o vendor/pg.jar
  mkdir vendor/pgsql
  (cd vendor/pgsql && python3 -m zipfile -e ../pg.jar . && tar xJf ./*.txz && rm -rf ./*.txz META-INF)
  rm vendor/pg.jar
fi
if [ ! -d vendor/wildfly ]; then
  curl -fsSL "$M/org/wildfly/wildfly-dist/26.1.3.Final/wildfly-dist-26.1.3.Final.tar.gz" -o vendor/wf.tgz
  tar xzf vendor/wf.tgz -C vendor && mv vendor/wildfly-26.1.3.Final vendor/wildfly && rm vendor/wf.tgz
  echo "driver.jar=$(pwd)/vendor/kafka/libs/postgresql-42.7.14.jar" > vendor/cli.properties
  vendor/wildfly/bin/jboss-cli.sh --file=wildfly-setup.cli --properties=vendor/cli.properties
  rm vendor/cli.properties
fi
(cd Clients && mvn -B -q compile assembly:single)
(cd Streams && mvn -B -q package -DskipTests)
(cd REST && mvn -B -q package -DskipTests)
(cd adminCLI && mvn -B -q package -DskipTests)
