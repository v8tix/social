package com.v8tix.katix.social.controller;

import com.v8tix.katix.social.exception.BeanValidationException;
import com.v8tix.katix.social.model.Post;
import com.v8tix.katix.social.service.PostReactiveService;
import com.v8tix.katix.social.util.LinkRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.v8tix.katix.social.util.RestHelper.*;
import static com.v8tix.katix.social.util.StringHelper.*;
import static java.lang.String.format;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path = "/katix/social/api/v1/posts")
@CrossOrigin(
    origins = "*",
    methods = {HEAD, OPTIONS, GET, POST, PUT, DELETE},
    allowedHeaders = "*",
    exposedHeaders = "Link, Location, Host",
    maxAge = 3600)
public class PostController {

  private static final int FIRST_PAGE = 1;
  private static final int PER_PAGE = 5;
  private final PostReactiveService postReactiveService;

  @Value("${rest.posts.path}")
  private String postsPath;

  @Value("${rest.comments.path}")
  private String commentsPath;

  @Value("${rest.users.path}")
  private String usersPath;

  @Value("${rest.posts.domain_path}")
  private String domainPostsPath;

  @Value("${rest.comments.domain_path}")
  private String domainCommentsPath;

  @Value("${rest.users.domain_path}")
  private String domainUsersPath;

  @Autowired
  public PostController(final PostReactiveService postReactiveService) {
    this.postReactiveService = postReactiveService;
  }

  @GetMapping("/")
  public Mono<ResponseEntity<Object>> getAll(
      final @RequestParam long page,
      final @RequestParam("per_page") long perPage,
      final @RequestParam(value = "embed", required = false) String embed) {
    final Mono<Long> numberOfElementsMono = postReactiveService.getNumberOfPosts();
    final Mono<List<Post>> listPostsMono =
        postReactiveService.findAllPageable(page, perPage, embed).collectList();
    return numberOfElementsMono
        .zipWith(listPostsMono)
        .map(
            tuple -> {
              final Long numberOfElements = tuple.getT1();
              final List<Post> tPosts = tuple.getT2();
              final String webLinks = getAllWebLinks(domainPostsPath, page, perPage);
              return createPageableLinkHeaderResponse(
                  domainPostsPath, page, perPage, numberOfElements, tPosts, webLinks);
            });
  }

  @GetMapping("/{postId}")
  public Mono<ResponseEntity<Object>> get(final @PathVariable(value = "postId") String postId) {
    return postReactiveService
        .findByPostId(postId)
        .map(
            post -> {
              final String webLinks = commonWebLinks(post, domainCommentsPath, domainUsersPath);
              return createSelfLinkHeaderResponse(
                  domainPostsPath, post.getId(), post, HttpStatus.OK, webLinks);
            })
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PostMapping("/")
  public Mono<ResponseEntity<Object>> save(final @RequestBody Post post)
      throws BeanValidationException {
    return postReactiveService
        .save(post)
        .map(
            updatedPost -> {
              final String webLinks =
                  commonWebLinks(updatedPost, domainCommentsPath, domainUsersPath);
              return createLocationAndLinkHeadersResponse(
                  domainPostsPath, updatedPost.getId(), webLinks);
            })
        .defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
  }

  @PutMapping("/{postId}")
  public Mono<ResponseEntity<Object>> update(
      final @PathVariable(value = "postId") String postId, final @RequestBody Post post)
      throws BeanValidationException {
    return postReactiveService
        .update(postId, post)
        .map(
            updatedPost -> {
              final String webLinks =
                  commonWebLinks(updatedPost, domainCommentsPath, domainUsersPath);
              return createSelfLinkHeaderResponse(
                  domainPostsPath, updatedPost.getId(), updatedPost, HttpStatus.OK, webLinks);
            })
        .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @DeleteMapping("/{postId}")
  public Mono<ResponseEntity<Object>> delete(final @PathVariable String postId) {
    return postReactiveService
        .delete(postId)
        .map(post -> new ResponseEntity<>(HttpStatus.NO_CONTENT))
        .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  private static String getAllWebLinks(
      final String domainPostsPath, final long page, final long perPage) {
    final String selfWebLink =
        createPageableWebLink(domainPostsPath, page, perPage, LinkRelation.SELF);
    return concatStrings(COMA, selfWebLink);
  }

  private static String commonWebLinks(
      final Post post, final String domainCommentsPath, final String domainUsersPath) {
    final String domainCommentsPathPostId = format(domainCommentsPath, post.getId());
    final String commentsWebLink =
        createPageableWebLink(
            domainCommentsPathPostId, FIRST_PAGE, PER_PAGE, LinkRelation.COMMENTS);
    final String domainUsersPathUserId =
        concatStrings(EMPTY_STRING, domainUsersPath, post.getUserId());
    final String userWebLink = createWebLink(domainUsersPathUserId, LinkRelation.USER);
    return concatStrings(commentsWebLink, userWebLink);
  }
}
