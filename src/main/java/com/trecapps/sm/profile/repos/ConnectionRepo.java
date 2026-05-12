package com.trecapps.sm.profile.repos;

import com.trecapps.sm.profile.models.ConnectionEntry;
import com.trecapps.sm.profile.models.ConnectionLink;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ConnectionRepo extends ReactiveCassandraRepository<ConnectionEntry, ConnectionLink> {

    @Query("select * from connection_entry where follower = :follower ALLOW FILTERING")
    @AllowFiltering
    Flux<ConnectionEntry> findByFollower(String follower, Pageable page);

    @Query("select * from connection_entry where followee = :followee ALLOW FILTERING")
    @AllowFiltering
    Flux<ConnectionEntry> findByFollowee(String followee, Pageable page);
}
