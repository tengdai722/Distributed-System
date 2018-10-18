# Distributed Grep Query

This project is to to query distributed log files on multiple machines, from any one of those machines.

## Develop environment

- IntelliJ IDEA
- Windows 10 and Mac OS
- OpenJDK 1.8.181 x64
- Maven 3 with wrapper

## Build and Unit test

Build system is **Maven 3**, and unit test relies on **JUnit5** together with **maven-surefire-plugin**.

And you need to equip and configure environment:

- JDK version 1.8 or above

### To build a runnable JAR

```bash
./mvnw package
```

During that process, Maven will run through all unit tests.

### How to run

1. Open a terminal

2. Build a runnable JAR (See before)

3. Deploy built JAR to all EE-VMs, using the script (You need a private key): `scripts/distribute_jar_to_all.sh`

#### For server

Start server on all VM with the script (which will first kill all server): `scripts/start_all_server.sh`

#### For parallel client

Use mode **p**, and use higher thread of clients can speed up the query. You also need the `server_list.txt` file to define the hostname of the servers and their corresponding log file.

Input grep query as below:

```bash
java -jar CS425-1.0-jar-with-dependencies.jar p 10 ".com"
```

That means run 10 threads with a grep pattern **.com**.

## Scripts

- To deploy built JAR to all EE-VMs, run: `scripts/distribute_jar_to_all.sh`
- To kill all running server/client, run: `scripts/kill_all_server.sh`
- To start server on all VM (which will first kill all server), run `scripts/start_all_server.sh`
