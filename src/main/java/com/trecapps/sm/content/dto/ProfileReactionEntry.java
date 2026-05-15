package com.trecapps.sm.content.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ProfileReactionEntry {

    UUID contentId;
    UUID brandId;
    String type;
    String version;
    boolean isPrivate;
    boolean isStale;
    OffsetDateTime made;

}
