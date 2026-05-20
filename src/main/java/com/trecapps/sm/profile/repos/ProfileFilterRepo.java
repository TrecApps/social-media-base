package com.trecapps.sm.profile.repos;

import com.trecapps.sm.profile.models.ProfileFilterList;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.UUID;

public interface ProfileFilterRepo extends ReactiveMongoRepository<ProfileFilterList, UUID> {
}
