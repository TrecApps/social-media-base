package com.trecapps.sm.profile.services;

//import com.trecapps.sm.common.functionality.ObjectResponseException;
//import com.trecapps.sm.common.functionality.ProfileFunctionality;
import com.trecapps.sm.common.models.ObjectResponseException;
import com.trecapps.sm.common.models.ResponseObj;
//import com.trecapps.sm.common.notify.ISMProducer;
//import com.trecapps.sm.common.notify.ImageEndpointType;
//import com.trecapps.sm.common.notify.NotificationPost;
import com.trecapps.sm.profile.models.ConnectionEntry;
import com.trecapps.sm.profile.models.ConnectionLink;
import com.trecapps.sm.profile.models.Profile;
import com.trecapps.sm.profile.models.ProfileConnections;
import com.trecapps.sm.profile.repos.ConnectionRepo;
import com.trecapps.sm.profile.repos.ProfileRepoMongo;
import com.trecauth.common.model.Account;
import com.trecauth.common.model.AccountList;
import com.trecauth.common.model.AccountType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ConnectionsService {

    @Autowired
    ConnectionRepo connectionRepo;

    @Autowired
    ProfileRepoMongo profileRepo;

//    @Autowired(required = false)
//    ISMProducer notificationProducer;

    @Value("${trecapps.sm.name}")
    String app;

    ConnectionLink getLink(UUID follower, UUID followee){
        ConnectionLink link = new ConnectionLink();
        link.setFollower(follower);
        link.setFollowee(followee);
        return link;
    }

    record ConnectionProfile(Optional<ConnectionEntry> entry, Profile profile){}

    public Mono<ProfileConnections> getTwoWayConnection(UUID profile1, UUID profile2) {
        ConnectionLink link1 = getLink(profile1, profile2); // As Followee, since requester would be the follower
        ConnectionLink link2 = getLink(profile2, profile1); // As Follower, since requester would be the followee

        Mono<Optional<ConnectionEntry>> mono1 = connectionRepo.findById(link1)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty());
        Mono<Optional<ConnectionEntry>> mono2 = connectionRepo.findById(link2)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty());

        return mono1.zipWith(mono2)
                .map((Tuple2<Optional<ConnectionEntry>, Optional<ConnectionEntry>> tup) -> {
                    ProfileConnections ret = new ProfileConnections();
                    Optional<ConnectionEntry> ent1 = tup.getT1(); // As Followee
                    ent1.ifPresent(ret::setAsFollowee);
                    Optional<ConnectionEntry> ent2 = tup.getT2();
                    ent2.ifPresent(ret::setAsFollower);

                    return ret;
                } );
    }



    public Mono<ResponseObj> attemptFollow(
            AccountList list,
            UUID profile
    ) {
        return profileRepo.findById(profile)
                .defaultIfEmpty(new Profile())
                .doOnNext((Profile profile1) -> {
                    if(profile1.getId() == null)
                        throw new ObjectResponseException(HttpStatus.NOT_FOUND, "Profile not found");
                })

                .flatMap((Profile profile1) -> {

                    Account requestAccount = list.getCurrentAccount();
//                    UUID profileId = .getId();

                    Mono<Optional<ConnectionEntry>> existingEntry = null;

                    if(profile1.getProfileType() == AccountType.USER){
                        // ToDo - check Block

                        if(requestAccount.getType() == AccountType.USER){
                            existingEntry = this.getTwoWayConnection(requestAccount.getId(), profile)
                                    .map((ProfileConnections connections) -> {
                                        ConnectionEntry entry = connections.getAsFollower();
                                        if(entry == null)
                                            entry = connections.getAsFollowee();
                                        return Optional.ofNullable(entry);
                                    });
                        }
                    }
                    if(existingEntry == null){
                        // We are dealing with a one way connection
                        existingEntry = connectionRepo.findById(getLink(requestAccount.getId(), profile))
                                .map(Optional::of)
                                .defaultIfEmpty(Optional.empty());
                    }
                    return existingEntry.map((Optional<ConnectionEntry> e) -> {
                       return new  ConnectionProfile(e, profile1);
                    });
                })
                .flatMap((ConnectionProfile connectionProfile) -> {

                    Optional<ConnectionEntry> oEntry = connectionProfile.entry();

                    if(oEntry.isPresent())
                        throw new ObjectResponseException(HttpStatus.ALREADY_REPORTED, "Connection Already Exists!");
                    ConnectionEntry entry = new ConnectionEntry();
                    entry.setMade(OffsetDateTime.now());
                    entry.setOneWay(connectionProfile.profile.getProfileType() == AccountType.BRAND || list.getCurrentAccount().getType() == AccountType.BRAND);
                    entry.setId(getLink(
                            list.getCurrentAccount().getId(), profile
                    ));

                    return connectionRepo.save(entry)
//                            .doOnNext((ConnectionEntry e) -> {
//                                if(notificationProducer == null) return;
//
//                                Profile followee = connectionProfile.profile();
//
//                                NotificationPost notifyPost = new NotificationPost();
//                                notifyPost.setAppId(app);
//                                notifyPost.setCategory("Connect");
//                                String followeeId = followee.getId();
//                                if(followeeId.startsWith("Brand-")){
//                                    notifyPost.setBrandId(followeeId.substring(6));
//                                    notifyPost.setType(ImageEndpointType.BRAND_PROFILE);
//                                } else {
//                                    notifyPost.setUserId(followeeId.substring(5));
//                                    notifyPost.setType(ImageEndpointType.USER_PROFILE);
//
//                                }
//
//                                notifyPost.setImageId(ProfileFunctionality.getProfileId(user, brand));
//                                notifyPost.setRelevantId(ProfileFunctionality.getProfileId(user, brand));
//
//                                if(e.isOneWay()){
//                                    notifyPost.setMessage(String.format(
//                                            "%s is now following you", brand == null ? user.getDisplayName() : brand.getName()
//                                    ));
//                                } else {
//                                    notifyPost.setMessage(String.format(
//                                            "%s wants to connect with you", brand == null ? user.getDisplayName() : brand.getName()
//                                    ));
//                                }
//
//                                notificationProducer.sendNotification(notifyPost).doOnNext((Boolean worked) -> {
//                                    if(!worked)
//                                        log.error("Failed to notify {} of follow by {}", e.getId().getFollowee(), e.getId().getFollower());
//                                }).subscribe();
//
//                            })
                            .thenReturn(ResponseObj.getInstanceOK("Made"));
                })
                .onErrorResume(ObjectResponseException.class, (ObjectResponseException e) -> Mono.just(e.toResponseObj()));
    }


    public Mono<ResponseObj> approveRequest(AccountList approver, UUID profile) {

        return connectionRepo.findById(getLink(profile, approver.getCurrentAccount().getId()))
                .defaultIfEmpty(new ConnectionEntry())
                .flatMap((ConnectionEntry entry) -> {
                    if(entry.getId() == null)
                        throw new ObjectResponseException(HttpStatus.NOT_FOUND, "Connection Entry not found!");

                    if(entry.isOneWay())
                        throw new ObjectResponseException(HttpStatus.BAD_REQUEST, "This endpoint only applies to User Profiles");

                    if(entry.getAccepted() != null)
                        throw new ObjectResponseException(HttpStatus.ALREADY_REPORTED, "Connection already Accepted!");

                    entry.setAccepted(OffsetDateTime.now());
                    return connectionRepo.save(entry)
//                            .doOnNext((ConnectionEntry e) -> {
//
//                                if(notificationProducer == null) return;
//
//                                profileRepo.findById(e.getId().getFollowee())
//                                        .flatMap((Profile followee) -> {
//                                            NotificationPost notifyPost = new NotificationPost();
//                                            notifyPost.setAppId(app);
//                                            notifyPost.setCategory("Connect");
//
//                                            notifyPost.setUserId(profile.substring(5));
//                                            notifyPost.setType(ImageEndpointType.USER_PROFILE);
//
//                                            notifyPost.setImageId(followee.getId());
//                                            notifyPost.setRelevantId(followee.getId());
//
//                                            notifyPost.setMessage(String.format(
//                                                    "%s has accepted your connection request!", user.getDisplayName()
//                                            ));
//
//                                            return notificationProducer.sendNotification(notifyPost);
//                                        }).map((Boolean worked) -> {
//
//                                            return worked ? 0 : 1;
//                                        })
//                                        .defaultIfEmpty(2).doOnNext((Integer result) -> {
//                                            switch(result){
//                                                case 1:
//                                                    log.error("Failed to notify {} of connection approval by {}", e.getId().getFollower(), e.getId().getFollowee());
//                                                    break;
//                                                case 2:
//                                                    log.error("Failed to recover profile for {} in connection request approval", profile);
//                                            }
//                                        }).subscribe();
//                            })
                            .thenReturn(ResponseObj.getInstanceOK("Made"));
                })
                .onErrorResume(ObjectResponseException.class, (ObjectResponseException e) -> Mono.just(e.toResponseObj()));
    }

    public Mono<ResponseObj> unfollow(AccountList list, UUID profile){
        return this.getTwoWayConnection(
                list.getCurrentAccount().getId(),
                profile
        )
        .map((ProfileConnections con) -> Optional.ofNullable(con.getAsFollowee()))
        .flatMap((Optional<ConnectionEntry> oEntry) -> {
            if(oEntry.isEmpty())
                throw new ObjectResponseException(HttpStatus.NOT_FOUND, "Connection not found!");

            ConnectionEntry entry = oEntry.get();

            if(entry.isOneWay() && entry.getId().getFollower().equals(profile)){
                throw new ObjectResponseException(HttpStatus.CONFLICT, "This endpoint does not stop others from following you!");
            }

            return connectionRepo.delete(entry)
                    .thenReturn(ResponseObj.getInstanceOK("Removed"));
        })
        .onErrorResume(ObjectResponseException.class, (ObjectResponseException e) -> Mono.just(e.toResponseObj()));
    }

    public Mono<List<ConnectionEntry>> findMyConnections(AccountList list, int page, int size, boolean getFollowers) {

        return Mono.just(list.getCurrentAccount().getId())
                .flatMap((UUID profileId) -> {
                    Pageable pageable = PageRequest.of(page,size);
                    Flux<ConnectionEntry> entries = getFollowers ?
                            connectionRepo.findByFollowee(profileId, pageable) :
                            connectionRepo.findByFollower(profileId, pageable);
                    return entries.collectList();
                });



    }


}
