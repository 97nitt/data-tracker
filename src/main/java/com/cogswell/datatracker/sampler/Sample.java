package com.cogswell.datatracker.sampler;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class Sample {

  private String market;
  private String instrument;
  private BigDecimal price;
  private BigDecimal volume;
  private BigDecimal volumeQuote;
}
