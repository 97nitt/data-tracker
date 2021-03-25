package com.cogswell.datatracker.cryptowatch;

import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MarketSummaries extends Response {

  private Map<String, AssetSummary> result;
}
