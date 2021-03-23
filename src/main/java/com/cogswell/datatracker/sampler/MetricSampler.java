package com.cogswell.datatracker.sampler;

import com.cogswell.datatracker.cryptowatch.AssetSummary;
import com.cogswell.datatracker.cryptowatch.CryptowatchClient;
import com.cogswell.datatracker.cryptowatch.MarketSummaries;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * This service periodically samples Cryptocurrency market metrics via the Cryptowatch REST API.
 */
@Service
public class MetricSampler {

  private static final Logger logger = LoggerFactory.getLogger(MetricSampler.class);

  private final CryptowatchClient cryptowatch;

  public MetricSampler(CryptowatchClient cryptowatch) {
    this.cryptowatch = cryptowatch;
  }

  @Scheduled(fixedDelayString = "${sampling.frequency}")
  public void sample() {
    logger.debug("Beginning metric sampling");

    MarketSummaries summaries = cryptowatch.getMarketSummaries();
    logger.debug("Retrieved {} market summaries", summaries.getResult().size());

    publishSamples(summaries);

    logger.debug("Completed metric sampling");
  }

  private void publishSamples(MarketSummaries summaries) {
    summaries.getResult()
        .entrySet()
        .stream()
        .map(this::mapToSample)
        .forEach(sample -> {
          // TODO: publish samples to Kafka
          logger.debug("Generated sample: {}", sample);
    });
  }

  private Sample mapToSample(Map.Entry<String, AssetSummary> result) {
    String[] marketAndInstrument = result.getKey().split(":");
    AssetSummary summary = result.getValue();

    Sample sample = new Sample();
    sample.setMarket(marketAndInstrument[0]);
    sample.setInstrument(marketAndInstrument[1]);
    sample.setPrice(summary.getPrice().getLast());
    sample.setVolume(summary.getVolume());
    sample.setVolumeQuote(summary.getVolumeQuote());

    return sample;
  }
}
