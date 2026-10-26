# Kafka Streams project

Credits and payments for a bank's clients flow through Kafka:

- **REST** (WildFly + JPA): the managers, clients and currencies in PostgreSQL, plus the results.
- **adminCLI**: a menu that adds managers, clients and currencies and shows the results through REST.
- **Kafka Connect** with the JDBC connector:
  - the source side publishes the clients and currencies tables;
  - the sink side writes the results back to the database.
- **Clients**: produces a random credit and payment every 5 seconds.
- **Streams**: computes, per client, credits, payments, balances and the monthly bill; totals; clients with no payments in two months; the client with the highest debt; and the manager with the most revenue.

`docker-dir/` has the original Docker Compose setup.

## Running it without Docker

The scripts in the repository root run the same pieces directly on Linux:

```sh
sudo apt install maven openjdk-17-jdk-headless   # Debian/Ubuntu
./build.sh            # builds the four programs; downloads Kafka 2.8.1, the JDBC connector,
                      # WildFly 26.1 and a portable PostgreSQL into vendor/
./start-pipeline.sh   # PostgreSQL, ZooKeeper, Kafka, WildFly + REST, demo data, Connect, Streams
./run-clients.sh      # in a second terminal
./run-admin.sh        # in a third terminal
```

`start-pipeline.sh` runs in the foreground and stops everything on Ctrl-C. Each start uses a fresh
database and adds two managers, six clients and three currencies through the REST service
(`seed.sh`). It then prints the totals from the database every 10 seconds. Results refresh about
every 30 seconds, Kafka Streams' commit interval.

The ports are set in `env.sh`:

| Service | Port |
| --- | --- |
| Kafka | 9092 (built into the programs) |
| ZooKeeper | 2181 |
| PostgreSQL | 5435 |
| WildFly | 8093 (management 10003) |
| Kafka Connect | 8094 |

WildFly 26 and Kafka 2.8 need Java 11 or 17, so `env.sh` picks a JDK 17.

Changes needed to run outside Docker, marked "archive revival":

- The admin CLI builds every URL from one base address, read from `REST_URL`. It defaults to the original `host.docker.internal:8080`.
- The connector files still say `database:5432`. `start-pipeline.sh` writes copies pointed at the local database, with the source connectors republishing every 15 seconds instead of 60, so the Clients program sees new clients and currencies sooner.
- Kafka Streams stops if its input topics don't exist yet, so `start-pipeline.sh` creates them before starting it.

With the Clients program stopped and the last results written, the totals in the database match the
totals the Clients program prints.
