package com.trecapps.sm.profile.models;

import com.trecapps.sm.profile.dto.Favorite;
import com.trecapps.sm.profile.dto.PronounVisibility;
import com.trecauth.common.model.AccountType;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Data
@Document("Profile-${trecapps.sm.name}")
public class Profile {

    @MongoId
    UUID id;

    AccountType profileType; // Whether this is a user or brand account

    UUID blockerId; // Marker indicating that this profile should be blocked from the requester (if blocked)

    @Indexed
    String title;

    String aboutMe;
    String aboutMeShort;

    List<ProfileLink> links = new ArrayList<>();

    String pronouns;
    PronounVisibility pronounVisibility;


    // For Coffeeshop
    List<Favorite> favorites = new ArrayList<>();

    List<Education> education = new ArrayList<>(); // and Water Cooler

    //Set<String> brandLikes;

    List<BrandDislike> brandDislikes;

    // Water Cooler

    List<WorkExpHolder> workExperiences = new ArrayList<>();

    List<Skill> skills = new ArrayList<>();


    // Validator methods

    boolean validatePronouns(){
        if(Constants.stringNullOrEmpty(pronouns)) return true;

        String tempPronouns = pronouns.toLowerCase(Locale.ROOT).replaceAll(" ", "");
        for(String validPronoun : Constants.VALID_PRONOUNS){
            if(validPronoun.equals(tempPronouns)) {
                pronouns = tempPronouns;
                return true;
            }
        }
        return false;
    }

    private static boolean keep(boolean isRecruiter, boolean isConnection, boolean isFollower, Education education1){
        if(education1.showEducation == FeatureShow.PUBLIC) return true;
        if(isRecruiter)
            return Constants.RECRUITER_LIST.contains(education1.showEducation);
        if(isConnection)
            return Constants.DIRECT_CONNECTION_LIST.contains(education1.showEducation);
        if(isFollower)
            return Constants.FOLLOWER_SHOW_LIST.contains(education1.showEducation);
        return false;
    }

    private static boolean keep(boolean isRecruiter, boolean isConnection, boolean isFollower, WorkExpHolder experience){
        if(experience.showExperiences == FeatureShow.PUBLIC) return true;
        if(isRecruiter)
            return Constants.RECRUITER_LIST.contains(experience.showExperiences);
        if(isConnection)
            return Constants.DIRECT_CONNECTION_LIST.contains(experience.showExperiences);
        if(isFollower)
            return Constants.FOLLOWER_SHOW_LIST.contains(experience.showExperiences);
        return false;
    }

    public void filterData(boolean isRecruiter, boolean isConnection, boolean isFollower){
        if(pronounVisibility == null ||
                pronounVisibility.equals(PronounVisibility.DO_NOT_SHOW) ||
                pronounVisibility.equals(PronounVisibility.SHOW_ON_POSTS)
        )
            pronouns = null;
        pronounVisibility = null;

        this.education = this.education.stream().filter((Education e) -> keep(isRecruiter, isConnection, isFollower, e)).toList();
        this.workExperiences = this.workExperiences.stream().filter((WorkExpHolder w) -> keep(isRecruiter, isConnection, isFollower, w)).toList();
    }
}
