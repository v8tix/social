package com.v8tix.katix.social.repository;

import com.v8tix.katix.social.model.Post;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface PostReactiveRepository extends ReactiveMongoRepository<Post, String> {
    Flux<Post> findByOrderByCreatedAtEpochDesc();
}
