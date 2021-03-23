package com.cogswell.datatracker.cryptowatch;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class AssetSummary {

  private PriceSummary price;
  private BigDecimal volume;
  private BigDecimal volumeQuote;
}
