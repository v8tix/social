package com.v8tix.katix.social.util;

import com.v8tix.katix.social.exception.BeanValidationException;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static com.v8tix.katix.social.util.MessageSourceHelper.PAGE_ERROR_MSG;
import static com.v8tix.katix.social.util.MessageSourceHelper.PER_PAGE_ERROR_MSG;
import static com.v8tix.katix.social.util.MessageSourceHelper.getMessage;

public interface PaginationValidationHelper {

    static void validatePaginationParameters(final long page,
                                             final long perPage,
                                             final MessageSource messageSource,
                                             final Locale locale) throws BeanValidationException {
        if (page < 1) {
            final String pageErrorMessage = getMessage(messageSource, PAGE_ERROR_MSG, locale);
            throw new BeanValidationException(String.format(pageErrorMessage, page));
        }
        if (perPage < 1) {
            final String perPageErrorMessage = getMessage(messageSource, PER_PAGE_ERROR_MSG, locale);
            throw new BeanValidationException(String.format(perPageErrorMessage, perPage));
        }
    }
}
