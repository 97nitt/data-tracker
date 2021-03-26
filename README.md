# Data Tracker

This application polls the [Cryptowatch REST API](https://docs.cryptowat.ch/rest-api/) for current 
cryptocurrency market data and calculates statistics (min, max, mean, standard deviation) at hourly 
intervals.

## Design

![Design](doc/data-tracker.png)

### Metric Sampler

The *Metric Sampler* queries exchange/instrument metrics from the Cryptowatch REST API at some 
configurable schedule (default is every minute). As currently written, each request to the API 
returns data for all available exchanges and instruments. A message is written to a `samples` Kafka
topic for each exchange/instrument combination in the API response. The key for each message is 
the exchange+instrument. This results in a consistent topic partition assignment for every sample 
received for each exchange/instrument. This becomes important when aggregating samples in the 
*Metric Aggregator*.

### Metric Aggregator

The *Metric Aggregator* is a [Kafka Streams](https://kafka.apache.org/documentation/streams) 
application that aggregates messages from the `samples` topic over 1-hour "tumbling" windows. The 
active window is continually updated as new samples come in, and the latest calculated statistics 
for each exchange/instrument are periodically flushed to the `statistics` Kafka topic (default is 
every 30s).

### Metric Writer (not implemented)

The *Metric Writer* has not yet been implemented. It would be a Kafka consumer application that 
would read metric statistics updates from the `statistics` topic and write them out to a data store 
suitable to the read requirements of the *Metric API* (maybe a relational database like MySQL or 
Postgres).

### Metric API (not implemented)

The *Metric API* has not yet been implemented. It would be either a REST or GraphQL API on top of 
the metric database that could be used by clients to query and/or aggregate metric statistics by 
various dimensions.

## Build Requirements

- [Java 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)

## Run It

Use [Docker Compose](https://docs.docker.com/compose) to stand up a local Kafka cluster and create 
required Kafka topics:

    $ docker-compose up -d

Build the application (and run tests):

    $ ./gradlew clean build

Start the application:

    $ java -jar build/libs/data-tracker-[version].jar

[Confluent Control Center](https://docs.confluent.io/platform/current/control-center/index.html) 
has been included in the Docker Compose environment. In the absence of the *Metric Writer* and 
*Metric API*, Control Center can be used to verify the messages being generated in Kafka.

Go to [http://localhost:9021](http://localhost:9021), then navigate to:

- **Topics** > **samples** > **Messages** to view metric samples
- **Topics** > **statistics** > **Messages** to view calculated statistics

## TODO

This application is incomplete and there are plenty of opportunities to improve it.

### Microservices

Break apart each of the major components into separate microservices that can be updated, deployed, 
and scaled independently:

- Metric Sampler
- Metric Aggregator
- Metric Writer
- Metric API

The *Metric Sampler* could be rewritten in either Go or Python, leveraging one of the official
[Cryptowatch client SDKs](https://docs.cryptowat.ch/rest-api/#api-clients) to minimize the amount of 
code required.

The *Metric Aggregator* could be lifted almost as-is from this repository.

The *Metric Writer* could be written in any language that has client support for Kafka and whatever
data store is chosen.

The *Metric API* could be written in any language that has client support for whatever data store is 
chosen.

### Testing

More unit tests should be added to improve test coverage. At a minimum, cover "happy path" scenarios 
through all software components with non-trivial logic, as well as a reasonable number of likely 
error conditions and edge cases. "Reasonable" tends to be in the eye of the beholder.

Also consider adding integration tests using something like [Testcontainers](https://www.testcontainers.org) 
to stand up external dependencies (like Kafka). These tests can be relatively simple in scope, but 
provide confidence that the application will function as expected when integrated in an environment 
that attempts to simulate production.

### Production Readiness

The following features could be added to make this application production-ready:

- integration with a Logging service (like CloudWatch or Loggly)
- integration with a Monitoring service (like CloudWatch or Datadog)
- integration with an external Configuration service (like Parameter Store or Consul)
- use a more compact Kafka message format instead of JSON (like Avro)
- if memory pressure from the Kafka Streams state stores becomes an issue, consider using disk 
  storage instead of memory

### Scaling

To increase the frequency of the *Metric Sampler*, you can simply update the cron expression to poll 
more frequently. If the Cryptowatch API latency becomes too high relative to the poll frequency, you 
could try to partition the data coming from that API by exchange and/or instrument. The 
*Metric Sampler* could be updated to poll each data partition in a separate thread. One concern with 
this approach is the rate limiting implemented by the Cryptowatch API, you would need to keep an eye 
on the usage of [Cryptowatch credits](https://docs.cryptowat.ch/rest-api/rate-limit), possibly 
implementing a backoff as 24-hour credit limits are approached.

As currently implemented, all metric samples are written to a single Kafka topic, which can have any 
number of partitions (limited to some extent by the number brokers in the Kafka cluster). To 
maximize throughput for the calculation and persistence of statistics, the *Metric Aggregator* and 
*Metric Writer* can be scaled out to the number of topic partitions (1 Kafka consumer per partition).

The *Metric API* should be a stateless application that can be scaled out horizontally as needed to 
meet increases in requests. Horizontal scaling should also be considered for the underlying database 
technology so that does not become a bottleneck.

### Feature Request: Active Monitoring

If we wanted to implement active monitoring for significant changes in metric values, we could 
implement another stateful Kafka Streams application that consumes both the `statistics` topic to 
maintain the average of each metric for the previous hour, and the `samples` topic to evaluate 
samples as they come in against those averages. If any incoming sample exceeds 3x the average over
the previous hour, a message would be published to a new `notifications` topic.

We would then have a consumer of the `notifications` topic responsible for sending notifications 
via email, SMS, or some other messaging service. That consumer could also aggregate the notification
messages to avoid "spamming" recipients (e.g. instead of sending a notification for each message
received, send a single notification for all messages received in the last *N* minutes).
