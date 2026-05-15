package com.trecapps.sm.profile.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ProfileSearchResult {

    UUID id;
    String displayName;
    String shortAboutMe;

}
