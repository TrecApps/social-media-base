package com.trecapps.sm.content.repos;

import com.trecapps.sm.content.models.Posting;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ContentRepo extends ReactiveMongoRepository<Posting, UUID> {
    @Query("{\"$or\": [{'ownerId': ?0}, {'posterId': ?0}], 'parent' :{ $exists: false }}")
    Flux<Posting> getContentByProfileId(@Param("profileId") UUID profileId, Pageable page);

    @Query("{'moduleId': ?0}")
    Flux<Posting> getContentByModuleId(@Param("moduleId") UUID moduleId, Pageable page);

    @Query("{'parent': ?0}")
    Flux<Posting> getContentByParent(@Param("parentId") UUID parentId, Pageable page);

    @Query("{'moduleId': ?0, $or: ['posterId': ?1, 'ownerId': ?1], 'parent': null}")
    Flux<Posting> getContentByModuleAndProfileId(@Param("moduleId") UUID moduleId, @Param("profileId") UUID profileID, Pageable page);
}
