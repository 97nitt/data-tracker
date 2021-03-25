package com.cogswell.datatracker.aggregator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class StatisticsTests {

  @Test
  public void testSingleValue() {
    // given
    List<BigDecimal> values = Collections.singletonList(new BigDecimal("1.0", MathContext.UNLIMITED));

    // when
    Statistics statistics = Statistics.from(values);

    // then
    assertEquals(1, statistics.getCount());
    assertEquals(1.0, statistics.getMin().doubleValue());
    assertEquals(1.0, statistics.getMax().doubleValue());
    assertEquals(1.0, statistics.getMean().doubleValue());
    assertEquals(0.0, statistics.getStddev().doubleValue());
  }

  @Test
  public void testMultipleValues() {
    // given
    List<BigDecimal> values = Arrays.asList(
        new BigDecimal("1.0", MathContext.UNLIMITED),
        new BigDecimal("2.0", MathContext.UNLIMITED),
        new BigDecimal("4.0", MathContext.UNLIMITED),
        new BigDecimal("5.0", MathContext.UNLIMITED));

    // when
    Statistics statistics = Statistics.from(values);

    // then
    assertEquals(4, statistics.getCount());
    assertEquals(1.0, statistics.getMin().doubleValue());
    assertEquals(5.0, statistics.getMax().doubleValue());
    assertEquals(3.0, statistics.getMean().doubleValue());
    assertEquals(1.5811388300841898, statistics.getStddev().doubleValue());
  }
}
