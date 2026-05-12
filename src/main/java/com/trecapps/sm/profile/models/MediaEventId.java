package com.trecapps.sm.profile.models;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.time.Instant;

@Data
@PrimaryKeyClass
public class MediaEventId {

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    @Column("profile_id")
    String profile;


    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 1)
    String category;        // The category to find the entry in

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 2)
    @Column("random_id")
    String randomId;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 3)
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    Instant added;
}
