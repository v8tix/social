package com.v8tix.katix.social.controller;

import com.v8tix.katix.social.exception.BeanValidationException;
import com.v8tix.katix.social.model.User;
import com.v8tix.katix.social.service.UserReactiveService;
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
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path = "/katix/social/api/v1/users")
@CrossOrigin(
    origins = "*",
    methods = {HEAD, OPTIONS, GET, POST, PUT, DELETE},
    allowedHeaders = "*",
    exposedHeaders = "Link, Location, Host",
    maxAge = 3600)
public class UserController {

  private final UserReactiveService userReactiveService;

  @Value("${rest.users.path}")
  private String usersPath;

  @Value("${rest.users.domain_path}")
  private String domainUsersPath;

  @Autowired
  public UserController(final UserReactiveService userReactiveService) {
    this.userReactiveService = userReactiveService;
  }

  @GetMapping("/")
  public Mono<ResponseEntity<Object>> getAll(
      final @RequestParam long page, final @RequestParam("per_page") long perPage) {
    final Mono<Long> numberOfElementsMono = userReactiveService.getNumberOfUsers();
    final Mono<List<User>> listUsersMono =
        userReactiveService.findAllPageable(page, perPage).collectList();
    return numberOfElementsMono
        .zipWith(listUsersMono)
        .map(
            tuple -> {
              final Long numberOfElements = tuple.getT1();
              final List<User> tUsers = tuple.getT2();
              final String webLinks = getAllWebLinks(domainUsersPath, page, perPage);
              return createPageableLinkHeaderResponse(
                  domainUsersPath, page, perPage, numberOfElements, tUsers, webLinks);
            });
  }

  @GetMapping("/{userId}")
  public Mono<ResponseEntity<Object>> get(final @PathVariable(value = "userId") String userId) {
    return userReactiveService
        .findById(userId)
        .map(
            user -> {
              final String webLinks = commonWebLinks(domainUsersPath, user);
              return createSelfLinkHeaderResponse(
                  domainUsersPath, user.getId(), user, HttpStatus.OK, webLinks);
            })
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PostMapping
  public Mono<ResponseEntity<Object>> save(final @RequestBody User user)
      throws BeanValidationException {
    return userReactiveService
        .save(user)
        .map(
            updatedUser -> {
              final String webLinks = commonWebLinks(domainUsersPath, updatedUser);
              return createLocationAndLinkHeadersResponse(
                  domainUsersPath, updatedUser.getId(), webLinks);
            })
        .defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
  }

  @PutMapping("/{userId}")
  public Mono<ResponseEntity<Object>> update(
      final @PathVariable(value = "userId") String userId, final @RequestBody User user)
      throws BeanValidationException {
    return userReactiveService
        .update(userId, user)
        .map(
            updatedUser -> {
              final String webLinks = commonWebLinks(domainUsersPath, updatedUser);
              return createSelfLinkHeaderResponse(
                  domainUsersPath, updatedUser.getId(), updatedUser, HttpStatus.OK, webLinks);
            })
        .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @DeleteMapping("/{userId}")
  public Mono<ResponseEntity<Object>> delete(final @PathVariable String userId) {
    return userReactiveService
        .delete(userId)
        .map(user -> new ResponseEntity<>(HttpStatus.NO_CONTENT))
        .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  private static String getAllWebLinks(
      final String domainUsersPath, final long page, final long perPage) {
    final String selfWebLink =
        createPageableWebLink(domainUsersPath, page, perPage, LinkRelation.SELF);
    return concatStrings(COMA, selfWebLink);
  }

  private static String commonWebLinks(final String domainUsersPath, final User user) {
    final String domainUsersPathUserId = concatStrings(EMPTY_STRING, domainUsersPath, user.getId());
    final String usersWebLink = createWebLink(domainUsersPathUserId, LinkRelation.SELF);
    return concatStrings(COMA, usersWebLink);
  }
}
