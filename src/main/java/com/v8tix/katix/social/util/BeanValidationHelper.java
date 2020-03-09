package com.v8tix.katix.social.util;

import com.v8tix.katix.social.exception.BeanValidationException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Iterator;
import java.util.Set;

public interface BeanValidationHelper<T> {

    String DELIMITER_STRING = ",";
    char DELIMITER_CHAR = ',';

    default void validate(final Validator validator, final T type) throws BeanValidationException {
        final Set<ConstraintViolation<T>> constraints = validator.validate(type);
        final Iterator<ConstraintViolation<T>> iterator = constraints.iterator();
        if (constraints.size() != 0) {
            throw new BeanValidationException(getErrors(iterator));
        }
    }

    default String getErrors(final Iterator<ConstraintViolation<T>> iterator) {
        final StringBuilder errorMessages = new StringBuilder();
        while (iterator.hasNext()) {
            errorMessages.append(iterator.next().getMessage()).append(DELIMITER_STRING);
        }
        return StringHelper.removeLastChar(errorMessages.toString(), DELIMITER_CHAR);
    }
}
