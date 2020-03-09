package com.v8tix.katix.social.controller;

import com.v8tix.katix.social.exception.BeanValidationException;
import com.v8tix.katix.social.exception.ErrorDetail;
import com.v8tix.katix.social.model.Comment;
import com.v8tix.katix.social.service.CommentReactiveService;
import com.v8tix.katix.social.util.LinkRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
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
@RequestMapping(path = "/katix/social/api/v1/posts/{postId}/comments")
@CrossOrigin(
    origins = "*",
    methods = {HEAD, OPTIONS, GET, POST, PUT, DELETE},
    allowedHeaders = "*",
    exposedHeaders = "Link, Location, Host",
    maxAge = 3600)
public class CommentController {

  private final CommentReactiveService commentReactiveService;
  private final MessageSource messageSource;

  @Value("${rest.comments.path}")
  private String commentsPath;

  @Value("${rest.users.path}")
  private String usersPath;

  @Value("${rest.comments.domain_path}")
  private String domainCommentsPath;

  @Value("${rest.users.domain_path}")
  private String domainUsersPath;

  @Autowired
  public CommentController(
      final CommentReactiveService commentReactiveService, final MessageSource messageSource) {
    this.commentReactiveService = commentReactiveService;
    this.messageSource = messageSource;
  }

  @GetMapping("/")
  public Mono<ResponseEntity<Object>> getAll(
      final @RequestParam int page,
      final @RequestParam("per_page") long perPage,
      final @PathVariable("postId") String postId) {
    final Mono<Long> numberOfElementsMono = commentReactiveService.getNumberOfComments(postId);
    final Mono<List<Comment>> listCommentsMono =
        commentReactiveService.findAllByPostId(page, perPage, postId).collectList();
    return numberOfElementsMono
        .zipWith(listCommentsMono)
        .map(
            tuple -> {
              final Long numberOfElements = tuple.getT1();
              final List<Comment> tComments = tuple.getT2();
              final String domainCommentsPathWithPostId = format(domainCommentsPath, postId);
              final String webLinks = getAllWebLinks(domainCommentsPathWithPostId, page, perPage);
              final String selfPath = format(domainCommentsPath, postId);
              return createPageableLinkHeaderResponse(
                  selfPath, page, perPage, numberOfElements, tComments, webLinks);
            });
  }

  @GetMapping("/{commentId}")
  public Mono<ResponseEntity<Object>> get(
      final @PathVariable("postId") String postId,
      final @PathVariable("commentId") String commentId) {
    return commentReactiveService
        .findById(postId, commentId)
        .map(comment -> getAndUpdateResponseEntity(postId, comment))
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PostMapping("/")
  public Mono<ResponseEntity<Object>> save(
      final @PathVariable("postId") String postId, final @RequestBody Comment comment)
      throws BeanValidationException {
    final ErrorDetail errorDetail = getUpsertErrorDetail(messageSource, null);
    return commentReactiveService
        .save(postId, comment)
        .map(
            savedComment -> {
              final String webLinks = commonWebLinks(domainUsersPath, savedComment);
              final String selfPath = format(domainCommentsPath, postId);
              return createLocationAndLinkHeadersResponse(selfPath, savedComment.getId(), webLinks);
            })
        .defaultIfEmpty(new ResponseEntity<>(errorDetail, HttpStatus.BAD_REQUEST));
  }

  @PutMapping("/{commentId}")
  public Mono<ResponseEntity<Object>> update(
      final @PathVariable("postId") String postId,
      final @PathVariable(value = "commentId") String commentId,
      final @RequestBody Comment comment)
      throws BeanValidationException {
    final ErrorDetail errorDetail = getUpsertErrorDetail(messageSource, null);
    return commentReactiveService
        .update(postId, commentId, comment)
        .map(updatedComment -> getAndUpdateResponseEntity(postId, comment))
        .defaultIfEmpty(new ResponseEntity<>(errorDetail, HttpStatus.NOT_FOUND));
  }

  @DeleteMapping("/{commentId}")
  public Mono<ResponseEntity<Object>> delete(final @PathVariable String commentId) {
    return commentReactiveService
        .delete(commentId)
        .map(comment -> new ResponseEntity<>(HttpStatus.NO_CONTENT))
        .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  private ResponseEntity<Object> getAndUpdateResponseEntity(String postId, Comment comment) {
    final String webLinks = commonWebLinks(domainUsersPath, comment);
    final String selfPath = format(domainCommentsPath, postId);
    return createSelfLinkHeaderResponse(selfPath, comment.getId(), comment, HttpStatus.OK, webLinks);
  }

  private static String getAllWebLinks(
      final String domainCommentsPath, final long page, final long perPage) {
    final String selfWebLink = createPageableWebLink(domainCommentsPath, page, perPage, LinkRelation.SELF);
    return concatStrings(COMA, selfWebLink);
  }

  private static String commonWebLinks(final String domainUsersPath, final Comment comment) {
    final String domainUsersPathUserId = concatStrings(EMPTY_STRING, domainUsersPath, comment.getUserId());
    final String usersWebLink = createWebLink(domainUsersPathUserId, LinkRelation.USER);
    return concatStrings(COMA, usersWebLink);
  }
}
