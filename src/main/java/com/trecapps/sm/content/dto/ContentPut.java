package com.trecapps.sm.content.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ContentPut {

    String content;
    UUID contentId;

}
