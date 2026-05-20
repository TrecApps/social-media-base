package com.trecapps.sm.profile.controllers;

import com.trecapps.sm.common.models.ResponseObj;
import com.trecapps.sm.profile.models.ConnectionEntry;
import com.trecapps.sm.profile.models.ProfileConnections;
import com.trecapps.sm.profile.services.ConnectionsService;
import com.trecauth.common.model.TrecauthAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/Connections")
public class ConnectionController {

    @Autowired
    ConnectionsService connectionsService;

    @GetMapping("/follow")
    Mono<ResponseEntity<ResponseObj>> follow(
            Authentication authentication,
            @RequestParam UUID profileId
    ) {
        TrecauthAuthentication trecAuthentication = (TrecauthAuthentication) authentication;
        return connectionsService.attemptFollow(
                trecAuthentication.getList(),
                profileId
        ).map(ResponseObj::toEntity);
    }

    @GetMapping("/with/{id}")
    Mono<ResponseEntity<ProfileConnections>> with(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        TrecauthAuthentication trecAuthentication = (TrecauthAuthentication) authentication;
        return connectionsService.getTwoWayConnection(
                trecAuthentication.getList().getCurrentAccount().getId(),
                id
        ).map(ResponseEntity::ok);
    }

    @GetMapping("/approve")
    Mono<ResponseEntity<ResponseObj>> approveFollow(
            Authentication authentication,
            @RequestParam UUID profileId
    ) {
        TrecauthAuthentication trecAuthentication = (TrecauthAuthentication) authentication;
        return connectionsService.approveRequest(
                trecAuthentication.getList(),
                profileId
        ).map(ResponseObj::toEntity);
    }

    @GetMapping("/unfollow")
    Mono<ResponseEntity<ResponseObj>> unfollow(
            Authentication authentication,
            @RequestParam UUID profileId
    ) {
        TrecauthAuthentication trecAuthentication = (TrecauthAuthentication) authentication;
        return connectionsService.unfollow(
                trecAuthentication.getList(),
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
        TrecauthAuthentication trecAuthentication = (TrecauthAuthentication) authentication;
        return connectionsService.findMyConnections(
                trecAuthentication.getList(),
                page, size,
                "followers".equals(getType)
        );
    }

}
