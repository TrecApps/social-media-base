package com.trecapps.sm.content.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ContentPost {

    String content;
    UUID moduleId;
    UUID parentId;
    UUID profileId;

}
