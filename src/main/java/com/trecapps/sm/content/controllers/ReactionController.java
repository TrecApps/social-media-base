package com.trecapps.sm.content.controllers;

import com.trecapps.sm.common.models.ResponseObj;
import com.trecapps.sm.content.dto.ContentReactionEntry;
import com.trecapps.sm.content.dto.ProfileReactionEntry;
import com.trecapps.sm.content.dto.ReactionPosting;
import com.trecapps.sm.content.services.ReactionService;
import com.trecauth.common.model.TrecauthAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/Reactions")
public class ReactionController {

    @Autowired
    ReactionService reactionService;


    @PostMapping("/{contentId}")
    Mono<ResponseEntity<ResponseObj>> postReaction(
            Authentication authentication,
            @PathVariable UUID contentId,
            @RequestBody ReactionPosting reactionPosting
            ) {
        TrecauthAuthentication trecAuthentication = (TrecauthAuthentication) authentication;
        return reactionService.postReaction(
                trecAuthentication.getList(),
                contentId,
                reactionPosting
        ).map(ResponseObj::toEntity);
    }

    @GetMapping("/count/{contentId}")
    Mono<ResponseEntity<ResponseObj>> getCount(
            Authentication authentication,
            @PathVariable UUID contentId
    ){
        TrecauthAuthentication trecAuthentication = (TrecauthAuthentication) authentication;
        return reactionService.getReactionCount(trecAuthentication.getList().getMainUserAccount(), contentId).map(ResponseObj::toEntity);

    }

    @DeleteMapping("/{contentId}")
    Mono<ResponseEntity<ResponseObj>> deleteReaction(
            Authentication authentication,
            @PathVariable UUID contentId
    ){
        TrecauthAuthentication trecAuthentication = (TrecauthAuthentication) authentication;
        return reactionService.removeReaction(trecAuthentication.getList().getMainUserAccount(), contentId).map(ResponseObj::toEntity);
    }

    @GetMapping("/list/{contentId}")
    Mono<List<ContentReactionEntry>> listReactionsByContent(
            Authentication authentication,
            @PathVariable UUID contentId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        TrecauthAuthentication trecAuthentication = (TrecauthAuthentication) authentication;
        return reactionService.getContentReactionListByContentId(
                trecAuthentication.getList(),
                contentId,
                type,
                page, size
        );
    }

    @GetMapping("/mine")
    Mono<List<ProfileReactionEntry>> listMyReactions(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        TrecauthAuthentication trecAuthentication = (TrecauthAuthentication) authentication;
        return reactionService.getSelfReactions(trecAuthentication.getList().getMainUserAccount(), page, size);
    }

}
