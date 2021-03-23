package com.cogswell.datatracker.cryptowatch;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PriceSummary {

    private BigDecimal last;
    private BigDecimal high;
    private BigDecimal low;
    private PriceChange change;
}
