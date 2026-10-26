#!/bin/bash
# Adds the demo managers, clients and currencies through the REST service,
# the same calls the admin CLI makes (options 1, 2 and 3).
cd "$(dirname "$0")"
. ./env.sh
post() { curl -sf -H 'Content-Type: application/json' -X POST --data "$2" "$REST_URL/$1"; echo; }
post addManager "Ana Ribeiro"
post addManager "Bruno Teixeira"
id() { curl -sf "$REST_URL/listManagers" | python3 -c "import json,sys; m=json.load(sys.stdin); print(next(k for k,v in m.items() if v==sys.argv[1]))" "$1"; }
ana=$(id "Ana Ribeiro"); bruno=$(id "Bruno Teixeira")
for c in "Carla Mendes:$ana" "Diogo Alves:$ana" "Eva Moreira:$ana" "Filipe Rocha:$bruno" "Gabriela Nunes:$bruno" "Hugo Martins:$bruno"; do
  post addClients "{\"name\":\"${c%%:*}\",\"managerID\":${c##*:}}"
done
post addCurrency '{"name":"dollar","exchangeRate":0.92}'
post addCurrency '{"name":"pound","exchangeRate":1.17}'
post addCurrency '{"name":"real","exchangeRate":0.17}'
