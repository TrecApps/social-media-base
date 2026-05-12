package com.trecapps.sm.profile.models;

import java.util.List;

public class Constants {

    public static boolean stringNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static final List<String> VALID_PRONOUNS = List.of(
            "he/him",
            "she/her",
            "they/them"
    );


    public static final List<FeatureShow> RECRUITER_LIST = List.of(
            FeatureShow.FOLLOWERS_AND_RECRUITERS,
            FeatureShow.DIRECT_CONNECTIONS_AND_RECRUITERS,
            FeatureShow.PRIVATE_AND_RECRUITERS
    );

    public static final List<FeatureShow> DIRECT_CONNECTION_LIST = List.of(
            FeatureShow.DIRECT_CONNECTIONS,
            FeatureShow.DIRECT_CONNECTIONS_AND_RECRUITERS
    );

    public static final List<FeatureShow> FOLLOWER_SHOW_LIST = List.of(
            FeatureShow.FOLLOWERS,
            FeatureShow.FOLLOWERS_AND_RECRUITERS
    );
}
