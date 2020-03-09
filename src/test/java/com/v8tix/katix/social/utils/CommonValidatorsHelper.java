package com.v8tix.katix.social.utils;

import com.v8tix.katix.social.exception.ErrorDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;

public interface CommonValidatorsHelper<T> {

    Logger LOGGER = LoggerFactory.getLogger(CommonValidatorsHelper.class);
    int FIRST_INDEX = 0;

    default void validateExpectedMessage(ErrorDetail errorDetail, String expectedMessage) {
        final List<String> userMessages = errorDetail.getUserMessages();
        assertEquals(1, userMessages.size());
        assertEquals(expectedMessage, userMessages.get(FIRST_INDEX));
    }

    default void validateErrorsArraySize(ErrorDetail errorDetail, String value) {
        Integer integer = Integer.parseInt(value);
        final List<String> userMessages = errorDetail.getUserMessages();
        assertEquals(integer.intValue(), userMessages.size());
    }

    default void equalsByHashCode(T existingType, T type) {
        assertEquals(existingType.hashCode(), type.hashCode());
    }

    default void equalsByTypeId(T existingType, T type) {
        assertEquals(existingType, type);
    }

    default void validateTypesByHashCode(List<T> existingTypes, List<T> types) {
        for (T existingType : existingTypes) {
            for (T type : types) {
                if (existingType.hashCode() == type.hashCode()) {
                    LOGGER.info("Existing type" + existingType.toString());
                    LOGGER.info("Type" + type.toString());
                    assertEquals(existingType.hashCode(), type.hashCode());
                }
            }
        }
    }

    default void validateListTypes(List<T> existingTypes, List<T> types) {
        assertEquals(existingTypes, types);
    }
}
