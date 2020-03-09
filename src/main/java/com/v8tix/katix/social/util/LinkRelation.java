package com.v8tix.katix.social.util;

import org.springframework.http.MediaType;


public enum LinkRelation {

    SELF("self", "self", MediaType.APPLICATION_JSON_UTF8.toString()),
    FIRST("first", "first page", MediaType.APPLICATION_JSON_UTF8.toString()),
    LAST("last", "last page", MediaType.APPLICATION_JSON_UTF8.toString()),
    NEXT("next", "next page", MediaType.APPLICATION_JSON_UTF8.toString()),
    PREV("prev", "previous page", MediaType.APPLICATION_JSON_UTF8.toString()),
    COMMENTS("comments", "fetch comments", MediaType.APPLICATION_JSON_UTF8.toString()),
    USER("user", "fetch user", MediaType.APPLICATION_JSON_UTF8.toString());

    private final String rel;
    private final String title;
    private final String type;

    LinkRelation(final String rel, final String title, final String type) {
        this.rel = rel;
        this.title = title;
        this.type = type;
    }

    public String getRel() {
        return rel;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }
}
