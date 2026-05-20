package com.trecapps.sm.content.pipeline;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.trecapps.sm.common.models.SocialMediaEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class AzureServiceBusEventInitiator implements IEventInitiator {

    ServiceBusSenderAsyncClient serviceBusSenderClient;
    ObjectMapper objectMapper;

    AzureServiceBusEventInitiator(String queueName, String connection, boolean useConnectionString) {
        if (useConnectionString) {
            this.serviceBusSenderClient = (new ServiceBusClientBuilder()).connectionString(connection).sender().queueName(queueName).buildAsyncClient();
        } else {
            DefaultAzureCredential credential = (new DefaultAzureCredentialBuilder()).build();
            this.serviceBusSenderClient = (new ServiceBusClientBuilder()).fullyQualifiedNamespace(String.format("%s.servicebus.windows.net", connection)).credential(credential).sender().queueName(queueName).buildAsyncClient();
        }

        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @SneakyThrows
    public Mono<Void> sendEvent(SocialMediaEvent event) {

            return this.serviceBusSenderClient.sendMessage(new ServiceBusMessage(this.objectMapper.writeValueAsString(event)))
                    .doOnError((Throwable thrown) -> {
                        log.error("Failed to send message {}, reason is: {}", event, thrown.getMessage());
                    });
    }
}
