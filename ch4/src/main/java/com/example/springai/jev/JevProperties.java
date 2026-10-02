package com.example.springai.jev;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jev")
public record JevProperties(boolean enabled, String apiKey, String baseUrl, String model) {
}
