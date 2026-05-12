package com.trecapps.sm.content.repos;

import com.trecapps.sm.content.models.Posting;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ContentRepo extends ReactiveMongoRepository<Posting, String> {
    @Query("{\"$or\": [{'profileOwner': ?0}, {'profilePoster': ?0}], 'parent' :{ $exists: false }}")
    Flux<Posting> getContentByProfileId(@Param("profileId") String profileId, Pageable page);

    @Query("{'moduleId': ?0}")
    Flux<Posting> getContentByModuleId(@Param("moduleId") String moduleId, Pageable page);

    @Query("{'parent': ?0}")
    Flux<Posting> getContentByParent(@Param("parentId") String parentId, Pageable page);

    @Query("{'moduleId': ?0, $or: ['profilePoster': ?1, 'profileOwner': ?1], 'parent': null}")
    Flux<Posting> getContentByModuleAndProfileId(@Param("moduleId") String moduleId, @Param("profileId") String profileID, Pageable page);
}
