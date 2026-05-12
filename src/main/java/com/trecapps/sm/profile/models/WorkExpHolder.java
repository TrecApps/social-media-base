package com.trecapps.sm.profile.models;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class WorkExpHolder {
    @Indexed
    Set<String> perspective;        // Perspectives of a set of work experiences
    FeatureShow showExperiences = FeatureShow.PUBLIC; // Whether to show based off of who is looking
    boolean allowAnalytics = true;

    List<WorkExp> workExperience = new ArrayList<>();         // Set of Work experiences
}
