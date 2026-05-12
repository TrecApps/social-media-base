package com.trecapps.sm.common.models;

public enum SocialMediaEventType {
    POST,               // A new Post was made
    COMMENT,            // A new Comment was made
    CONTENT_EDIT,       // A Posting was edited (alert previous reactors that their reaction is now stale)
    POST_REACTION,      // A reaction to a post
    COMMENT_REACTION;   // A reaction to a comment was made
}
