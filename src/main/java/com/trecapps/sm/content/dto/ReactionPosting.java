package com.trecapps.sm.content.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ReactionPosting {

    String reactType;
    boolean makePrivate = false;
    UUID accountId;

}
