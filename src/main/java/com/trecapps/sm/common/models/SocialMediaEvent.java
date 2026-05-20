package com.trecapps.sm.common.models;

import lombok.Data;

import java.util.UUID;

@Data
public class SocialMediaEvent {

    UUID resourceId;
    UUID postId;
    SocialMediaEventType type;
    UUID module;
    UUID profile;
    UUID userId;
    String reaction;
    String category;
}
