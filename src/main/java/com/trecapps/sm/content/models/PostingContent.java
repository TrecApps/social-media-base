package com.trecapps.sm.content.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

@Data
public class PostingContent implements Comparable<PostingContent> {

    String content;
    @JsonFormat(pattern="dd/MM/yyyy HH:mm:ss Z")
    @DateTimeFormat(pattern ="dd/MM/yyyy HH:mm:ss Z")
    OffsetDateTime made;
    String version;

    @Override
    public int compareTo(PostingContent o) {
        return made.compareTo(o.made);
    }
}
