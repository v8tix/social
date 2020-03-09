package com.v8tix.katix.social.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.RequestBodySpec;
import org.springframework.test.web.reactive.server.WebTestClient.RequestBodyUriSpec;
import org.springframework.test.web.reactive.server.WebTestClient.RequestHeadersSpec;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

public interface WebTestClientHelper<T> {

    Logger LOGGER = LoggerFactory.getLogger(WebTestClientHelper.class);

    default ResponseSpec upsertExchange(Class<T> typeClass, Mono<T> type, String uri, RequestBodyUriSpec operation) {
        final RequestBodySpec requestBodySpec = getUpsertRequest(uri, operation);
        return requestBodySpec.body(type, typeClass).exchange();
    }

    default ResponseSpec getExchange(final String uri, final WebTestClient webTestClient) {
        final RequestHeadersSpec requestBodySpec = getRequest(uri, webTestClient);
        return requestBodySpec.exchange();
    }

    default ResponseSpec deleteExchange(final String uri, final WebTestClient webTestClient) {
        final RequestHeadersSpec requestBodySpec = deleteRequest(uri, webTestClient);
        return requestBodySpec.exchange();
    }

    default RequestBodySpec getUpsertRequest(String uri, RequestBodyUriSpec operation) {
        return operation.uri(uri).contentType(APPLICATION_JSON_UTF8);
    }

    default RequestHeadersSpec<?> getRequest(final String uri, final WebTestClient webTestClient) {
        return webTestClient.get().uri(uri).accept(APPLICATION_JSON_UTF8);
    }

    default RequestHeadersSpec<?> deleteRequest(final String uri, final WebTestClient webTestClient) {
        return webTestClient.delete().uri(uri).accept(APPLICATION_JSON_UTF8);
    }
}
