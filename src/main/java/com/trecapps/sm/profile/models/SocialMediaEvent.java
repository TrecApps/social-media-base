package com.trecapps.sm.profile.models;

import com.trecapps.sm.common.models.SocialMediaEventType;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

@Table("social_media_event")
@Data
public class SocialMediaEvent {
    @PrimaryKey
    MediaEventId id;

    @Column("content_id")
    String contentId;

    @Column("parent_content_id")
    String parentContentId;

    @Column("type")
    String type;

    @Column("other_profile")
    String otherProfile;

    //@Column("content_poster")

    public void setType(SocialMediaEventType type){
        this.type = type.name();
    }
}
