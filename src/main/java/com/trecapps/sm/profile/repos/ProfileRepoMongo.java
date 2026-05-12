package com.trecapps.sm.profile.repos;

import com.trecapps.sm.profile.models.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface ProfileRepoMongo extends ReactiveMongoRepository<Profile, UUID> {

    @Query(value = "{\"$or\": [{ 'title' : { '$regex' : '?0', '$options' : 'i'}}, { 'aboutMeShort' : { '$regex' : '?0', '$options' : 'i'}}], " +
            "'blockerId': { \"$nin\": ?1 }}")
    Flux<Profile> findProfileByQuery(String query, List<UUID> blockers, Pageable page);
}
