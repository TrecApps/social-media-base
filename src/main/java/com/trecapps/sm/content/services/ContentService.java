package com.trecapps.sm.content.services;

import com.trecapps.auth.common.models.TcBrands;
import com.trecapps.auth.common.models.TcUser;
import com.trecapps.sm.common.functionality.ObjectResponseException;
import com.trecapps.sm.common.functionality.ProfileFunctionality;
import com.trecapps.sm.common.models.ResponseObj;
import com.trecapps.sm.common.models.SocialMediaEvent;
import com.trecapps.sm.common.models.SocialMediaEventType;
import com.trecapps.sm.content.dto.ContentPost;
import com.trecapps.sm.content.dto.ContentPut;
import com.trecapps.sm.content.models.Posting;
import com.trecapps.sm.content.pipeline.IEventInitiator;
import com.trecapps.sm.content.repos.ContentRepo;
import com.trecapps.sm.profile.models.Profile;
import com.trecapps.sm.profile.repos.ProfileRepoMongo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ContentService {

    @Autowired
    ContentRepo contentRepo;

    @Autowired
    ProfileRepoMongo profileRepo;

    @Autowired(required = false)
    IEventInitiator eventInitiator;

    @Value("${trecapps.sm.enable-cross-profile-posting:true}")
    boolean allowCrossProfilePosting;

    public Mono<ResponseObj> postContent(TcUser user, TcBrands brand, ContentPost post){

        return Mono.just(ProfileFunctionality.getProfileId(user, brand))
                .doOnNext((String profileId) -> {
                    // ToDo - check block table and make sure post isn't crossing any lines
                })
                .doOnNext((String profileId) -> {
                    // ToDo - handle modules (does module exist, is user allowed to post there
                })
                .flatMap((String profileId) -> {
                    String parent = post.getParentId();
                    Posting newPost = new Posting();
                    OffsetDateTime now = OffsetDateTime.now();

                    newPost.setId(UUID.randomUUID().toString());
                    newPost.setMade(now);
                    newPost.setUserId(user.getId());
                    newPost.setProfilePoster(profileId);

                    newPost.appendContent(post.getContent());

                    if(parent == null){
                        return Mono.just(newPost);
                    }
                    return contentRepo.findById(parent)
                            .defaultIfEmpty(new Posting())
                            .map((Posting parentPosting) -> {
                                if(parentPosting.getId() == null)
                                    throw new ObjectResponseException(HttpStatus.NOT_FOUND, "Parent content not found");
                                newPost.setParents(parentPosting);
                                newPost.setParent(parent);
                                return newPost;
                            });
                })
                .flatMap((Posting newPost) -> {
                    if(post.getProfileId() == null){
                        newPost.setProfileOwner(newPost.getProfilePoster());
                        return Mono.just(newPost);
                    }
                    return profileRepo.findById(post.getProfileId())
                            .defaultIfEmpty(new Profile())
                            .map((Profile profile) -> {
                                if(profile.getId() == null)
                                    throw new ObjectResponseException(HttpStatus.NOT_FOUND, "Parent content not found");
                                newPost.setProfileOwner(profile.getId());
                                return newPost;
                            });
                })
                .flatMap(contentRepo::save)
                .doOnNext((Posting newPost) -> {
                    // ToDo - add Broadcast mechanism
                    if(eventInitiator == null) return;

                    if(newPost.getParents().size() > 1) return;

                    SocialMediaEvent event = new SocialMediaEvent();
                    event.setUserId(newPost.getUserId());
                    event.setResourceId(newPost.getId());
                    event.setModule(newPost.getModuleId());
                    event.setProfile(newPost.getProfilePoster());
                    if(newPost.isPost()){
                        event.setType(SocialMediaEventType.POST);
                    } else {
                        event.setType(SocialMediaEventType.COMMENT);
                        event.setPostId(newPost.getParent());
                    }

                    eventInitiator.sendEvent(event).subscribe();
                })
                .map((Posting newPost) -> {
                    ResponseObj ret = ResponseObj.getInstanceOK("Posted!", newPost.getId());
                    ret.setData(newPost);
                    return ret;
                })
                .onErrorResume(ObjectResponseException.class, (ObjectResponseException e) -> Mono.just(e.toResponseObj()))
                .onErrorResume((Throwable thrown)-> {
                    log.error("Error processing Content Posting", thrown);
                    return Mono.just(ResponseObj.getInstance(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error!"));
                });
    }

    public Mono<ResponseObj> editContent(TcUser user, TcBrands brand, ContentPut put){
        return Mono.just(ProfileFunctionality.getProfileId(user, brand))
                .flatMap((String profileId) -> {
                    return contentRepo.findById(put.getContentId())
                            .defaultIfEmpty(new Posting())
                            .doOnNext((Posting post) -> {
                                if(post.getId() == null)
                                    throw new ObjectResponseException(HttpStatus.NOT_FOUND, "Content not found");
                                if(!post.getProfilePoster().equals(profileId) || !user.getId().equals(post.getUserId()))
                                    throw new ObjectResponseException(HttpStatus.FORBIDDEN, "This is not your Content!");
                            });
                })
                .flatMap((Posting post) -> {
                    post.appendContent(put.getContent());

                    // ToDo - decide if editing content removes delete mar or if this request shoudl be blocked in an earlier step
                    post.setDeleteSet(null);


                    return contentRepo.save(post);
                })
                .doOnNext((Posting post) -> {
                    // ToDo - render stale every reaction that reacted to the previous version of this post

                    if(eventInitiator == null) return;

                    SocialMediaEvent event = new SocialMediaEvent();
                    event.setUserId(post.getUserId());
                    event.setResourceId(post.getId());
                    event.setModule(post.getModuleId());
                    event.setPostId(post.getParent()); // Since posts/comments are in the same database, it does
                        // not matter if the parent is a post or comment

                    event.setType(SocialMediaEventType.CONTENT_EDIT);
                    event.setProfile(post.getProfilePoster());

                    eventInitiator.sendEvent(event).subscribe();
                })
                .map((Posting p) -> {
                    ResponseObj obj = ResponseObj.getInstanceOK("Success");
                    obj.setData(p);
                    return obj;
                })
                .onErrorResume(ObjectResponseException.class, (ObjectResponseException e) -> Mono.just(e.toResponseObj()))
                .onErrorResume((Throwable thrown)-> {
                    log.error("Error processing Content Update", thrown);
                    return Mono.just(ResponseObj.getInstance(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error!"));
                });
    }

    public Mono<ResponseObj> deleteContent(TcUser user, TcBrands brand, String contentId){
        return Mono.just(ProfileFunctionality.getProfileId(user, brand))
        .flatMap((String profileId) -> {
            return contentRepo.findById(contentId)
                    .defaultIfEmpty(new Posting())
                    .doOnNext((Posting post) -> {
                        if(post.getId() == null)
                            throw new ObjectResponseException(HttpStatus.NOT_FOUND, "Content not found");
                        if(!post.getProfilePoster().equals(profileId) || !user.getId().equals(post.getUserId()))
                            throw new ObjectResponseException(HttpStatus.FORBIDDEN, "This is not your Content!");
                    });
        })
                .flatMap((Posting post) -> {
                    if(post.getDeleteSet() != null)
                        throw new ObjectResponseException(HttpStatus.CONFLICT, "Delete has already been scheduled for this post");
                    post.setDeleteSet(OffsetDateTime.now());
                    return contentRepo.save(post);
                })

                .thenReturn(ResponseObj.getInstanceOK("Success"))
                .onErrorResume(ObjectResponseException.class, (ObjectResponseException e) -> Mono.just(e.toResponseObj()))
                .onErrorResume((Throwable thrown)-> {
                    log.error("Error processing Content Deletion", thrown);
                    return Mono.just(ResponseObj.getInstance(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error!"));
                });
    }


    public Mono<ResponseObj> getPosting(TcUser user, TcBrands brand, String contentId)
    {
        return Mono.just(ProfileFunctionality.getProfileId(user, brand))
                .flatMap((String profileId) -> {
                    return contentRepo.findById(contentId)
                            .defaultIfEmpty(new Posting())
                            .doOnNext((Posting post) -> {
                                if(post.getId() == null)
                                    throw new ObjectResponseException(HttpStatus.NOT_FOUND, "Content not found");
                            });
                })
                .doOnNext((Posting post) -> {
                    // ToDo - Need to make sure that whoever posted this is not blocking the requester

                })
                .map(ResponseObj::getDataInstance)
                .onErrorResume(ObjectResponseException.class, (ObjectResponseException e) -> Mono.just(e.toResponseObj()))
                .onErrorResume((Throwable thrown)-> {
                    log.error("Error retrieving Content", thrown);
                    return Mono.just(ResponseObj.getInstance(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error!"));
                });
    }

    public Mono<List<String>> getPostingList(TcUser user, TcBrands brands, String profileId, String moduleId, int page, int size){
        // ToDo - add mechanism so that any poster who is blocking this person does not have their content included in results

        Flux<Posting> ret;
        Pageable pageSize = PageRequest.of(page,size);

        if(profileId == null){
            ret =
                    moduleId == null ?
                            Flux.fromIterable(new ArrayList<Posting>()) :
                            contentRepo.getContentByModuleId(moduleId,pageSize);

        } else {
            ret = moduleId == null ?
                    contentRepo.getContentByProfileId(profileId, pageSize) :
                    contentRepo.getContentByModuleAndProfileId(moduleId, profileId,pageSize);
        }

        return ret.map(Posting::getId).collectList();
    }

    public Mono<List<Posting>> getReplyList(TcUser user, TcBrands brands, String parentId, int page, int size){
        // ToDo - add mechanism so that any poster who is blocking this person does not have their content included in results

        Pageable pageSize = PageRequest.of(page,size);



        return this.contentRepo.getContentByParent(parentId, pageSize).collectList();
    }


}
