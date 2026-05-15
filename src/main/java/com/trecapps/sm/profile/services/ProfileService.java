package com.trecapps.sm.profile.services;

import com.trecapps.sm.common.models.ResponseObj;
import com.trecapps.sm.profile.dto.Favorite;
import com.trecapps.sm.profile.dto.PostProfile;
import com.trecapps.sm.profile.dto.ProfileSearchResult;
import com.trecapps.sm.profile.dto.SkillPost;
import com.trecapps.sm.profile.models.Education;
import com.trecapps.sm.profile.models.Profile;
import com.trecapps.sm.profile.models.WorkExpHolder;
import com.trecauth.common.model.AccountList;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ProfileService {

    Mono<ResponseObj> createProfile(AccountList list, PostProfile post);

    Mono<List<ProfileSearchResult>> searchProfiles(AccountList list, String query, int page, int size);

    Mono<Profile> getProfile(AccountList list, UUID profileId);

    Mono<ResponseObj> updateFavorites(AccountList list, List<Favorite> favorites);

    Mono<ResponseObj> setEducation(AccountList list, @Nullable String eduId, Education education);

    Mono<ResponseObj> setWorkExperience(AccountList list, @Nullable String perspective, WorkExpHolder experience);

    Mono<ResponseObj> setSkill(AccountList list, @NotNull String name, SkillPost skillPost);

    Mono<ResponseObj> removeEducation(AccountList list, @NotNull String eduId);

    Mono<ResponseObj> removeWorkExperience(AccountList list, @NotNull String perspective);

    Mono<ResponseObj> removeSkill(AccountList list, @NotNull List<String> names);



}
