package com.example.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/** Section 8. RAG 품질 개선 — 튜닝과 평가 */
@SpringBootApplication
public class Section08Application {
    public static void main(String[] args) {
        SpringApplication.run(Section08Application.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
