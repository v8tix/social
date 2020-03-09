package com.v8tix.katix.social.util;

import com.v8tix.katix.social.exception.ErrorDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Locale;

import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

public interface RestHelper {

    Logger LOGGER = LoggerFactory.getLogger(RestHelper.class);
    String RFC_5988_LINK_FORMAT = "<%s>;rel=\"%s\";title=\"%s\";type=\"%s\"";
    String LOCATION_EXCEPTION = "Location exception: %s";
    String UNKOWN_EXCEPTION = "Unknown Host exception: %s";
    String PAGEABLE_PART = "?page=%d&per_page=%d";
    String EMBED_COMMENTS = "comments";
    String EMBED_PART = "&embed=";
    String LINK_HEADER = "Link";
    String HOST_HEADER = "Host";
    int FIRST_PAGE = 1;

    static ResponseEntity<Object> createLocationAndLinkHeadersResponse(final String path,
                                                                       final String id,
                                                                       final String... webLinks) {
        final HttpHeaders responseHeaders = new HttpHeaders();
        URI newUserPath = null;
        try {
            final String selfWebLink = createSelfPath(path, id);
            newUserPath = new URI(selfWebLink);
        } catch (URISyntaxException e) {
            LOGGER.info(format(LOCATION_EXCEPTION, e.getMessage()));
        }
        responseHeaders.setLocation(newUserPath);
        responseHeaders.set(HOST_HEADER, getHostName());
        final String concatenatedLinks = StringHelper.concatStrings(StringHelper.COMA, null, webLinks);
        responseHeaders.set(LINK_HEADER, concatenatedLinks);
        return new ResponseEntity<>(null, responseHeaders, HttpStatus.CREATED);
    }

    static ResponseEntity<Object> LinkHeaderResponse(final Object body,
                                                     final HttpStatus httpStatus,
                                                     final String... WebLinks) {
        final String concatenatedLinks = StringHelper.concatStrings(StringHelper.COMA, null, WebLinks);
        return buildLinkResponseEntity(body, httpStatus, concatenatedLinks);
    }

    static String createPageableWebLink(final String path,
                                        final long page,
                                        final long perPage,
                                        final LinkRelation linkRelation) {
        final String pageablePath = createPageablePath(path, page, perPage);
        return createWebLink(pageablePath, linkRelation);
    }

    static String createWebLink(final String path,
                                final LinkRelation linkRelation) {
        final String rel = linkRelation.getRel();
        final String title = linkRelation.getTitle();
        final String type = linkRelation.getType();
        return format(RFC_5988_LINK_FORMAT, path, rel, title, type);
    }

    static String createFirstWebLink(final String path, final long perPage) {
        return createPageableWebLink(path, FIRST_PAGE, perPage, LinkRelation.FIRST);
    }

    static String createLastWebLink(final String path,
                                    final long numberOfElements,
                                    final long perPage) {
        final long numberOfPages = getNumberOfPages(numberOfElements, perPage);
        return createPageableWebLink(path, numberOfPages, perPage, LinkRelation.LAST);
    }

    static String createNextWebLink(final String path,
                                    final long numberOfElements,
                                    final long actualPage,
                                    final long perPage) {
        String webLink = StringHelper.EMPTY_STRING;
        if (hasNextPage(numberOfElements, actualPage, perPage)) {
            final long nextPage = actualPage + 1;
            webLink = createPageableWebLink(path, nextPage, perPage, LinkRelation.NEXT);
        }
        return webLink;
    }

    static String createPreviousWebLink(final String path,
                                        final long numberOfElements,
                                        final long actualPage,
                                        final long perPage) {
        String webLink = StringHelper.EMPTY_STRING;
        if (hasPreviousPage(numberOfElements, actualPage, perPage)) {
            final long previousPage = actualPage - 1;
            webLink = createPageableWebLink(path, previousPage, perPage, LinkRelation.PREV);
        }
        return webLink;
    }

    static ResponseEntity<Object> createSelfLinkHeaderResponse(final String path,
                                                               final String id,
                                                               final Object body,
                                                               final HttpStatus httpStatus,
                                                               final String... webLinks) {
        final String selfPath = createSelfPath(path, id);
        final String selfPathWebLink = createWebLink(selfPath, LinkRelation.SELF);
        final String selfAndWebLinks = StringHelper.concatStrings(StringHelper.COMA, selfPathWebLink, webLinks);
        return buildLinkResponseEntity(body, httpStatus, selfAndWebLinks);
    }

    static ResponseEntity<Object> buildLinkResponseEntity(Object body, HttpStatus httpStatus, String selfAndWebLinks) {
        final HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HOST_HEADER, getHostName());
        responseHeaders.set(LINK_HEADER, selfAndWebLinks);
        return new ResponseEntity<>(body, responseHeaders, httpStatus);
    }

    static ResponseEntity<Object> createPageableLinkHeaderResponse(final String path,
                                                                   final long page,
                                                                   final long perPage,
                                                                   final long numberOfElements,
                                                                   final Object body,
                                                                   final String... webLinks) {
        if (numberOfElements > 0) {
            final String pageableWebLinks = createPageableWebLinks(path,
                    page, perPage, numberOfElements);
            final String selfAndPageableAndWebLinks = StringHelper.concatStrings(StringHelper.COMA, pageableWebLinks, webLinks);
            return LinkHeaderResponse(body, HttpStatus.OK, selfAndPageableAndWebLinks);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    static String createPageableWebLinks(final String path,
                                         final long page,
                                         final long perPage,
                                         final long numberOfElements) {
        final String firstWebLink = createFirstWebLink(path, perPage);
        final String lastWebLink = createLastWebLink(path, numberOfElements, perPage);
        final String nextWebLink = createNextWebLink(path, numberOfElements, page, perPage);
        final String previousWebLink = createPreviousWebLink(path, numberOfElements, page, perPage);
        return StringHelper.concatStrings(StringHelper.COMA, firstWebLink, lastWebLink, nextWebLink, previousWebLink);
    }

    static String createSelfPath(final String path, final String id) {
        return StringHelper.concatStrings(StringHelper.EMPTY_STRING, path, id);
    }

    static String createPageablePath(final String path,
                                     final long page,
                                     final long perPage) {
        final String pageablePathPart = format(PAGEABLE_PART, page, perPage);
        return StringHelper.concatStrings(StringHelper.EMPTY_STRING, path, pageablePathPart);
    }

    static String createEmbedPath(final String path,
                                  final String embedParameter) {
        return StringHelper.concatStrings(StringHelper.EMPTY_STRING, path, EMBED_PART, embedParameter);
    }

    static boolean hasNextPage(final long numberOfElements, final long actualPage, final long perPage) {
        final long numberOfPages = getNumberOfPages(numberOfElements, perPage);
        final long nextPage = actualPage + 1;
        return numberOfPages > 0 && actualPage >= 1 && actualPage <= numberOfPages && nextPage <= numberOfPages;
    }

    static boolean hasPreviousPage(final long numberOfElements, final long actualPage, final long perPage) {
        final long numberOfPages = getNumberOfPages(numberOfElements, perPage);
        final long previousPage = actualPage - 1;
        return numberOfPages > 0 && actualPage > 1 && actualPage <= numberOfPages && previousPage >= 1;
    }

    static long getNumberOfPages(final long numberOfElements, final long perPage) {
        final double numberOfPages = numberOfElements / MathHelper.longToDouble(perPage);
        return MathHelper.doubleToLong(numberOfPages);
    }

    @SuppressWarnings("unchecked")
    static Mono<Object> getByIdRequest(final WebClient webClient,
                                       final String path,
                                       final String id,
                                       final Class clazz) {
        final String pathWithId = StringHelper.concatStrings(StringHelper.EMPTY_STRING, path, id);
        return webClient.get().uri(pathWithId)
                .accept(APPLICATION_JSON_UTF8)
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().equals(HttpStatus.OK)) {
                        return clientResponse.bodyToMono(clazz);
                    } else {
                        return Mono.empty();
                    }
                });
    }

    static ErrorDetail getUpsertErrorDetail(MessageSource messageSource, Locale locale) {
        final String title = MessageSourceHelper.getMessage(messageSource, MessageSourceHelper.UPSERT_INVALID_PARAMS_TITLE, locale);
        final String detail = MessageSourceHelper.getMessage(messageSource, MessageSourceHelper.UPSERT_INVALID_PARAMS_DETAIL, locale);
        final String developerMessage = MessageSourceHelper.getMessage(messageSource, MessageSourceHelper.UPSERT_INVALID_PARAMS_DEVELOPER, locale);
        final String userMessage = MessageSourceHelper.getMessage(messageSource, MessageSourceHelper.UPSERT_INVALID_PARAMS_USER, locale);
        OffsetDateTime offsetDateTime = DateHelper.getOffsetDateTime();
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setDetail(detail);
        errorDetail.setTitle(title);
        errorDetail.setDeveloperMessage(developerMessage);
        errorDetail.setStatus(HttpStatus.BAD_REQUEST.value());
        errorDetail.setTimeStamp(DateHelper.isoDate(offsetDateTime));
        errorDetail.setUserMessages(Collections.singletonList(userMessage));
        return errorDetail;
    }

    static String getHostName() {
        final InetAddress localhost;
        String hostname = null;
        try {
            localhost = InetAddress.getLocalHost();
            hostname = localhost.getHostName();
        } catch (UnknownHostException e) {
            LOGGER.info(format(UNKOWN_EXCEPTION, e.getMessage()));
        }
        return hostname;
    }
}
