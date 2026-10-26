#!/bin/bash
# The Clients program: reads clients and currencies from Kafka and produces a
# random credit and payment every 5 seconds.
cd "$(dirname "$0")"
. ./env.sh
(echo > /dev/tcp/127.0.0.1/9092) 2>/dev/null || { echo "Kafka isn't running. Start the pipeline first."; exit 1; }
exec java -jar Clients/target/Clients-jar-with-dependencies.jar
