package com.trecapps.sm.content.controllers;

import com.trecapps.sm.common.models.ResponseObj;
import com.trecapps.sm.content.dto.ContentPost;
import com.trecapps.sm.content.dto.ContentPut;
import com.trecapps.sm.content.models.Posting;
import com.trecapps.sm.content.services.ContentService;
import com.trecauth.common.model.TrecauthAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/Content")
public class ContentController {

    @Autowired
    ContentService contentService;

    @PostMapping
    Mono<ResponseEntity<ResponseObj>> postContent(
            Authentication authentication,
            @RequestBody ContentPost post
            ){
        TrecauthAuthentication trecAuthentication = (TrecauthAuthentication) authentication;
        return contentService.postContent(trecAuthentication.getList(), post)
                .map(ResponseObj::toEntity);
    }

    @PutMapping
    Mono<ResponseEntity<ResponseObj>> putContent(
            Authentication authentication,
            @RequestBody ContentPut put
    ){
        TrecauthAuthentication trecAuthentication = (TrecauthAuthentication) authentication;
        return contentService.editContent(trecAuthentication.getList(), put)
                .map(ResponseObj::toEntity);
    }

    @DeleteMapping
    Mono<ResponseEntity<ResponseObj>> deleteContent(
            Authentication authentication,
            @RequestParam UUID contentId
    ){
        TrecauthAuthentication trecAuthentication = (TrecauthAuthentication) authentication;
        return contentService.deleteContent(trecAuthentication.getList(), contentId)
                .map(ResponseObj::toEntity);
    }

    @GetMapping("/id/{id}")
    Mono<ResponseEntity<Posting>> getPosting(
            Authentication authentication,
            @PathVariable UUID id){
        TrecauthAuthentication trecAuthentication = (TrecauthAuthentication) authentication;
        return contentService.getPosting(trecAuthentication.getList(), id)
                .map((ResponseObj obj) -> {
                    if(obj.getStatus() == 200)
                        return ResponseEntity.ok((Posting)obj.getData());
                    return new ResponseEntity<>(HttpStatus.valueOf(obj.getStatus()));
                });
    }

    @GetMapping("/byProfile/{profileId}")
    Mono<List<UUID>> getPostingsByProfile(
            Authentication authentication,
            @PathVariable UUID profileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        TrecauthAuthentication trecAuthentication = (TrecauthAuthentication) authentication;
        return contentService.getPostingList(
                trecAuthentication.getList(),
                profileId,
                null,
                page,
                size );
    }

    @GetMapping("/byModule/{moduleId}")
    Mono<List<UUID>> getPostingsByModule(
            Authentication authentication,
            @PathVariable UUID moduleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        TrecauthAuthentication trecAuthentication = (TrecauthAuthentication) authentication;
        return contentService.getPostingList(
                trecAuthentication.getList(),
                null,
                moduleId,
                page,
                size );
    }

    @GetMapping("/byParent/{parentId}")
    Mono<List<Posting>> getPostingsByParent(
            Authentication authentication,
            @PathVariable UUID parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        TrecauthAuthentication trecAuthentication = (TrecauthAuthentication) authentication;
        return contentService.getReplyList(
                trecAuthentication.getList(),
                parentId,
                page,
                size );
    }
}
