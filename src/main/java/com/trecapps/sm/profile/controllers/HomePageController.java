package com.trecapps.sm.profile.controllers;

import com.trecapps.sm.common.models.ResponseObj;
import com.trecapps.sm.profile.dto.PostFilterRequest;
import com.trecapps.sm.profile.models.ProfileFilterList;
import com.trecapps.sm.profile.models.SocialMediaEvent;
import com.trecapps.sm.profile.services.HomePageManagementService;
import com.trecauth.common.model.TrecauthAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/Home")
@Slf4j
public class HomePageController {

    @Autowired
    HomePageManagementService homePageManagementService;

    @GetMapping
    Mono<ResponseEntity<List<SocialMediaEvent>>> getHomePageContent(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Authentication authentication
    ){
        return Mono.just((TrecauthAuthentication) authentication)
                .flatMap((TrecauthAuthentication tAuth) ->
                        homePageManagementService.getHomePageEvents(
                                tAuth.getList().getCurrentAccount().getId(),
                                category,
                                page,
                                size
                        )
                ).map(ResponseEntity::ok)
                .onErrorResume((Throwable e) -> {
                    log.error("Issue retrieving Social Media Content!", e);
                    return Mono.just(new ResponseEntity<>(HttpStatusCode.valueOf(500)));
                })
                ;
    }

    @PostMapping("/filters")
    Mono<ResponseEntity<ResponseObj>> updateFilters(
            @RequestBody PostFilterRequest request,
            Authentication authentication
    ){
        return Mono.just((TrecauthAuthentication) authentication)
                .flatMap((TrecauthAuthentication tAuth) ->
                    homePageManagementService.addOrUpdateFilter(
                            tAuth.getList().getCurrentAccount().getId(),
                            request
                    ).onErrorResume((Throwable e) -> {
                        log.error(
                                "Error updating Filters for {}",
                                tAuth.getList().getCurrentAccount().getId(),
                                e
                                );
                        return Mono.just(ResponseObj.getInstance(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update Filters"));
                    })
                )
                .map(ResponseObj::toEntity)
                ;
    }

    @DeleteMapping("/filters")
    Mono<ResponseEntity<ResponseObj>> removeFilter(
            @RequestBody PostFilterRequest request,
            Authentication authentication
    ){
        return Mono.just((TrecauthAuthentication) authentication)
                .flatMap((TrecauthAuthentication tAuth) ->
                        homePageManagementService.removeFilter(
                                tAuth.getList().getCurrentAccount().getId(),
                                request
                        ).onErrorResume((Throwable e) -> {
                            log.error(
                                    "Error updating Filters for {}",
                                    tAuth.getList().getCurrentAccount().getId(),
                                    e
                            );
                            return Mono.just(ResponseObj.getInstance(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update Filters"));
                        })
                )
                .map(ResponseObj::toEntity)
                ;
    }

    @GetMapping("/filters")
    Mono<ResponseEntity<ProfileFilterList>> getFilterList(Authentication authentication){
        return Mono.just((TrecauthAuthentication) authentication)
                .flatMap((TrecauthAuthentication tAuth) -> homePageManagementService.getFilterList(
                        tAuth.getList().getCurrentAccount().getId()
                )).map(ResponseEntity::ok)
                .onErrorResume((Throwable e) -> {
                    log.error("Issue retrieving Profile list!", e);
                    return Mono.just(new ResponseEntity<>(HttpStatusCode.valueOf(500)));
                });
    }

}
