package com.trecapps.sm.profile.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalWebFluxExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalWebFluxExceptionHandler.class);

    // 1. Catches malformed JSON payloads and missing parameters
    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleWebInputException(ServerWebInputException ex, ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();

        // Log the root cause (e.g., Jackson DecodingException)
        logger.warn("400 Bad Request on path: {} | Reason: {} | Cause: {}",
                path, ex.getReason(), ex.getCause() != null ? ex.getCause().getMessage() : "None");

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getReason());

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
    }

    // 2. Catches @Valid / @Validated DTO binding validation failures
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationException(WebExchangeBindException ex, ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        logger.warn("400 Bad Request (Validation Failure) on path: {}", path);

        Map<String, Object> errors = new HashMap<>();
        ex.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("details", errors);

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
    }
}

