package com.trecapps.sm.profile.models;

import lombok.Data;

@Data
public class Skill {
    String name;
    String details;
    int level = -1;
}
