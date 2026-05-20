package com.trecapps.sm.content.pipeline;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitiatorConfig {

    @Bean
    @ConditionalOnProperty(
            prefix = "trecapps.sminitiator",
            name = {"strategy"},
            havingValue = "azure-service-bus-entra"
    )
    IEventInitiator getProducerServiceBusEntra(
            @Value("${trecapps.sminitiator.queue}") String queue,
            @Value("${trecapps.sminitiator.namespace}") String namespace) {
        return new AzureServiceBusEventInitiator(queue, namespace, false);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "trecapps.sminitiator",
            name = {"strategy"},
            havingValue = "azure-service-bus-connection-string"
    )
    IEventInitiator getProducerServiceBusConnString(
            @Value("${trecapps.sminitiator.queue}") String queue,
            @Value("${trecapps.sminitiator.connection}") String connection) {
        return new AzureServiceBusEventInitiator(queue, connection, true);
    }
}
