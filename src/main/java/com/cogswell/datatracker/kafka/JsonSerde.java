package com.cogswell.datatracker.kafka;

import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonDeserializerConfig;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.Serdes.WrapperSerde;

/**
 * Custom Serde implementation using Confluent's JSON serializer & deserializer.
 */
public class JsonSerde<T> extends WrapperSerde<T> {

  public JsonSerde() {
    super(new KafkaJsonSerializer<>(), new KafkaJsonDeserializer<>());
  }

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    super.configure(configs, isKey);
  }

  /**
   * Get JSON Serde instance.
   *
   * @param type message type class
   * @param isKey true if key Serde, false if value Serde
   * @param <T> message type
   * @return Serde
   */
  public static <T> JsonSerde<T> get(Class<T> type, boolean isKey) {
    Map<String, Object> configs = new HashMap<>();
    if (isKey) {
      configs.put(KafkaJsonDeserializerConfig.JSON_KEY_TYPE, type);
    } else {
      configs.put(KafkaJsonDeserializerConfig.JSON_VALUE_TYPE, type);
    }

    JsonSerde<T> serde = new JsonSerde<>();
    serde.configure(configs, isKey);
    return serde;
  }
}
