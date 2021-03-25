package com.cogswell.datatracker.cryptowatch;

import lombok.Data;

/**
 * Base class defining common fields in all Cryptowatch REST API responses.
 */
@Data
public abstract class Response {

  private Allowance allowance;
}
