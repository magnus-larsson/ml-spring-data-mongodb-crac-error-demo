This project can be used to reproduce problems with `spring-data-mongodb` when MongoDB is accessed during warmup before creating a CRaC checkpoint.
The checkpoint command will be aborted due to an open socket, used for the connection to MongoDB, with an error message like:

```
An exception during a checkpoint operation:
jdk.internal.crac.mirror.CheckpointException
        Suppressed: jdk.internal.crac.mirror.impl.CheckpointOpenSocketException: Socket[addr=localhost/127.0.0.1,port=27017,localport=54228]
```

This problem can be mitigated by closing the Mongo Client before the checkpoint using CRaC's callback method `beforeCheckpoint`.
To restart the Mongo Client after a restore from a CRaC checkpoint, `spring-cloud-refresh` can be used.

Unfortunately, new configuration parameters for the MongoDB connection are not picked up when restarted from a CRaC checkpoint.

The project consists of a Spring Boot app that provides a HTTP endpoint for getting data from a MongoDB database.
The project is configured to use an external configuration file, `runtime-configuration.yml`. It can be used to override the default configuration values in the file `src/main/resources/application.yml`. The project provides external configuration files, one empty that is used during development, and one that points to another MongoDB database to be used when run in production. Its configuration looks like:

	spring.data.mongodb:
	host: mongodb
	port: 27017
	database: prod-db

To reproduce the problems and its mitigations in a Linux environment, clone the project from GitHub and run the test script `tests.bash` to create a checkpoint. The test script calls the HTTP endpoint a couple of time during the warmup, i.e., before the `jcmd myapp.jar JDK.checkpoint` command is executed.

First ensure that a Java 21 JDK with CRaC support is used, e.g. by running a command like:

```
sdk use java 21.0.2.crac-zulu
```

Commands to get the source code and create a checkpoint:

```
git clone https://github.com/magnus-larsson/ml-spring-data-mongodb-crac-error-demo.git
cd ml-spring-data-mongodb-crac-error-demo

cp runtime-configuration-dev.yml runtime-configuration.yml
./tests.bash
```

> **Note**: The external configuration files for development, `runtime-configuration-dev.yml`, is used when the checkpoint is created.

The tests-script shall end with the log message:

	CR: Checkpoint ...

...and the folder `checkpoint` has been created.


The Spring Boot app can now be restareted from the checkpoint with the commands:

```
cp runtime-configuration-prod.yml runtime-configuration.yml
java -XX:CRaCRestoreFrom=checkpoint
```

> **Note**: The external configuration files for production, `runtime-configuration-prod.yml`, is used when the app is restarted from the checkpoint.

Note from the log output that `spring-cloud-refresh` detects the updated configuration of the MongoDB database (i.e. the properties `spring.data.mongodb.database` and `spring.data.mongodb.host`):

	2024-07-23T11:49:10.555+02:00  INFO 674800 --- [data-mongodb-test] [Attach Listener] o.s.c.c.refresh.RefreshScopeLifecycle    : Refreshing context on restart.
	2024-07-23T11:49:10.835+02:00  INFO 674800 --- [data-mongodb-test] [Attach Listener] o.s.c.c.refresh.RefreshScopeLifecycle    : Refreshed keys: [spring.data.mongodb.database, spring.data.mongodb.host]

In a separate terminal, call the app's health endpoint and get some data from the MongoDB database to verify the app:

```
curl localhost:8080/actuator/health
curl localhost:8080/getAuthor/id-1
curl localhost:8080/getAuthor/id-2
curl localhost:8080/getAuthor/id-3
```

Note how the app connects to the MongoDB database, but uses the default connection string `localhost:27017`, ignoring the new hostname `mongodb`, specified in the external configuration file

	2024-07-23T11:51:58.169+02:00  INFO 674800 --- [data-mongodb-test] [localhost:27017] org.mongodb.driver.cluster               : Monitor thread successfully connected to server with description ServerDescription{address=localhost:27017, type=STANDALONE, state=CONNECTED, ok=true, minWireVersion=0, maxWireVersion=17, maxDocumentSize=16777216, logicalSessionTimeoutMinutes=30, roundTripTimeNanos=2169377}


So, the mongo client is successfully restarted after the restart from a checkpoint, but the new configuration s ignored.

Wrap up stopping the Spring Boot app with the command:

```
kill $(jcmd | grep build/libs/demo-0.0.1-SNAPSHOT.jar | awk '{print $1}')
```

> **Note**: When the app is restarted from a checkpoint it does not react on `CTRL/C`, so it has to be stopped using the `kill` command.