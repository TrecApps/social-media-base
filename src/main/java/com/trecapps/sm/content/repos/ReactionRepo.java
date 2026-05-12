package com.trecapps.sm.content.repos;

import com.trecapps.sm.content.models.ReactionEntry;
import com.trecapps.sm.content.models.ReactionId;
import com.trecapps.sm.content.models.ReactionTypeCount;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Repository
public interface ReactionRepo extends ReactiveCassandraRepository<ReactionEntry, ReactionId> {

//    @AllowFiltering
    @Query(value = "select * from reaction_entry where content_id = :contentId")
    Flux<ReactionEntry> findTypesByContentId(UUID contentId);

//    @Query(value = "select new com.trecapps.sm.content.models.ReactionTypeCount(re.type, count(re.type) )" +
//            " from reactionEntry re where content_id = :contentId group by re.type")
//    Flux<ReactionTypeCount> findCountByContentId(String contentId);

    default Mono<List<ReactionTypeCount>> findCountByContentId(UUID contentId) {
        return findTypesByContentId(contentId)
                .map((ReactionEntry entity) -> {
                    return entity.getReactionId() == null ? "" : entity.getReactionId().getType();
                })
                .filter((String val) -> !val.isEmpty())
                .collectList()
                .map((List<String> types) -> {
                    Map<String, Long> typeMap = new TreeMap<>();
                    for(String type: types){
                        typeMap.put(type, typeMap.getOrDefault(type, 0L) + 1);
                    }
                    return typeMap;
                })
                .map((Map<String, Long> typeMap) -> {
                    List<ReactionTypeCount> ret = new ArrayList<>(typeMap.size());
                    typeMap.forEach((String key, Long value) -> {
                        ret.add(new ReactionTypeCount(key, value));
                    });
                    return ret;
                });
    }

    @Query(value = "select * from reaction_entry where content_id = :contentId")
    Flux<ReactionEntry> findByContentId(UUID contentId, Pageable page);

    @Query(value = "select * from reaction_entry where content_id = :contentId and type = :type")
    Flux<ReactionEntry> findByContentIdAndType(UUID contentId, String type, Pageable page);

    @Query(value = "select * from reaction_entry where user_id = :userId")
    Flux<ReactionEntry> findByUserId(UUID userId, Pageable page);

    @Query(value = "select * from reaction_entry where content_id = :contentId and user_id = :userId")
    Mono<ReactionEntry> findByContentAndUserId(UUID contentId, UUID userId);

}
