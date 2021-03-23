package com.cogswell.datatracker.cryptowatch;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for the Cryptowatch REST API.
 */
@Component
public class CryptowatchClient {

  private static final String baseUrl = "https://api.cryptowat.ch";

  private final RestTemplate restTemplate;

  public CryptowatchClient() {
    this(new RestTemplate());
  }

  public CryptowatchClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  /**
   * Get current 24-hour market summaries.
   */
  public MarketSummaries getMarketSummaries() {
    ResponseEntity<MarketSummaries> response =  restTemplate.getForEntity(
        baseUrl + "/markets/summaries",
        MarketSummaries.class);

    if (response.getStatusCodeValue() == 200) {
      return response.getBody();
    } else {
      throw new RuntimeException("Failed to get market summaries: " + response);
    }
  }
}
