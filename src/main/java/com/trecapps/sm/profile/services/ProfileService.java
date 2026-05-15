package com.trecapps.sm.profile.service;

import com.trecapps.auth.common.models.TcBrands;
import com.trecapps.auth.common.models.TcUser;
import com.trecapps.sm.common.models.ResponseObj;
import com.trecapps.sm.profile.dto.Favorite;
import com.trecapps.sm.profile.dto.PostProfile;
import com.trecapps.sm.profile.dto.ProfileSearchResult;
import com.trecapps.sm.profile.dto.SkillPost;
import com.trecapps.sm.profile.models.Education;
import com.trecapps.sm.profile.models.Profile;
import com.trecapps.sm.profile.models.WorkExpHolder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ProfileService {

    Mono<ResponseObj> createProfile(@NotNull TcUser userId, @Nullable TcBrands brandId, PostProfile post);

    Mono<List<ProfileSearchResult>> searchProfiles(@NotNull String userId, String query, int page, int size);

    Mono<Profile> getProfile(@NotNull TcUser userId, @Nullable String brandId, String profileId);

    Mono<ResponseObj> updateFavorites(@NotNull String userId, @Nullable String brandId, List<Favorite> favorites);

    Mono<ResponseObj> setEducation(@NotNull String userId, @Nullable String brandId, @Nullable String eduId, Education education);

    Mono<ResponseObj> setWorkExperience(@NotNull String userId, @Nullable String brandId, @Nullable String perspective, WorkExpHolder experience);

    Mono<ResponseObj> setSkill(@NotNull String userId, @Nullable String brandId, @NotNull String name, SkillPost skillPost);

    Mono<ResponseObj> removeEducation(@NotNull String userId, @Nullable String brandId, @NotNull String eduId);

    Mono<ResponseObj> removeWorkExperience(@NotNull String userId, @Nullable String brandId, @NotNull String perspective);

    Mono<ResponseObj> removeSkill(@NotNull String userId, @Nullable String brandId, @NotNull List<String> names);



}
