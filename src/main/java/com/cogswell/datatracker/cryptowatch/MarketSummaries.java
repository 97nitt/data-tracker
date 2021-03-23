package com.cogswell.datatracker.cryptowatch;

import java.util.Map;
import lombok.Data;

@Data
public class MarketSummaries {

  private Map<String, AssetSummary> result;
}
