package com.cogswell.datatracker.aggregator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricStatistics {

  private String market;
  private String instrument;
  private Long start;
  private Long end;
  private Statistics priceStats;
  private Statistics volumeStats;
  private Statistics volumeQuoteStats;
}
