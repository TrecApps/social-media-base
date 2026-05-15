package com.trecapps.sm.common.models;

import java.time.OffsetDateTime;

public class ProfileConnection {
    String follower;    // Who is following (or made the request)
    String followee;    // Who is being followed (or received the request)
    ProfileConnectionType type;

    OffsetDateTime made;
    OffsetDateTime accepted;
}
