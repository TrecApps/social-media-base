package com.trecapps.sm.profile.dto;

import com.trecapps.sm.profile.models.Profile;
import lombok.Data;

import java.util.UUID;

@Data
public class BasicProfile {
    UUID id;
    String displayName;
    String shortAboutMe;
    String pronouns;

    public static BasicProfile getInstance(Profile profile) {
        BasicProfile ret = new BasicProfile();
        ret.setId(profile.getId());
        ret.setDisplayName(profile.getTitle());
        ret.setShortAboutMe(profile.getAboutMeShort());

        PronounVisibility vis = profile.getPronounVisibility();
        if(vis == PronounVisibility.SHOW_ALL || vis == PronounVisibility.SHOW_ON_POSTS){
            ret.setPronouns(profile.getPronouns());
        }
        return ret;
    }
}
