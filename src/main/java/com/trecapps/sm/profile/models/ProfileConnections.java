package com.trecapps.sm.profile.models;

import lombok.Data;

@Data
public class ProfileConnections {

    ConnectionEntry asFollower;
    ConnectionEntry asFollowee;

}
