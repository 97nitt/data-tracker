package com.cogswell.datatracker.sampler;

import com.cogswell.datatracker.cryptowatch.AssetSummary;
import com.cogswell.datatracker.cryptowatch.CryptowatchClient;
import com.cogswell.datatracker.cryptowatch.MarketSummaries;
import com.cogswell.datatracker.kafka.ProducerProperties;
import java.util.Map;
import javax.annotation.PreDestroy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * This service periodically samples Cryptocurrency market metrics via the Cryptowatch REST API.
 */
@Service
public class MetricSampler {

  private static final Logger logger = LoggerFactory.getLogger(MetricSampler.class);

  private final CryptowatchClient cryptowatch;
  private final Producer<String, Sample> producer;
  private final String topic;

  /**
   * Auto-wired constructor will have dependencies injected via Spring-managed DI.
   *
   * @param cryptowatch Cryptowatch REST client
   * @param producerConfig Kafka producer configuration
   */
  @Autowired
  public MetricSampler(
      CryptowatchClient cryptowatch,
      ProducerProperties producerConfig) {

    this(cryptowatch, new KafkaProducer<>(producerConfig.getProperties()), producerConfig.getTopic());
  }

  /**
   * Package-private constructor, for unit testing purposes.
   *
   * @param cryptowatch Cryptowatch REST client
   * @param producer Kafka producer
   * @param topic Kafka producer topic
   */
  MetricSampler(
      CryptowatchClient cryptowatch,
      Producer<String, Sample> producer,
      String topic) {

    this.cryptowatch = cryptowatch;
    this.producer = producer;
    this.topic = topic;
  }

  /**
   * Poll for metric samples and write them to Kafka.
   */
  @Scheduled(fixedDelayString = "${sampling.frequency}")
  public void sample() {
    logger.debug("Beginning metric sampling");

    MarketSummaries summaries = cryptowatch.getMarketSummaries();
    logger.debug("Retrieved {} market summaries", summaries.getResult().size());

    publishSamples(summaries);

    logger.debug("Completed metric sampling");
  }

  @PreDestroy
  public void close() {
    if (producer != null) {
      // close Kafka producer to ensure any queued messages are flushed prior to shutdown
      logger.info("Closing Kafka producer");
      producer.close();
    }
  }

  private void publishSamples(MarketSummaries summaries) {
    summaries.getResult()
        .entrySet()
        .stream()
        .map(this::mapToProducerRecord)
        .forEach(this::publish);
  }

  private ProducerRecord<String, Sample> mapToProducerRecord(Map.Entry<String, AssetSummary> result) {
    String[] marketAndInstrument = result.getKey().split(":");
    AssetSummary summary = result.getValue();

    Sample sample = new Sample();
    sample.setMarket(marketAndInstrument[0]);
    sample.setInstrument(marketAndInstrument[1]);
    sample.setPrice(summary.getPrice().getLast());
    sample.setVolume(summary.getVolume());
    sample.setVolumeQuote(summary.getVolumeQuote());

    return new ProducerRecord<>(topic, sample.getInstrument(), sample);
  }

  private void publish(ProducerRecord<String, Sample> record) {
    producer.send(record, (meta, ex) -> {
      if (ex == null) {
        logger.debug("Successfully sent message: topic={}, partition={}, offset={}, key={}, value={}",
            meta.topic(),
            meta.partition(),
            meta.offset(),
            record.key(),
            record.value());

      } else {
        logger.error("Failed to send message: topic={}, partition={}, key={}, value={}",
            meta.topic(),
            meta.partition(),
            record.key(),
            record.value(),
            ex);
      }
    });
  }
}
