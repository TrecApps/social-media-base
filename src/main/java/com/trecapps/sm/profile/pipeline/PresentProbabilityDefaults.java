package com.trecapps.sm.profile.pipeline;

import com.trecapps.sm.common.models.SocialMediaEventType;
import org.springframework.jmx.access.InvalidInvocationException;

public class PresentProbabilityDefaults {

    private PresentProbabilityDefaults()
    {
        throw new InvalidInvocationException("This class is purely static!");
    }

    public static final double PROB_REG_POST = 0.8;
    public static final double PROB_REG_COMMENT = 0.4;
    public static final double PROB_REACT_POST = 0.25;
    public static final double PROB_REACT_COMMENT = 0.15;
    public static final double PROB_EDIT = 0.05;

    public static double getDefaultByType(SocialMediaEventType type)
    {
        return switch(type){
            case CONTENT_EDIT -> PROB_EDIT;
            case POST_REACTION -> PROB_REACT_POST;
            case POST -> PROB_REG_POST;
            case COMMENT -> PROB_REG_COMMENT;
            case COMMENT_REACTION -> PROB_REACT_COMMENT;
        };
    }
}
