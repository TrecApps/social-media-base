package com.trecapps.sm.profile.models;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.time.OffsetDateTime;

@Table("connection_entry")
@Data
public class ConnectionEntry {
    @PrimaryKey
    ConnectionLink id;
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    Instant made;
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    Instant accepted;
    @Column("one_way") boolean oneWay;             // Set true when a Brand Profile is involved, false if both parties are User Profiles

    public void setMade(Instant instant) {
        made = instant;
    }

    public void setMade(OffsetDateTime made){
        this.made = made.toInstant();
    }

    public void setAccepted(Instant instant) {
        accepted = instant;
    }

    public void setAccepted(OffsetDateTime accepted){
        this.accepted = accepted.toInstant();
    }
}
