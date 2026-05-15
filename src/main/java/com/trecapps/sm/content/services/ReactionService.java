package com.trecapps.sm.content.services;


import com.trecapps.sm.common.models.ObjectResponseException;
import com.trecapps.sm.common.models.ReactionStats;
import com.trecapps.sm.common.models.ResponseObj;
//import com.trecapps.sm.common.models.SocialMediaEvent;
import com.trecapps.sm.common.models.SocialMediaEventType;
import com.trecapps.sm.content.dto.ContentReactionEntry;
import com.trecapps.sm.content.dto.ProfileReactionEntry;
import com.trecapps.sm.content.dto.ReactionPosting;
import com.trecapps.sm.content.models.Posting;
import com.trecapps.sm.content.models.ReactionEntry;
import com.trecapps.sm.content.models.ReactionId;
import com.trecapps.sm.content.models.ReactionTypeCount;
//import com.trecapps.sm.content.pipeline.IEventInitiator;
import com.trecapps.sm.content.repos.ReactionRepo;
import com.trecauth.common.model.AccountList;
import com.trecauth.common.model.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ReactionService {

    @Autowired
    ReactionRepo reactionRepo;

    @Autowired
    ContentService contentService;

//    @Autowired(required = false)
//    IEventInitiator eventInitiator;

    @Value("#{'${trecapps.sm.reaction-types}'.split(',')}")
    List<String> reactionTypes;

    public Mono<ResponseObj> postReaction(
            AccountList list,
            UUID contentId,
            ReactionPosting reactionPosting

    ) {
        if(!reactionTypes.contains(reactionPosting.getReactType()))
            return Mono.just(ResponseObj.getInstance(HttpStatus.BAD_REQUEST, "Type is not supported on this platform!"));

        return contentService.getPosting(list, contentId)
                .map((ResponseObj responseObj) -> {
                    if(responseObj.getStatus() != 200)
                    {
                        // if status is not 200, then we don't have a posting and we need to error
                        throw new ObjectResponseException(HttpStatus.NOT_FOUND, "Content Not Found!");
                    }
                    return (Posting) responseObj.getData();
                })
                .flatMap((Posting posting) -> {

                    AtomicBoolean isNew = new AtomicBoolean(false);

                    return reactionRepo.findByContentAndUserId(contentId, list.getMainUserAccount().getId())
                            .doOnNext((ReactionEntry entity) -> {
                                reactionRepo.delete(entity);
                                isNew.set(true);
                            }).thenReturn(new ReactionEntry())
                            .doOnNext((ReactionEntry entity) -> {
                                entity.setVersion(posting.getContents().last().getVersion());
                                entity.setStale(false);
                                entity.setNew(isNew.get());
                                entity.setContentParent(posting.getParent());
                                entity.setModuleId(posting.getModuleId());
                            });


                })
                .flatMap((ReactionEntry entity) -> {
                    ReactionId reactionId = new ReactionId();
                    reactionId.setContentId(contentId);
                    reactionId.setUserAccountId(list.getMainUserAccount().getId());
                    reactionId.setType(reactionPosting.getReactType());

                    entity.setReactionId(reactionId);
                    entity.setMade(OffsetDateTime.now());
                    entity.setPrivate(reactionPosting.isMakePrivate());
                    entity.setAccountId(list.getCurrentAccount().getId());

                    return reactionRepo.save(entity);
                })
                .doOnNext((ReactionEntry entity) -> {
//                    if(eventInitiator == null || !entity.isNew()) {
//                        // we only care about new reactions (and if the event initiator is available)
//                        return;
//                    }
//                    SocialMediaEvent event = new SocialMediaEvent();
////                    event.setUserId(entity.getUserId());
//                    event.setResourceId(entity.getReactionId().getContentId());
//                    event.setModule(entity.getModuleId());
//                    String parent = entity.getContentParent();
//                    if(parent == null){
//                        event.setType(SocialMediaEventType.POST_REACTION);
//                    } else {
//                        event.setType(SocialMediaEventType.COMMENT_REACTION);
//                        event.setPostId(parent);
//                    }
//
//                    event.setProfile(ProfileFunctionality.getProfileId(entity.getReactionId().getUserId(), entity.getBrandId()));
//
//                    eventInitiator.sendEvent(event).subscribe();

                })
                .flatMap((ReactionEntry entity) -> {
                    return getReactionCount(list.getMainUserAccount(), contentId);
                });
    }


    public Mono<ResponseObj> getReactionCount(UserAccount user, UUID contentId) {
        return reactionRepo.findCountByContentId(contentId)
//                .collectList()
                .map((List<ReactionTypeCount> reactionList) -> {
                    // Use a List type that handles deletions easily
                    reactionList = new LinkedList<>(reactionList);
                    // Prepare for the data to return
                    ReactionStats stats = new ReactionStats();
                    HashMap<String, Long> map = new HashMap<>();
                    while(!reactionList.isEmpty()){
                        ReactionTypeCount curCount = reactionList.getFirst();
                        reactionList.removeFirst();
                        map.put(curCount.getType(), curCount.getCount());
                    }
                    stats.setReactions(map);
                    return stats;
                })
                .flatMap((ReactionStats stats) -> {
                    return reactionRepo.findByContentAndUserId(contentId, user.getId())
                            .map((ReactionEntry entity) -> {
                               stats.setYourReaction(entity.getReactionId().getType());
                               return stats;
                            });
                })
                .map((ReactionStats stats)-> {
                    ResponseObj ret = ResponseObj.getInstanceOK("Success");
                    ret.setReactStats(stats);
                    return ret;
                })
                .defaultIfEmpty(ResponseObj.getInstanceNOTFOUND("Content Not Found!"));
    }

    public Mono<ResponseObj> removeReaction(UserAccount user, UUID contentId) {
        return reactionRepo.findByContentAndUserId(contentId, user.getId())
                .doOnNext((ReactionEntry entity) -> {
                    reactionRepo.delete(entity);
                })
                .defaultIfEmpty(new ReactionEntry())
                .flatMap((ReactionEntry entity) -> {
                    return this.getReactionCount(user, contentId)
                            .doOnNext((ResponseObj obj) -> {
                                if(entity.getReactionId() == null) {
                                    obj.setStatus(HttpStatus.NOT_MODIFIED.value());
                                    obj.setMessage("You Have no reaction to this content!");
                                }
                            });
                });
    }

    public Mono<List<ContentReactionEntry>> getContentReactionListByContentId(
            AccountList list,
            UUID contentId,
            String type,
            int page,
            int size
    ) {
        return contentService.getPosting(list, contentId)
                .flatMap((ResponseObj obj) -> {
                    if(obj.getStatus() != 200)
                    {
                        // if status is not 200, then we don't have a posting and we need to error
                        throw new ObjectResponseException(HttpStatus.NOT_FOUND, "Content Not Found!");
                    }
                    Pageable pageable = PageRequest.of(page, size);

                    Flux<ReactionEntry> reactions = type == null ?
                            reactionRepo.findByContentId(contentId, pageable) :
                            reactionRepo.findByContentIdAndType(contentId, type, pageable);
                    return reactions.collectList();
                })
                .map((List<ReactionEntry> entities) -> {
                    return entities.stream()
                            .map((ReactionEntry entity) -> {
                                ContentReactionEntry reactionEntry = new ContentReactionEntry();
                                reactionEntry.setMade(entity.getMade());
                                reactionEntry.setType(entity.getReactionId().getType());


                                reactionEntry.setProfileId
                                        (list.getCurrentAccount().getId());
                                return reactionEntry;

                            }).toList();
                });

    }

    public Mono<List<ProfileReactionEntry>> getSelfReactions(UserAccount user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reactionRepo.findByUserId(user.getId(), pageable)
                .map((ReactionEntry entity) -> {

                    ProfileReactionEntry ret = new ProfileReactionEntry();

                    ret.setType(entity.getReactionId().getType());
                    ret.setPrivate(entity.isPrivate());
                    ret.setContentId(entity.getReactionId().getContentId());
                    ret.setBrandId(entity.getAccountId());
                    ret.setMade(entity.getMade().atOffset(ZoneOffset.UTC));
                    ret.setStale(entity.isStale());
                    ret.setVersion(entity.getVersion());
                    return ret;

                })
                .collectList();

    }


}
