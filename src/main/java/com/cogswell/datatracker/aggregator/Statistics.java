package com.cogswell.datatracker.aggregator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Statistics {

  private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;

  private Integer count;
  private BigDecimal min;
  private BigDecimal max;
  private BigDecimal mean;
  private BigDecimal stddev;

  /**
   * Calculate statistics from given list of numbers.
   *
   * @param values input values
   * @return statistics
   */
  public static Statistics from(List<BigDecimal> values) {
    // TODO: should be able to calculate min, max, sum with a single iteration through values
    BigDecimal min = values.stream()
        .reduce(BigDecimal.valueOf(Double.MAX_VALUE), BigDecimal::min);

    BigDecimal max = values.stream()
        .reduce(BigDecimal.ZERO, BigDecimal::max);

    BigDecimal mean = values.stream()
        .reduce(BigDecimal.ZERO, (x, y) -> x.add(y, MATH_CONTEXT))
        .divide(new BigDecimal(values.size()), MATH_CONTEXT);

    BigDecimal variance = values.stream()
        .map(x -> x.subtract(mean, MATH_CONTEXT))
        .map(x -> x.multiply(x, MATH_CONTEXT))
        .reduce(BigDecimal.ZERO, (x, y) -> x.add(y, MATH_CONTEXT))
        .divide(new BigDecimal(values.size()), MATH_CONTEXT);

    BigDecimal stddev = variance.sqrt(MATH_CONTEXT);

    return new Statistics(values.size(), min, max, mean, stddev);
  }
}
