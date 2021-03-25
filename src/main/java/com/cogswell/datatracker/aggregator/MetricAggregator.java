package com.cogswell.datatracker.aggregator;

import com.cogswell.datatracker.kafka.JsonSerde;
import com.cogswell.datatracker.kafka.StreamsProperties;
import com.cogswell.datatracker.sampler.Sample;
import java.time.Duration;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.WindowBytesStoreSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service wraps a Kafka Streams topology that calculates averages and standard deviations for
 * incoming metric samples over 1-hour tumbling windows.
 */
@Service
public class MetricAggregator {

  private static final Logger logger = LoggerFactory.getLogger(MetricAggregator.class);

  private final KafkaStreams streams;

  /**
   * Auto-wired constructor will have dependencies injected via Spring-managed DI.
   *
   * @param config Kafka Streams configuration
   */
  @Autowired
  public MetricAggregator(StreamsProperties config) {
    this(new KafkaStreams(buildTopology(config), config.getProperties()));
  }

  /**
   * Package-private constructor, for unit testing purposes.
   *
   * @param streams Kafka Streams client
   */
  MetricAggregator(KafkaStreams streams) {
    this.streams = streams;
  }

  /**
   * Build Kafka Streams topology
   *
   * @param config Kafka Streams configuration
   * @return topology
   */
  static Topology buildTopology(StreamsProperties config) {
    Serde<String> keySerde = Serdes.String();
    WindowBytesStoreSupplier storeSupplier = Stores.inMemoryWindowStore(
        "SampleStore",
        Duration.ofHours(1),
        Duration.ofHours(1),
        false);


    // build input stream of metric samples
    StreamsBuilder builder = new StreamsBuilder();
    builder.<String, Sample>stream(config.getInput(), Consumed.with(keySerde, JsonSerde.get(Sample.class, false)))
        // group by message key (instrument)
        .groupByKey()
        // 1-hour tumbling windows
        .windowedBy(TimeWindows.of(Duration.ofHours(1)))
        // aggregate samples in a windowed state store
        .aggregate(
            MetricSamples::new,
            new SampleAggregator(),
            Materialized.<String, MetricSamples>as(storeSupplier)
                .withKeySerde(keySerde)
                .withValueSerde(JsonSerde.get(MetricSamples.class, false)))
        .toStream()
        // calculate statistics on current samples
        .map(MetricAggregator::calculateStatistics)
        // write statistics to output topic
        .to(config.getOutput(), Produced.with(keySerde, JsonSerde.get(MetricStatistics.class, false)));

    return builder.build();
  }

  /**
   * Calculate statistics from collected metric samples.
   *
   * @param windowedKey time-windowed message key
   * @param samples metric samples
   * @return statistics
   */
  static KeyValue<String, MetricStatistics> calculateStatistics(Windowed<String> windowedKey, MetricSamples samples) {
    String key = windowedKey.key();
    MetricStatistics value = new MetricStatistics(
        samples.getMarket(),
        samples.getInstrument(),
        windowedKey.window().start(),
        windowedKey.window().end(),
        Statistics.from(samples.getPrice()),
        Statistics.from(samples.getVolume()),
        Statistics.from(samples.getVolumeQuote()));

    return new KeyValue<>(key, value);
  }

  @PostConstruct
  public void start() {
    logger.info("Starting Kafka Streams client");
    streams.start();
  }

  @PreDestroy
  public void close() {
    logger.info("Closing Kafka Streams client");
    streams.close();
  }
}
