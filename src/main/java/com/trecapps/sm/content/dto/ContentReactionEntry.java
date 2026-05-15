package com.trecapps.sm.content.dto;

import lombok.Data;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ContentReactionEntry {

    UUID profileId;
    String type;
    Instant made;

}
