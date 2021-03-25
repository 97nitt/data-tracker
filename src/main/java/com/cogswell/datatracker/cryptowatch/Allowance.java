package com.cogswell.datatracker.cryptowatch;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class Allowance {

  private BigDecimal cost;
  private BigDecimal remaining;
}
