package com.example.payments.infrastructure.config;
import com.fasterxml.jackson.databind.ObjectMapper;import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;import org.springframework.context.annotation.*;
@Configuration
public class JacksonConfig { @Bean ObjectMapper objectMapper(){ return new ObjectMapper().registerModule(new JavaTimeModule()); } }
