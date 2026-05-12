package com.trecapps.sm.content.models;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Table("reaction_entry")
@Data
public class ReactionEntry {

    transient boolean isNew = false;
    transient String contentParent = null;
    transient String moduleId = null;

    @PrimaryKey
    ReactionId reactionId;

    @Column("account_id")
    UUID accountId;     // The brand id used when reacting to the content
    @Column("block_accounts")
    UUID blockAccounts; // Marker so that Apps can recognize that this reactor is blocking the requester

    String version;     // the version of the content being reacted to, in case the content changes drastically
    @CassandraType(type = CassandraType.Name.TIMESTAMP)
    Instant made;// When the Reaction was made
    @Column("is_stale")
    boolean isStale;    // Whether the content has been updated since the reaction was made
    @Column("is_private")
    boolean isPrivate;  // Whether the id of the reactor should be private


    public void setMade(Instant instant) {
        this.made = instant;
    }

    public void setMade(OffsetDateTime made){
        this.made = made.toInstant();
    }
}
