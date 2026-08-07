package com.trecapps.sm.profile.services;

import com.trecapps.sm.common.models.ResponseObj;
import com.trecapps.sm.profile.dto.PostFilterRequest;
import com.trecapps.sm.profile.models.ProfileFilterList;
import com.trecapps.sm.profile.models.SocialMediaEvent;
import com.trecapps.sm.profile.repos.MediaEventRepo;
import com.trecapps.sm.profile.repos.ProfileFilterRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class HomePageManagementService {

    @Autowired
    ProfileFilterRepo profileFilterRepo;

    @Autowired
    MediaEventRepo mediaEventRepo;

    public Mono<ProfileFilterList> getFilterList(UUID profileId){
        return profileFilterRepo.findById(profileId)
                .defaultIfEmpty(new ProfileFilterList())
                .doOnNext((ProfileFilterList list) -> {
                    list.setId(profileId);
                });
    }

    public Mono<ResponseObj> addOrUpdateFilter(UUID profileId, PostFilterRequest request){
        return getFilterList(profileId)
                .doOnNext((ProfileFilterList list) -> {
                    list.updateFilter(request);
                }).flatMap((ProfileFilterList list) -> profileFilterRepo.save(list))
                .thenReturn(ResponseObj.getInstanceOK("Filters Updated"));
    }

    public Mono<ResponseObj> removeFilter(UUID profileId, PostFilterRequest request){
        return getFilterList(profileId)
                .doOnNext((ProfileFilterList list) -> {
                    list.removeFilter(request);
                }).flatMap((ProfileFilterList list) -> profileFilterRepo.save(list))
                .thenReturn(ResponseObj.getInstanceOK("Filters Updated"));
    }



    public Mono<List<SocialMediaEvent>> getHomePageEvents(UUID profileId, String category, int page, int size){
        return mediaEventRepo.getEventsByProfileAndCategory(profileId, category, page, size).collectList();
    }





}
