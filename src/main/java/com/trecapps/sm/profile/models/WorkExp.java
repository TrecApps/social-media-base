package com.trecapps.sm.profile.models;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class WorkExp {

    WorkType type;

    String employerId;
    String employerName;

    Date startDate;

    Date endDate;

    String title;

    String description;

    List<WorkExp> subExperience;

    String shortSubExperience;

}
