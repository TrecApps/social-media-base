package com.trecapps.sm.profile.models;

import lombok.Data;

import java.util.List;

@Data
public class Education {
    FeatureShow showEducation = FeatureShow.PUBLIC;
    boolean allowAnalytics = true;

    String schoolId;
    String schoolName;
    EduDegree degree;

    List<Subject> majors;
    List<Subject> minors;

    float gpa;
    boolean showGpa = false;

    MonthYear start;
    MonthYear graduation;
}
