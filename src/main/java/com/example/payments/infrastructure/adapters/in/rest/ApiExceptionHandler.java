package com.example.payments.infrastructure.adapters.in.rest;
import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import java.time.Instant;import java.util.Map;
@RestControllerAdvice
public class ApiExceptionHandler { @ExceptionHandler(Exception.class) ResponseEntity<Map<String,Object>> handle(Exception ex){ return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("timestamp", Instant.now().toString(), "error", ex.getMessage())); } }
