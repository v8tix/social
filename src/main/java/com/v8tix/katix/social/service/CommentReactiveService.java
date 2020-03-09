package com.v8tix.katix.social.service;

import com.v8tix.katix.social.exception.BeanValidationException;
import com.v8tix.katix.social.model.Comment;
import com.v8tix.katix.social.model.Post;
import com.v8tix.katix.social.model.User;
import com.v8tix.katix.social.repository.CommentReactiveRepository;
import com.v8tix.katix.social.util.BeanValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import javax.validation.Validator;

import static com.v8tix.katix.social.util.MessageSourceHelper.POST_ID_NOT_EQUALS_ERROR_MSG;
import static com.v8tix.katix.social.util.MessageSourceHelper.getMessage;
import static com.v8tix.katix.social.util.PaginationValidationHelper.validatePaginationParameters;
import static com.v8tix.katix.social.util.RestHelper.getByIdRequest;

@Service
public class CommentReactiveService implements BeanValidationHelper<Comment> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommentReactiveService.class);
  private final CommentReactiveRepository commentReactiveRepository;
  private final MessageSource messageSource;
  private final Validator validator;
  private final WebClient webClient;

  @Value("${rest.posts.domain_path}")
  private String domainPostsPath;

  @Value("${rest.users.domain_path}")
  private String domainUsersPath;

  @Autowired
  public CommentReactiveService(
      final CommentReactiveRepository commentReactiveRepository,
      final @Qualifier("JSR-380-Validator") Validator validator,
      final MessageSource messageSource,
      final WebClient webClient) {
    this.commentReactiveRepository = commentReactiveRepository;
    this.messageSource = messageSource;
    this.validator = validator;
    this.webClient = webClient;
  }

  private Mono<Comment> findCommentById(final String commentId) {
    return commentReactiveRepository.findById(commentId);
  }

  public Mono<Comment> findById(final String postId, final String commentId) {
    return validatePostAndComment(postId, commentId).map(objects -> (Comment) objects.getT2());
  }

  public Flux<Comment> findAllByPostId(final long page, final long perPage, final String postId) {
    validatePaginationParameters(page, perPage, messageSource, null);
    return findAllCommentsByPostIdOrderByCreatedAtEpochDesc(postId)
        .skip(perPage * (page - 1))
        .take(perPage);
  }

  public Mono<Long> getNumberOfComments(final String postId) {
    return findAllCommentsByPostIdOrderByCreatedAtEpochDesc(postId)
        .collectList()
        .map(list -> (long) list.size());
  }

  private Flux<Comment> findAllCommentsByPostIdOrderByCreatedAtEpochDesc(String postId) {
    return commentReactiveRepository.findAllByPostIdOrderByCreatedAtEpochDesc(postId);
  }

  private Mono<Comment> save(final Comment comment) throws BeanValidationException {
    return commentReactiveRepository.save(comment);
  }

  public Mono<Comment> save(final String postId, final Comment comment) {
    return validatePostAndUser(postId, comment)
        .flatMap(
            objects -> {
              final Post post = (Post) objects.getT1();
              final User user = (User) objects.getT2();
              LOGGER.info("save Post id: " + post.getId());
              LOGGER.info("save User id: " + user.getId());
              return save(comment);
            });
  }

  public Mono<Comment> update(final String postId, final String commentId, final Comment comment)
      throws BeanValidationException {
    final Mono<Tuple2<Object, Object>> tuplePostUserMono = validatePostAndUser(postId, comment);
    final Mono<Comment> commentMono = commentReactiveRepository.findById(commentId);
    return tuplePostUserMono
        .zipWith(commentMono)
        .flatMap(
            objects -> {
              final Comment existingComment = objects.getT2();
              existingComment.makeCopyOf(comment);
              return commentReactiveRepository.save(existingComment);
            });
  }

  public Mono<Comment> delete(final String id) {
    return commentReactiveRepository
        .findById(id)
        .map(
            user -> {
              commentReactiveRepository.delete(user).subscribe();
              return user;
            });
  }

  private Mono<Tuple2<Object, Object>> validatePostAndUser(
      final String postId, final Comment comment) {
    validate(validator, comment);
    if (!postId.equals(comment.getPostId())) {
      throw new BeanValidationException(
          getMessage(messageSource, POST_ID_NOT_EQUALS_ERROR_MSG, null));
    }
    final Mono<Object> postMono = fetchPostById(postId);
    final Mono<Object> userMono = fetchUserById(comment.getUserId());
    return postMono.zipWith(userMono);
  }

  private Mono<Tuple2<Object, Object>> validatePostAndComment(
      final String postId, final String commentId) {
    final Mono<Object> postMono = fetchPostById(postId);
    final Mono<Comment> commentMono = findCommentById(commentId);
    return postMono.zipWith(commentMono);
  }

  private Mono<Object> fetchPostById(final String postId) {
    return getByIdRequest(webClient, domainPostsPath, postId, Post.class);
  }

  private Mono<Object> fetchUserById(final String userId) {
    return getByIdRequest(webClient, domainUsersPath, userId, User.class);
  }
}
