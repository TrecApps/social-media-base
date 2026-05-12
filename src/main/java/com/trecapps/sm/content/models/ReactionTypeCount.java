package com.trecapps.sm.content.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReactionTypeCount {
    String type;
    Long count;
}
