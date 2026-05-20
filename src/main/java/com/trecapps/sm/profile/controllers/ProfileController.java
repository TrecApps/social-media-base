package com.trecapps.sm.profile.controllers;

import com.trecapps.auth.common.models.TcBrands;
import com.trecapps.auth.common.models.TcUser;
import com.trecapps.auth.common.models.TrecAuthentication;
import com.trecapps.auth.webflux.services.IUserStorageServiceAsync;
import com.trecapps.sm.common.functionality.ObjectResponseException;
import com.trecapps.sm.common.models.ResponseObj;
import com.trecapps.sm.profile.dto.Favorite;
import com.trecapps.sm.profile.dto.PostProfile;
import com.trecapps.sm.profile.dto.ProfileSearchResult;
import com.trecapps.sm.profile.dto.SkillPost;
import com.trecapps.sm.profile.models.Education;
import com.trecapps.sm.profile.models.Profile;
import com.trecapps.sm.profile.models.WorkExpHolder;
import com.trecapps.sm.profile.service.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/Profile")
@Slf4j
public class ProfileController {

    @Autowired
    ProfileService profileService;

    @Autowired
    IUserStorageServiceAsync userStorageService;

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
        return Mono.just((TrecAuthentication)authentication)
                .flatMap((TrecAuthentication trecAuthentication) -> {
                    TcUser user = trecAuthentication.getUser();
                    TcBrands brands = trecAuthentication.getBrand();

                    Mono<ResponseObj> ret;

                    if(!brandId.trim().isEmpty()){
                        if(!user.getBrands().contains(brandId))
                            throw new ObjectResponseException(HttpStatus.FORBIDDEN, "Brand does not belong to you!");

                        ret = userStorageService
                                .getBrandById(brandId)
                                .flatMap((Optional<TcBrands> oBrands) -> {
                                    if(oBrands.isEmpty())
                                        throw new ObjectResponseException(HttpStatus.INTERNAL_SERVER_ERROR, "Brand Account not Found!");
                                    return profileService.createProfile(user, oBrands.get(), postProfile);
                                });
                    } else {
                        ret = profileService.createProfile(user, brands, postProfile);
                    }
                    return ret;
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
        return Mono.just((TrecAuthentication) authentication)
                .flatMap((TrecAuthentication trecAuthentication) -> {
                    TcUser user = trecAuthentication.getUser();
                    return profileService.searchProfiles(user.getId(), query, page, size);
                });
    }

    @GetMapping("/id/{id}")
    Mono<ResponseEntity<Profile>> getProfile(
            Authentication authentication,
            @PathVariable String id
    ){
        return Mono.just((TrecAuthentication) authentication)
                .flatMap((TrecAuthentication trecAuthentication) -> {
                    TcUser user = trecAuthentication.getUser();
                    TcBrands brands = trecAuthentication.getBrand();

                    String[] idComponents = id.split("-", 2);
                    if(idComponents.length != 2)
                        throw new ObjectResponseException(HttpStatus.BAD_REQUEST, "invalid id format");
                    if(idComponents[1].length() == 32){
                        idComponents[1] =
                                idComponents[1].substring(0, 8) + "-" +
                                        idComponents[1].substring(8, 12) + "-" +
                                        idComponents[1].substring(12, 16) + "-" +
                                        idComponents[1].substring(16, 20) + "-" +
                                        idComponents[1].substring(20);
                    }

                    return profileService.getProfile(user, brands == null ? null : brands.getId(), String.join("-", idComponents));
                })
                .map(ResponseEntity::ok)
                .onErrorResume(ObjectResponseException.class, (ObjectResponseException o) -> Mono.just(new ResponseEntity<>(o.getStatus())));
    }

    @GetMapping("/basic/{id}")
    Mono<ResponseEntity<BasicProfile>> getBasicProfile(
            Authentication authentication,
            @PathVariable String id
    ){
        return Mono.just((TrecAuthentication) authentication)
                .flatMap((TrecAuthentication trecAuthentication) -> {
                    TcUser user = trecAuthentication.getUser();
                    TcBrands brands = trecAuthentication.getBrand();
                    return profileService.getProfile(user, brands == null ? null : brands.getId(), id);
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
        return Mono.just((TrecAuthentication) authentication)
                .flatMap((TrecAuthentication trecAuthentication) -> {
                    TcUser user = trecAuthentication.getUser();
                    TcBrands brands = trecAuthentication.getBrand();
                    return profileService.updateFavorites(user.getId(), brands == null ? null : brands.getId(), favorites);
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
        return Mono.just((TrecAuthentication) authentication)
                .flatMap((TrecAuthentication trecAuthentication) -> {
                    Mono<ResponseObj> ret;

                    TcUser user = trecAuthentication.getUser();
                    TcBrands brands = trecAuthentication.getBrand();
                    String brandId = brands == null ? null : brands.getId();


                        ret = isDeleting ?
                                profileService.removeEducation(user.getId(), brandId, id) :
                                profileService.setEducation(user.getId(), brandId, id, educationObject);
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
        return Mono.just((TrecAuthentication) authentication)
                .flatMap((TrecAuthentication trecAuthentication) -> {
                    Mono<ResponseObj> ret;

                    TcUser user = trecAuthentication.getUser();
                    TcBrands brands = trecAuthentication.getBrand();
                    String brandId = brands == null ? null : brands.getId();


                    ret = isDeleting ?
                            profileService.removeWorkExperience(user.getId(), brandId, id) :
                            profileService.setWorkExperience(user.getId(), brandId, id, workExpHolder);
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
        return Mono.just((TrecAuthentication) authentication)
                .flatMap((TrecAuthentication trecAuthentication) -> {
                    TcUser user = trecAuthentication.getUser();
                    TcBrands brands = trecAuthentication.getBrand();
                    String brandId = brands == null ? null : brands.getId();

                    return profileService.setSkill(user.getId(), brandId, name, skillPost);
                })
                .map(ResponseObj::toEntity);
    }

    @DeleteMapping("/Skills/{name}")
    Mono<ResponseEntity<ResponseObj>> setSkill(
            Authentication authentication,
            @PathVariable String name
    ){
        return Mono.just((TrecAuthentication) authentication)
                .flatMap((TrecAuthentication trecAuthentication) -> {
                    TcUser user = trecAuthentication.getUser();
                    TcBrands brands = trecAuthentication.getBrand();
                    String brandId = brands == null ? null : brands.getId();

                    return profileService.removeSkill(user.getId(), brandId, List.of(name));
                })
                .map(ResponseObj::toEntity);
    }
}
