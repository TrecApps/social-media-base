package com.trecapps.sm.profile.models;

/**
 * Whether a given profile feature should be shown (Note: Recruiter options are meant for Water Cooler)
 */
public enum FeatureShow {
    PRIVATE,
    PRIVATE_AND_RECRUITERS,
    DIRECT_CONNECTIONS,
    DIRECT_CONNECTIONS_AND_RECRUITERS,
    FOLLOWERS,
    FOLLOWERS_AND_RECRUITERS,
    PUBLIC
}
