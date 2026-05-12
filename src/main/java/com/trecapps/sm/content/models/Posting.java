package com.trecapps.sm.content.models;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

@Data
@Document("posting")
@Slf4j
public class Posting {

    static final String VERSION_CHARS =
            "ABCDEFG" +
                    "abcdefg" +
                    "01234567890";

    private String generateVersion(){
        StringBuilder builder = new StringBuilder();
        for(int c = 0; c < 4; c++){
            builder.append(
                    VERSION_CHARS.charAt(
                            (int) Math.floor(Math.random() * VERSION_CHARS.length())
                    )
            );
        }
        return builder.toString();
    }

    private boolean versionUsed(String version) {
        for(PostingContent contentVersions : contents){
            if(version.equals(contentVersions.version))
                return true;
        }
        return false;
    }

    @MongoId
    UUID id;          // ID of the posting

    List<UUID> parents = new ArrayList<>(); // Parents (Posting is a comment if non-empty)
    UUID parent;

    UUID posterId;          // Account ID of the poster
    UUID userAccountId;   // User Account that posted it
    UUID blockerAccount; // Account ID of the poster (used to hide from anyone the poster has blocked)

    UUID ownerId;       // Populated only if poster directs it to another profile
    UUID ownerBlocker; // Provided if the ownerId is set, similar purpose to "blockerAccount"

    UUID moduleId;        // If part of a group, which group this belongs to
    //@JsonFormat(pattern="dd/MM/yyyy HH:mm:ss Z")
    Instant made;
    //@JsonFormat(pattern="dd/MM/yyyy HH:mm:ss Z")
    Instant deleteSet;

    SortedSet<PostingContent> contents = new TreeSet<>();

    public boolean isPost() {
        return parents.isEmpty();
    }

    public void setParents(Posting parentPosting){
        if(new HashSet<>(parentPosting.getParents()).size() != parentPosting.getParents().size()){
            log.error("Circular Parent Detection on Posting {} detected!", parentPosting.getId());
            throw new IllegalStateException(String.format("Posting with Circular parenting detected. Posting id %s", parentPosting.getId()));
        }

        parents.clear();
        parents.addAll(parentPosting.getParents());
        parents.add(parentPosting.getId());
    }

    public void appendContent(String content) {
        String version;
        do{
            version = this.generateVersion();
        } while(this.versionUsed(version));

        PostingContent newContent = new PostingContent();
        newContent.setContent(content);
        newContent.setMade(OffsetDateTime.now());
        newContent.setVersion(version);

        this.contents.add(newContent);
    }
}
