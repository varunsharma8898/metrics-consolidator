# Metrics consolidator service
An video sensor released by an OTT company writes bitrate and framerate values to 2 separate UDP socket ports.
This application consumes data from both these streams to show consolidated values which is printed to the log/console. More information in [video_streams.md]().

The application has been built using Vertx. So there are separate verticles (event loops) for server and clients - bitrate and framerate.
A cache is being used through a service which is deployed on the main verticle and communication with it happens over event bus.

Both the client processes emit one message every 2000ms.
For testing purposes, the starting timestamp has been kept static and incremented upon each message produced.

At the server side, the messages are kept in a cache with TTL. 
If a corresponding message is not received within expiry time, the entry is discarded. Ideally it should be retried for a few predetermined times and then discarded to a dead-letter queue.
This cache can be replaced with a distributed cache with TTL for scalability and allowing worker processes to be stateless.

## Compile, Test and Coverage report
```
# Compile and build
cd metrics-consolidator/
./gradlew shadowJar

# Run tests
./gradlew clean test

# coverage report will be present at:
metrics-consolidator/build/jacocoHtml/index.html

```
## Run
### To run both client and server verticles in the same process
```
java -jar build/libs/metrics-consolidator-1.0-SNAPSHOT-all.jar
or
java -jar build/libs/metrics-consolidator-1.0-SNAPSHOT-all.jar all
```

### To run separate processes for each - bitrate client, framerate client and udp server
```
java -jar build/libs/metrics-consolidator-1.0-SNAPSHOT-all.jar bitrate_client
java -jar build/libs/metrics-consolidator-1.0-SNAPSHOT-all.jar framerate_client
java -jar build/libs/metrics-consolidator-1.0-SNAPSHOT-all.jar server
```

### Run/debug through IntelliJ 
Run the MainVerticle::main() directly through IDE.