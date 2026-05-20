package com.trecapps.sm.profile.dto;

import com.trecapps.sm.common.models.SocialMediaEventType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PostFilterRequest {

    @NotNull
    SocialMediaEventType type;
    UUID from;

    boolean decrease = true;

}
