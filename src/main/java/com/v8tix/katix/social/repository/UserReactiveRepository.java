package com.v8tix.katix.social.repository;

import com.v8tix.katix.social.model.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface UserReactiveRepository extends ReactiveMongoRepository<User, String> {
    Flux<User> findByOrderByCreatedAtEpochDesc();
}
