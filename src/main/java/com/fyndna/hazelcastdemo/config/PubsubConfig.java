package com.fyndna.hazelcastdemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "pubsub")
public class PubsubConfig {

    private String name;
    private String topic;
}
