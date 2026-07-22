package com.trecapps.sm.profile.controllers;

import com.trecapps.sm.common.models.ObjectResponseException;
import com.trecapps.sm.common.models.ResponseObj;
import com.trecapps.sm.profile.dto.*;
import com.trecapps.sm.profile.models.Education;
import com.trecapps.sm.profile.models.Profile;
import com.trecapps.sm.profile.models.WorkExpHolder;
import com.trecapps.sm.profile.services.ProfileService;
import com.trecauth.common.model.AccountList;
import com.trecauth.common.model.TrecauthAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/Profile")
@Slf4j
public class ProfileController {

    @Autowired
    ProfileService profileService;

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(HttpMessageNotReadableException e) {
        log.warn("Returning HTTP 400 Bad Request", e);
    }

    @PostMapping
    Mono<ResponseEntity<ResponseObj>> createProfile(
            Authentication authentication,
            @RequestParam(value = "brandId",defaultValue = "") String brandId,
            @RequestBody PostProfile postProfile
            ){
        return Mono.just((TrecauthAuthentication)authentication)
                .flatMap((TrecauthAuthentication trecAuthentication) -> {
                    AccountList list = trecAuthentication.getList();

                    return profileService.createProfile(list, postProfile);
                })
                .onErrorResume(ObjectResponseException.class, (ObjectResponseException o) -> Mono.just(o.toResponseObj()))
                // ToDo - Handle Unexpected error
                .map(ResponseObj::toEntity);
    }

    @GetMapping("/search")
    Mono<List<ProfileSearchResult>> searchProfiles(
            Authentication authentication,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        return Mono.just((TrecauthAuthentication) authentication)
                .flatMap((TrecauthAuthentication trecAuthentication) -> {
                    return profileService.searchProfiles(trecAuthentication.getList(), query, page, size);
                });
    }

    @GetMapping("/id/{id}")
    Mono<ResponseEntity<Profile>> getProfile(
            Authentication authentication,
            @PathVariable UUID id
    ){
        return Mono.just((TrecauthAuthentication) authentication)
                .flatMap((TrecauthAuthentication trecAuthentication) -> {


                    return profileService.getProfile(trecAuthentication.getList(), id);
                })
                .map(ResponseEntity::ok)
                .onErrorResume(ObjectResponseException.class, (ObjectResponseException o) -> Mono.just(new ResponseEntity<>(o.getStatus())));
    }

    @GetMapping("/basic/{id}")
    Mono<ResponseEntity<BasicProfile>> getBasicProfile(
            Authentication authentication,
            @PathVariable UUID id
    ){
        return Mono.just((TrecauthAuthentication) authentication)
                .flatMap((TrecauthAuthentication trecAuthentication) -> {

                    return profileService.getProfile(trecAuthentication.getList(), id);
                })
                .map(BasicProfile::getInstance)
                .map(ResponseEntity::ok)
                .onErrorResume(ObjectResponseException.class, (ObjectResponseException o) -> Mono.just(new ResponseEntity<>(o.getStatus())));
    }

    @PutMapping("/favorites")
    Mono<ResponseEntity<ResponseObj>> setFavorites(
            Authentication authentication,
            @RequestBody List<Favorite> favorites
    ){
        return Mono.just((TrecauthAuthentication) authentication)
                .flatMap((TrecauthAuthentication trecAuthentication) -> {
                    return profileService.updateFavorites(trecAuthentication.getList(), favorites);
                })
                .map(ResponseEntity::ok);
    }

    ///
    /// Education Endpoints
    ///

    Mono<ResponseEntity<ResponseObj>> handleEducation(
            Authentication authentication,
            Education educationObject,
            String id,
            boolean isDeleting
    ) {
        return Mono.just((TrecauthAuthentication) authentication)
                .flatMap((TrecauthAuthentication trecAuthentication) -> {
                    Mono<ResponseObj> ret;

                    AccountList list = trecAuthentication.getList();


                        ret = isDeleting ?
                                profileService.removeEducation(list, id) :
                                profileService.setEducation(list, id, educationObject);
                    return ret;
                })
                .map(ResponseObj::toEntity);
    }

    @PutMapping("/Education/{id}")
    Mono<ResponseEntity<ResponseObj>> setEducation(
            Authentication authentication,
            @RequestBody Education educationObject,
            @PathVariable String id){
        return handleEducation(authentication, educationObject, id, false);
    }

    @PostMapping("/Education")
    Mono<ResponseEntity<ResponseObj>> setEducation(
            Authentication authentication,
            @RequestBody Education educationObject){
        return handleEducation(authentication, educationObject, null, false);
    }

    @DeleteMapping("/Education/{id}")
    Mono<ResponseEntity<ResponseObj>> setEducation(
            Authentication authentication,
            @PathVariable String id){
        return handleEducation(authentication, null, id, true);
    }

    ///
    /// Work Endpoints
    ///

    Mono<ResponseEntity<ResponseObj>> handleExperience(
            Authentication authentication,
            WorkExpHolder workExpHolder,
            String id,
            boolean isDeleting
    ) {
        return Mono.just((TrecauthAuthentication) authentication)
                .flatMap((TrecauthAuthentication trecAuthentication) -> {
                    Mono<ResponseObj> ret;

                    AccountList list = trecAuthentication.getList();


                    ret = isDeleting ?
                            profileService.removeWorkExperience(list, id) :
                            profileService.setWorkExperience(list, id, workExpHolder);
                    return ret;
                })
                .map(ResponseObj::toEntity);
    }

    @PutMapping("/Experience/{id}")
    Mono<ResponseEntity<ResponseObj>> setExperience(
            Authentication authentication,
            @RequestBody WorkExpHolder workExpHolder,
            @PathVariable String id){
        return handleExperience(authentication, workExpHolder, id, false);
    }

    @PostMapping("/Experience")
    Mono<ResponseEntity<ResponseObj>> setExperience(
            Authentication authentication,
            @RequestBody WorkExpHolder workExpHolder){
        return handleExperience(authentication, workExpHolder, null, false);
    }

    @DeleteMapping("/Experience/{id}")
    Mono<ResponseEntity<ResponseObj>> setExperience(
            Authentication authentication,
            @PathVariable String id){
        return handleExperience(authentication, null, id, true);
    }

    ///
    /// Skills
    ///

    @PutMapping("/Skills/{name}")
    Mono<ResponseEntity<ResponseObj>> setSkill(
            Authentication authentication,
            @RequestBody SkillPost skillPost,
            @PathVariable String name
    ){
        return Mono.just((TrecauthAuthentication) authentication)
                .flatMap((TrecauthAuthentication trecAuthentication) -> {
                    AccountList list = trecAuthentication.getList();

                    return profileService.setSkill(list, name, skillPost);
                })
                .map(ResponseObj::toEntity);
    }

    @DeleteMapping("/Skills/{name}")
    Mono<ResponseEntity<ResponseObj>> setSkill(
            Authentication authentication,
            @PathVariable String name
    ){
        return Mono.just((TrecauthAuthentication) authentication)
                .flatMap((TrecauthAuthentication trecAuthentication) -> {
                    AccountList list = trecAuthentication.getList();

                    return profileService.removeSkill(list, List.of(name));
                })
                .map(ResponseObj::toEntity);
    }
}
