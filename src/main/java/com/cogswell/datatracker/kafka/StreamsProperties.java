package com.cogswell.datatracker.kafka;

import java.util.Properties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("kafka.streams")
public class StreamsProperties {

  private String input;
  private String output;
  private Properties properties;
}
