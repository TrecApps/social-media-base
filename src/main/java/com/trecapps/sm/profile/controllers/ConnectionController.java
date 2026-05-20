package com.trecapps.sm.profile.controllers;

import com.trecapps.auth.common.models.TrecAuthentication;
import com.trecapps.sm.common.functionality.ProfileFunctionality;
import com.trecapps.sm.common.models.ResponseObj;
import com.trecapps.sm.profile.models.ConnectionEntry;
import com.trecapps.sm.profile.models.ProfileConnections;
import com.trecapps.sm.profile.service.ConnectionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/Connections")
public class ConnectionController {

    @Autowired
    ConnectionsService connectionsService;

    @GetMapping("/follow")
    Mono<ResponseEntity<ResponseObj>> follow(
            Authentication authentication,
            @RequestParam String profileId
    ) {
        TrecAuthentication trecAuthentication = (TrecAuthentication) authentication;
        return connectionsService.attemptFollow(
                trecAuthentication.getUser(),
                trecAuthentication.getBrand(),
                profileId
        ).map(ResponseObj::toEntity);
    }

    @GetMapping("/with/{id}")
    Mono<ResponseEntity<ProfileConnections>> with(
            Authentication authentication,
            @PathVariable String id
    ) {
        TrecAuthentication trecAuthentication = (TrecAuthentication) authentication;
        return connectionsService.getTwoWayConnection(
                ProfileFunctionality.getProfileId(trecAuthentication.getUser(), trecAuthentication.getBrand()),
                id
        ).map(ResponseEntity::ok);
    }

    @GetMapping("/approve")
    Mono<ResponseEntity<ResponseObj>> approveFollow(
            Authentication authentication,
            @RequestParam String profileId
    ) {
        TrecAuthentication trecAuthentication = (TrecAuthentication) authentication;
        return connectionsService.approveRequest(
                trecAuthentication.getUser(),
                profileId
        ).map(ResponseObj::toEntity);
    }

    @GetMapping("/unfollow")
    Mono<ResponseEntity<ResponseObj>> unfollow(
            Authentication authentication,
            @RequestParam String profileId
    ) {
        TrecAuthentication trecAuthentication = (TrecAuthentication) authentication;
        return connectionsService.unfollow(
                trecAuthentication.getUser(),
                trecAuthentication.getBrand(),
                profileId
        ).map(ResponseObj::toEntity);
    }

    @GetMapping("/{getType:followers|followees}")
    Mono<List<ConnectionEntry>> getConnections(
            Authentication authentication,
            @PathVariable String getType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        TrecAuthentication trecAuthentication = (TrecAuthentication) authentication;
        return connectionsService.findMyConnections(
                trecAuthentication.getUser(),
                trecAuthentication.getBrand(),
                page, size,
                "followers".equals(getType)
        );
    }

}
