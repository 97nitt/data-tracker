package com.cogswell.datatracker.kafka;

import java.util.Properties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Kafka producer configuration.
 */
@Data
@Component
@ConfigurationProperties("kafka.producer")
public class ProducerProperties {

  private String topic;
  private Properties properties;
}
