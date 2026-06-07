package com.trecapps.sm.common;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;

@Configuration
@ConditionalOnProperty(prefix = "trecapps.sm.repo", name = "strategy", havingValue = "mongo-uri")
public class MongoSMUrlConfiguration {

    @Bean
    public MongoClient trecappsSMMongoClient(
            @Value("${trecapps.mongo.sm.uri}")String mongoUri
    ) {

        return MongoClients.create(MongoClientSettings.builder().uuidRepresentation(UuidRepresentation.STANDARD)
                .applyConnectionString(new ConnectionString(mongoUri)).build());
    }

    @Bean
    @Primary
    public ReactiveMongoDatabaseFactory trecappsSMMongoFactory(
            @Qualifier("trecappsSMMongoClient") MongoClient client,
            @Value("${trecapps.mongo.sm.database}") String database
    ){
        return new SimpleReactiveMongoDatabaseFactory(client, database);
    }

    @Bean(name="trecappsSMMongoTemplate")
    public ReactiveMongoTemplate trecappsSMMongoTemplate(
            @Qualifier("trecappsSMMongoFactory") ReactiveMongoDatabaseFactory factory
    ) {
        return new ReactiveMongoTemplate(factory);
    }

}
