#!/bin/bash
# The admin CLI, talking to the REST service.
cd "$(dirname "$0")"
. ./env.sh
curl -s -o /dev/null "$REST_URL/listManagers" || { echo "The REST service isn't running. Start the pipeline first."; exit 1; }
exec java -jar adminCLI/target/restclient.jar
