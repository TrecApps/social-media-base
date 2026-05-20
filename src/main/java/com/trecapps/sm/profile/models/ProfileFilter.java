package com.trecapps.sm.profile.models;

import com.trecapps.sm.common.models.SocialMediaEventType;
import lombok.Data;

import java.util.UUID;

@Data
public class ProfileFilter {

    UUID from;
    SocialMediaEventType type;
    double probability;

}
