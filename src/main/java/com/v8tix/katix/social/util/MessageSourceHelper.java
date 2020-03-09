package com.v8tix.katix.social.util;

import org.springframework.context.MessageSource;

import java.util.Locale;

public interface MessageSourceHelper {
    //    USER
    String INVALID_EMAIL = "invalid.email";
    String USER_FIRST_NAME_SIZE = "user.first.name.size";
    String USER_LAST_NAME_SIZE = "user.last.name.size";
    String USER_GENDER_SIZE = "user.gender.size";
    String EMAIL_NOT_EMPTY = "user.email.not.empty";
    String FIRST_NAME_NOT_EMPTY = "user.first.name.not.empty";
    String LAST_NAME_NOT_EMPTY = "user.last.name.not.empty";
    String GENDER_NOT_EMPTY = "user.gender.not.empty";
    String PROFILE_PIC_NOT_EMPTY = "user.profile.pic.not.empty";
    //    COMMENT
    String COMMENT_NOT_EMPTY = "comment.not.empty";
    //    POST
    String CONTENT_NOT_EMPTY = "content.not.empty";
    //    REST
    String POST_ID_NOT_EQUALS_ERROR_MSG = "post.id.not.equals";
    String PAGE_ERROR_MSG = "page.not.valid";
    String PER_PAGE_ERROR_MSG = "per_page.not.valid";
    String UPSERT_INVALID_PARAMS_TITLE="invalid.post.comment.id.title";
    String UPSERT_INVALID_PARAMS_DETAIL="invalid.post.comment.id.detail";
    String UPSERT_INVALID_PARAMS_DEVELOPER="invalid.post.comment.id.developer.message";
    String UPSERT_INVALID_PARAMS_USER="invalid.post.comment.id.user.message";

    static String getMessage(MessageSource messageSource,
                             String errorMessage, Locale locale, Object... objects) {
        return messageSource.getMessage(errorMessage, objects, locale);
    }
}
