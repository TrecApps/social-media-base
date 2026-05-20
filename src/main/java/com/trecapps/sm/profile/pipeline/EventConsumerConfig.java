package com.trecapps.sm.profile.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventConsumerConfig {
    @Bean
    @ConditionalOnProperty(
            prefix = "trecapps.smconsumer",
            name = {"strategy"},
            havingValue = "azure-service-bus-entra"
    )
    IEventConsumer getConsumerServiceBusEntra(
            @Value("${trecapps.smconsumer.queue}") String queue,
            @Value("${trecapps.smconsumer.namespace}") String namespace,
            ObjectMapper objectMapper) {
        return new AzureServiceBusEventConsumer(queue, namespace, objectMapper, false);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "trecapps.smconsumer",
            name = {"strategy"},
            havingValue = "azure-service-bus-connection-string"
    )
    IEventConsumer getConsumerServiceBusConnString(
            @Value("${trecapps.smconsumer.queue}") String queue,
            @Value("${trecapps.smconsumer.connection}") String connection,
            ObjectMapper objectMapper) {
        return new AzureServiceBusEventConsumer(queue, connection, objectMapper, true);
    }
}
