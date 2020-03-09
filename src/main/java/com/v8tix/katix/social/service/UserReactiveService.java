package com.v8tix.katix.social.service;

import com.v8tix.katix.social.exception.BeanValidationException;
import com.v8tix.katix.social.model.User;
import com.v8tix.katix.social.repository.UserReactiveRepository;
import com.v8tix.katix.social.util.BeanValidationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Validator;

import static com.v8tix.katix.social.util.PaginationValidationHelper.validatePaginationParameters;
import static java.util.Comparator.comparing;

@Service
public class UserReactiveService implements BeanValidationHelper<User> {

  private final UserReactiveRepository userReactiveRepository;
  private final MessageSource messageSource;
  private final Validator validator;

  @Autowired
  public UserReactiveService(
      final UserReactiveRepository userReactiveRepository,
      final @Qualifier("JSR-380-Validator") Validator validator,
      final MessageSource messageSource) {
    this.userReactiveRepository = userReactiveRepository;
    this.messageSource = messageSource;
    this.validator = validator;
  }

  public Flux<User> findAllPageable(final long page, final long perPage) {
    validatePaginationParameters(page, perPage, messageSource, null);
    return getPageablePosts(page, perPage).sort(comparing(User::getCreatedAtEpoch).reversed());
  }

  private Flux<User> getPageablePosts(long page, long perPage) {
    return userReactiveRepository
        .findByOrderByCreatedAtEpochDesc()
        .skip(perPage * (page - 1))
        .take(perPage);
  }

  public Mono<Long> getNumberOfUsers() {
    return userReactiveRepository.findAll().collectList().map(list -> (long) list.size());
  }

  public Mono<User> findById(final String id) {
    return userReactiveRepository.findById(id);
  }

  public Mono<User> save(final User user) throws BeanValidationException {
    validate(validator, user);
    return userReactiveRepository.save(user);
  }

  public Mono<User> update(final String id, final User user) throws BeanValidationException {
    validate(validator, user);
    return userReactiveRepository
        .findById(id)
        .flatMap(
            existingUser -> {
              existingUser.makeCopyOf(user);
              return userReactiveRepository.save(existingUser);
            });
  }

  public Mono<User> delete(final String id) {
    return userReactiveRepository
        .findById(id)
        .map(
            user -> {
              userReactiveRepository.delete(user).subscribe();
              return user;
            });
  }
}
