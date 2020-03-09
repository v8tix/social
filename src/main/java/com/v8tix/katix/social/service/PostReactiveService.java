package com.v8tix.katix.social.service;

import com.v8tix.katix.social.exception.BeanValidationException;
import com.v8tix.katix.social.model.Comment;
import com.v8tix.katix.social.model.Post;
import com.v8tix.katix.social.repository.PostReactiveRepository;
import com.v8tix.katix.social.util.BeanValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.validation.Validator;
import java.util.List;

import static com.v8tix.katix.social.util.PaginationValidationHelper.validatePaginationParameters;
import static com.v8tix.katix.social.util.RestHelper.EMBED_COMMENTS;
import static com.v8tix.katix.social.util.RestHelper.createPageablePath;
import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

@Service
public class PostReactiveService implements BeanValidationHelper<Post> {

  private static final int COMMENTS_PAGE = 1;
  private static final int COMMENTS_PER_PAGE = 5;
  private final PostReactiveRepository postReactiveRepository;
  private final MessageSource messageSource;
  private final WebClient webClient;
  private final Validator validator;

  @Value("${rest.comments.domain_path}")
  private String domainCommentsPath;

  @Autowired
  public PostReactiveService(
      final PostReactiveRepository postReactiveRepository,
      final @Qualifier("JSR-380-Validator") Validator validator,
      final MessageSource messageSource,
      final WebClient webClient) {
    this.postReactiveRepository = postReactiveRepository;
    this.messageSource = messageSource;
    this.validator = validator;
    this.webClient = webClient;
  }

  public Mono<Long> getNumberOfPosts() {
    return postReactiveRepository.findAll().collectList().map(list -> (long) list.size());
  }

  public Flux<Post> findAllPageable(
      final long page, final long perPage, final String embedParameter) {
    validatePaginationParameters(page, perPage, messageSource, null);
    return embedComments(embedParameter, page, perPage);
  }

  private Flux<Post> getPageablePosts(long page, long perPage) {
    return postReactiveRepository
        .findByOrderByCreatedAtEpochDesc()
        .skip(perPage * (page - 1))
        .take(perPage);
  }

  private Flux<Post> embedComments(
      final String embedParameter, final long page, final long perPage) {
    if (embedParameter != null && embedParameter.equals(EMBED_COMMENTS)) {
      return getPageablePosts(page, perPage)
          .flatMap(
              post ->
                  Mono.just(post).flatMap(this::zipPostComments).subscribeOn(Schedulers.parallel()))
          .sort(comparing(Post::getCreatedAtEpoch).reversed());
    } else {
      return getPageablePosts(page, perPage).sort(comparing(Post::getCreatedAtEpoch).reversed());
    }
  }

  public Mono<Post> findByPostId(final String postId) {
    return postReactiveRepository.findById(postId).flatMap(this::zipPostComments);
  }

  public Mono<Post> save(final Post post) {
    validate(validator, post);
    return postReactiveRepository.save(post);
  }

  public Mono<Post> update(final String id, final Post post) throws BeanValidationException {
    validate(validator, post);
    return postReactiveRepository
        .findById(id)
        .flatMap(
            existingPost -> {
              existingPost.makeCopyOf(post);
              return postReactiveRepository.save(existingPost);
            });
  }

  public Mono<Post> delete(final String id) {
    return postReactiveRepository
        .findById(id)
        .map(
            user -> {
              postReactiveRepository.delete(user).subscribe();
              return user;
            });
  }

  private Mono<Post> zipPostComments(final Post post) {
    final Mono<Post> monoPost = Mono.just(post);
    final Mono<List<Comment>> monoComments = fetchCommentsByPostIdAsMono(webClient, post.getId());
    return monoPost
        .zipWith(monoComments)
        .map(
            tuple -> {
              final Post tPost = tuple.getT1();
              final List<Comment> comments = tuple.getT2();
              tPost.addComments(comments);
              return tPost;
            });
  }

  private Mono<List<Comment>> fetchCommentsByPostIdAsMono(
      final WebClient webClient, final String postId) {
    final String domainCommentsPathPostId = format(domainCommentsPath, postId);
    final String pageableCommentsPath =
        createPageablePath(domainCommentsPathPostId, COMMENTS_PAGE, COMMENTS_PER_PAGE);
    return webClient
        .get()
        .uri(pageableCommentsPath)
        .accept(APPLICATION_JSON_UTF8)
        .exchange()
        .flatMap(
            clientResponse -> {
              if (clientResponse.statusCode().equals(HttpStatus.OK)) {
                return clientResponse.bodyToFlux(Comment.class).collectList();
              } else {
                return Mono.empty();
              }
            });
  }
}
