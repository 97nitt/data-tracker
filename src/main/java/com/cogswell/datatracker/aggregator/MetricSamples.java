package com.cogswell.datatracker.aggregator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class MetricSamples {

  private String market;
  private String instrument;

  private final List<BigDecimal> price = new ArrayList<>();
  private final List<BigDecimal> volume = new ArrayList<>();
  private final List<BigDecimal> volumeQuote = new ArrayList<>();
}
