package com.v8tix.katix.social.repository;

import com.v8tix.katix.social.model.Comment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface CommentReactiveRepository extends ReactiveMongoRepository<Comment, String> {
    Flux<Comment> findAllByPostIdOrderByCreatedAtEpochDesc(String postId);
}
