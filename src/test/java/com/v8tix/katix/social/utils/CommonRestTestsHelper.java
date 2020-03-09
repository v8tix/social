package com.v8tix.katix.social.utils;

import com.v8tix.katix.social.exception.ErrorDetail;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.BiConsumer;

import static com.v8tix.katix.social.util.StringHelper.EMPTY_STRING;
import static com.v8tix.katix.social.util.StringHelper.concatStrings;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

public interface CommonRestTestsHelper<T> extends WebTestClientHelper<T> {

    String LOCATION_HEADER = "Location";
    String LINK_HEADER = "Link";

    default void getById(final WebTestClient webTestClient,
                         final String baseURI,
                         final String id,
                         final T existingType,
                         final Class<T> typeClass,
                         BiConsumer<T, T> typeConsumer) {
        final String uri = concatStrings(EMPTY_STRING, baseURI, id);
        getExchange(uri, webTestClient)
                .expectStatus().isOk()
                .expectHeader().exists(LINK_HEADER)
                .expectHeader().contentType(APPLICATION_JSON_UTF8)
                .expectBody(typeClass)
                .consumeWith(result ->
                        {
                            final T resultType = result.getResponseBody();
                            assert resultType != null;
                            typeConsumer.accept(existingType, resultType);
                        }
                );
    }

    default void getByFakeId(final WebTestClient webTestClient,
                             final String baseURI,
                             final String id) {
        final String uri = concatStrings(EMPTY_STRING, baseURI, id);
        getExchange(uri, webTestClient).expectStatus().isNotFound();
    }

    default void deleteById(final WebTestClient webTestClient,
                            final String baseURI,
                            final String id) {
        final String uri = concatStrings(EMPTY_STRING, baseURI, id);
        deleteExchange(uri, webTestClient).expectStatus().isNoContent();
    }

    default void deleteByFakeId(final WebTestClient webTestClient,
                                final String baseURI,
                                final String id) {
        final String uri = concatStrings(EMPTY_STRING, baseURI, id);
        deleteExchange(uri, webTestClient).expectStatus().isNotFound();
    }

    default void getAll(final WebTestClient webTestClient,
                        final String baseURI,
                        final List<T> existingTypes,
                        final Class<T> typeClass,
                        BiConsumer<List<T>, List<T>> typeConsumer) {
        getExchange(baseURI, webTestClient)
                .expectStatus().isOk()
                .expectHeader().exists(LINK_HEADER)
                .expectHeader().contentType(APPLICATION_JSON_UTF8)
                .expectBodyList(typeClass)
                .consumeWith(result ->
                        {
                            final List<T> resultType = result.getResponseBody();
                            assert resultType != null;
                            typeConsumer.accept(existingTypes, resultType);
                        }
                );
    }

    default void postInvalidType(final WebTestClient webTestClient,
                                 final String baseURI,
                                 final Class<T> typeClass,
                                 final T type,
                                 final String expectedMessage,
                                 final BiConsumer<ErrorDetail, String> errorDetailConsumer) {
        Mono<T> monoType = Mono.just(type);
        upsertExchange(typeClass, monoType, baseURI, webTestClient.post())
                .expectStatus().isBadRequest()
                .expectBody(ErrorDetail.class)
                .consumeWith(errorDetailEntityExchangeResult ->
                        {
                            final ErrorDetail errorDetail = errorDetailEntityExchangeResult.getResponseBody();
                            assert errorDetail != null;
                            errorDetailConsumer.accept(errorDetail, expectedMessage);
                        }
                );
    }

    default void postSingleError(final WebTestClient webTestClient,
                                 final String baseURI,
                                 final Class<T> typeClass,
                                 final T type,
                                 final String expectedMessage) {
        Mono<T> monoType = Mono.just(type);
        upsertExchange(typeClass, monoType, baseURI, webTestClient.post())
                .expectStatus().isBadRequest()
                .expectBody(ErrorDetail.class)
                .consumeWith(errorDetailEntityExchangeResult ->
                        {
                            final ErrorDetail errorDetail = errorDetailEntityExchangeResult.getResponseBody();
                            assert errorDetail != null;
                            assertEquals(errorDetail.getUserMessages().get(0), expectedMessage);
                        }
                );
    }

    default void postValidType(final WebTestClient webTestClient,
                               final String baseURI,
                               final Class<T> typeClass,
                               final T type) {
        Mono<T> monoType = Mono.just(type);
        upsertExchange(typeClass, monoType, baseURI, webTestClient.post())
                .expectStatus().isCreated()
                .expectHeader().exists(LINK_HEADER)
                .expectHeader().exists(LOCATION_HEADER)
                .expectBody(typeClass);
    }

    default void putIdValidType(final WebTestClient webTestClient,
                                final String baseURI,
                                final String id,
                                final Class<T> typeClass,
                                final T type) {
        Mono<T> monoType = Mono.just(type);
        final String uri = concatStrings(EMPTY_STRING, baseURI, id);
        upsertExchange(typeClass, monoType, uri, webTestClient.put())
                .expectStatus().isOk()
                .expectHeader().exists(LINK_HEADER);
    }

    default void putIdInvalidType(
            final WebTestClient webTestClient,
            final String baseURI,
            final String id,
            final Class<T> typeClass,
            final T type,
            final String expectedMessage,
            final BiConsumer<ErrorDetail, String> errorDetailConsumer) {
        Mono<T> monoType = Mono.just(type);
        final String uri = concatStrings(EMPTY_STRING, baseURI, id);
        upsertExchange(typeClass, monoType, uri, webTestClient.put())
                .expectStatus().isBadRequest()
                .expectBody(ErrorDetail.class)
                .consumeWith(errorDetailEntityExchangeResult ->
                        {
                            final ErrorDetail errorDetail = errorDetailEntityExchangeResult.getResponseBody();
                            assert errorDetail != null;
                            LOGGER.info(errorDetail.toString());
                            errorDetailConsumer.accept(errorDetail, expectedMessage);
                        }
                );
    }

    default void putInvalidIdValidType(final WebTestClient webTestClient,
                                       final String baseURI,
                                       final String id,
                                       final Class<T> typeClass,
                                       final T type) {
        Mono<T> monoType = Mono.just(type);
        final String uri = concatStrings(EMPTY_STRING, baseURI, id);
        upsertExchange(typeClass, monoType, uri, webTestClient.put())
                .expectStatus().isNotFound();
    }

    default void putSingleError(final WebTestClient webTestClient,
                                final String baseURI,
                                final String id,
                                final Class<T> typeClass,
                                final T type,
                                final String expectedMessage,
                                final BiConsumer<ErrorDetail, String> errorDetailConsumer) {
        Mono<T> monoType = Mono.just(type);
        final String uri = concatStrings(EMPTY_STRING, baseURI, id);
        upsertExchange(typeClass, monoType, uri, webTestClient.put())
                .expectStatus().isNotFound()
                .expectBody(ErrorDetail.class)
                .consumeWith(errorDetailEntityExchangeResult ->
                        {
                            final ErrorDetail errorDetail = errorDetailEntityExchangeResult.getResponseBody();
                            assert errorDetail != null;
                            LOGGER.info(errorDetail.toString());
                            errorDetailConsumer.accept(errorDetail, expectedMessage);
                        }
                );
    }
}
