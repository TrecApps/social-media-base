package com.trecapps.sm.common;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.Collections;

import static org.springframework.data.mongodb.core.ReactiveMongoTemplate.NO_OP_REF_RESOLVER;

@Configuration
@ConditionalOnProperty(prefix = "trecapps.sm.repo", name = "strategy", havingValue = "mongo-uri")
public class MongoSMUrlConfiguration {


    @Autowired
    private ApplicationContext appContext;

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

    private MappingMongoConverter getDefaultMongoConverter(ReactiveMongoDatabaseFactory factory) {

        MongoCustomConversions conversions = new MongoCustomConversions(Collections.emptyList());

        MongoMappingContext context = mongoMappingContext();
        context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        context.afterPropertiesSet();

        MappingMongoConverter converter = new MappingMongoConverter(NO_OP_REF_RESOLVER, context);
        converter.setCustomConversions(conversions);
        converter.setCodecRegistryProvider(factory);
        converter.afterPropertiesSet();

        return converter;
    }

    public MongoMappingContext mongoMappingContext() {
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setApplicationContext(appContext);
        return mappingContext;
    }

    @Bean(name="trecappsSMMongoTemplate")
    public ReactiveMongoTemplate trecappsSMMongoTemplate(
            @Qualifier("trecappsSMMongoFactory") ReactiveMongoDatabaseFactory factory
    ) {
        final MappingMongoConverter converter =getDefaultMongoConverter(factory);
        return new ReactiveMongoTemplate(factory, converter);
    }

}
