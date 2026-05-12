package com.trecapps.sm.profile.repos;

import com.trecapps.sm.profile.models.MediaEventId;
import com.trecapps.sm.profile.models.SocialMediaEvent;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MediaEventRepo extends ReactiveCassandraRepository<SocialMediaEvent, MediaEventId> {

    @Query("select * from social_media_event where profile_id = :profileId and category = :category")
    Flux<SocialMediaEvent> getEventsByProfileAndCategory(String profileId, String category, Pageable page);

    default Flux<SocialMediaEvent> getEventsByProfileAndCategory(String profileId, String category, int page, int pageSize){
        Sort sort = Sort.by(Sort.Direction.DESC, "added");
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        return this.getEventsByProfileAndCategory(profileId, category, pageable);
    }
}
