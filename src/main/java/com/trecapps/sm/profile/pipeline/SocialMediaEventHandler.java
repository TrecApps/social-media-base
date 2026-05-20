package com.trecapps.sm.profile.pipeline;

import com.trecapps.sm.common.models.SocialMediaEvent;
import com.trecapps.sm.common.models.SocialMediaEventType;
import com.trecapps.sm.profile.models.ConnectionEntry;
import com.trecapps.sm.profile.models.MediaEventId;
import com.trecapps.sm.profile.models.ProfileFilterList;
import com.trecapps.sm.profile.repos.ConnectionRepo;
import com.trecapps.sm.profile.repos.MediaEventRepo;
import com.trecapps.sm.profile.repos.ProfileFilterRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class SocialMediaEventHandler implements IEventHandler {

    @Autowired
    ProfileFilterRepo profileFilterRepo;

    @Autowired
    MediaEventRepo mediaEventRepo;

    @Autowired
    ConnectionRepo connectionRepo;

    int pageSize;

    @Autowired
    SocialMediaEventHandler(
            ProfileFilterRepo profileFilterRepo,
            MediaEventRepo mediaEventRepo,
            ConnectionRepo connectionRepo,
            @Value("${sm.broadcast.page-size:100}") int pageSize,
            IEventConsumer consumer
            ){
        this.profileFilterRepo = profileFilterRepo;
        this.mediaEventRepo = mediaEventRepo;
        this.connectionRepo = connectionRepo;
        this.pageSize = pageSize;

        consumer.initialize(this);
    }


    @Override
    public Mono<Boolean> processEvent(SocialMediaEvent var1) {

        Mono<Boolean> byFollowers = processFollowingEvent(var1, 0);
        Mono<Boolean> byOtherConnections = processFollowerEvent(var1, 0);
        return byFollowers.zipWith(byOtherConnections).map((Tuple2<Boolean, Boolean> results) -> results.getT1() || results.getT2());
    }


    Mono<Boolean> processFollowerEvent(SocialMediaEvent var1, int page) {

        Flux<ConnectionEntry> currentConnections = Mono.just(PageRequest.of(page, pageSize))
                .flatMapMany((Pageable p) -> connectionRepo.findByFollower(var1.getProfile(), p));

        return currentConnections
                .filter((ConnectionEntry entry) -> !entry.isOneWay())
                .flatMap((ConnectionEntry entry) -> {
                    return profileFilterRepo.findById(entry.getId().getFollowee()).defaultIfEmpty(new ProfileFilterList()).doOnNext((ProfileFilterList list) -> {
                        if(list.getId() == null) list.setId(entry.getId().getFollowee());
                    });
                })
                .flatMap((ProfileFilterList list) -> {
                    double prop = list.getProbability(var1.getProfile(), var1.getType());

                    double result = Math.random();
                    if(result < prop)
                    {
                        com.trecapps.sm.profile.models.SocialMediaEvent event = new com.trecapps.sm.profile.models.SocialMediaEvent();
                        event.setType(var1.getType());
                        event.setContentId(var1.getResourceId());
                        if(var1.getType().equals(SocialMediaEventType.COMMENT) || var1.getType().equals(SocialMediaEventType.COMMENT_REACTION)){
                            event.setParentContentId(var1.getPostId());
                        }
                        event.setOtherProfile(var1.getProfile());

                        MediaEventId eventId = new MediaEventId();
                        eventId.setAdded(Instant.now());
                        eventId.setCategory("Following");
                        eventId.setProfile(list.getId());
                        eventId.setRandomId(UUID.randomUUID().toString());

                        event.setId(eventId);

                        return mediaEventRepo.save(event).thenReturn(true);
                    }
                    return Mono.just(true);

                })
                .doOnError((Throwable e) -> {
                    log.error("Error detected processing {}", var1, e);
                })
                .collectList().flatMap((List<Boolean> booleans) -> {
                    Boolean ret = booleans.isEmpty() || booleans.contains(Boolean.TRUE);
                    if(booleans.size() < this.pageSize)
                        return Mono.just(ret);

                    return processFollowerEvent(var1, page + 1).map((Boolean b) -> b || ret);
                });


    }

    Mono<Boolean> processFollowingEvent(SocialMediaEvent var1, int page) {

        Flux<ConnectionEntry> currentConnections = Mono.just(PageRequest.of(page, pageSize))
                .flatMapMany((Pageable p) -> connectionRepo.findByFollowee(var1.getProfile(), p));

        return currentConnections.flatMap((ConnectionEntry entry) -> {
                    return profileFilterRepo.findById(entry.getId().getFollowee()).defaultIfEmpty(new ProfileFilterList()).doOnNext((ProfileFilterList list) -> {
                        if(list.getId() == null) list.setId(entry.getId().getFollowee());
                    });
                })
                .flatMap((ProfileFilterList list) -> {
                    double prop = list.getProbability(var1.getProfile(), var1.getType());

                    double result = Math.random();
                    if(result < prop)
                    {
                        com.trecapps.sm.profile.models.SocialMediaEvent event = new com.trecapps.sm.profile.models.SocialMediaEvent();
                        event.setType(var1.getType());
                        event.setContentId(var1.getResourceId());
                        if(var1.getType().equals(SocialMediaEventType.COMMENT) || var1.getType().equals(SocialMediaEventType.COMMENT_REACTION)){
                            event.setParentContentId(var1.getPostId());
                        }
                        event.setOtherProfile(var1.getProfile());

                        MediaEventId eventId = new MediaEventId();
                        eventId.setAdded(Instant.now());
                        eventId.setCategory("Following");
                        eventId.setProfile(list.getId());
                        eventId.setRandomId(UUID.randomUUID().toString());

                        event.setId(eventId);

                        return mediaEventRepo.save(event).thenReturn(true);
                    }
                    return Mono.just(true);

                })
                .doOnError((Throwable e) -> {
                    log.error("Error detected processing {}", var1, e);
                })
                .collectList().flatMap((List<Boolean> booleans) -> {
                    Boolean ret = booleans.isEmpty() || booleans.contains(Boolean.TRUE);
                    if(booleans.size() < this.pageSize)
                        return Mono.just(ret);

                    return processFollowingEvent(var1, page + 1).map((Boolean b) -> b || ret);
                });


    }
}
