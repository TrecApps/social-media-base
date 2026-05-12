package com.trecapps.sm.profile.models;

import lombok.Data;

@Data
public class ProfileLink {
    String title;
    String link;
    FeatureShow showLink = FeatureShow.PUBLIC;

    /// ToDo implement validation

    public boolean validate(){
        return true;
    }

    private boolean validateFacebook(){
        return true;
    }

    private boolean validateXorTwitter(){
        return true;
    }

    private boolean validateBlueSky(){
        return true;
    }

    private boolean validateReddit() {
        return true;
    }
}
