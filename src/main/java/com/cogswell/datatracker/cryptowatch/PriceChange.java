package com.cogswell.datatracker.cryptowatch;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PriceChange {

  private BigDecimal percentage;
  private BigDecimal absolute;
}
