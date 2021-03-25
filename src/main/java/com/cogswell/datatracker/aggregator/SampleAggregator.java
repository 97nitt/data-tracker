package com.cogswell.datatracker.aggregator;

import com.cogswell.datatracker.sampler.Sample;
import org.apache.kafka.streams.kstream.Aggregator;

/**
 * Kafka Streams aggregator that adds incoming {@link Sample}s to {@link MetricSamples} in a
 * windowed state store.
 */
public class SampleAggregator implements Aggregator<String, Sample, MetricSamples> {

  @Override
  public MetricSamples apply(String key, Sample value, MetricSamples aggregate) {
    aggregate.setMarket(value.getMarket());
    aggregate.setInstrument(value.getInstrument());
    aggregate.getPrice().add(value.getPrice());
    aggregate.getVolume().add(value.getVolume());
    aggregate.getVolumeQuote().add(value.getVolumeQuote());
    return aggregate;
  }
}
