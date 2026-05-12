package com.trecapps.sm.content.repos;

import com.trecapps.sm.content.models.Posting;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface ContentRepo extends ReactiveMongoRepository<Posting, UUID> {
    @Query("{\"$or\": [{'ownerId': ?0}, {'posterId': ?0}], 'parent' :{ $exists: false }," +
            "'blockerAccount': { \"$nin\": ?1 }," +
            "'ownerBlocker': { \"$nin\": ?1 }}")
    Flux<Posting> getContentByProfileId(@Param("profileId") UUID profileId, List<UUID> blockers, Pageable page);

    @Query("{'moduleId': ?0," +
            "'blockerAccount': { \"$nin\": ?1 }," +
            "'ownerBlocker': { \"$nin\": ?1 }}")
    Flux<Posting> getContentByModuleId(@Param("moduleId") UUID moduleId, List<UUID> blockers, Pageable page);

    @Query("{'parent': ?0," +
            "'blockerAccount': { \"$nin\": ?1 }," +
            "'ownerBlocker': { \"$nin\": ?1 }}")
    Flux<Posting> getContentByParent(@Param("parentId") UUID parentId, List<UUID> blockers, Pageable page);

    @Query("{'moduleId': ?0, $or: ['posterId': ?1, 'ownerId': ?1], 'parent': null," +
            "'blockerAccount': { \"$nin\": ?2 }," +
            "'ownerBlocker': { \"$nin\": ?2 }}")
    Flux<Posting> getContentByModuleAndProfileId(@Param("moduleId") UUID moduleId, @Param("profileId") UUID profileID, List<UUID> blockers, Pageable page);
}
