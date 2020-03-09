package com.v8tix.katix.social.exception;

import com.v8tix.katix.social.util.BeanValidationHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.OffsetDateTime;
import java.util.StringTokenizer;

import static com.v8tix.katix.social.util.DateHelper.getOffsetDateTime;
import static com.v8tix.katix.social.util.DateHelper.isoDate;

@ControllerAdvice
public class RestExceptionHandler {

    private static final String VALIDATION_FAILED = "Validation Failed";
    private static final String INPUT_VALIDATION_FAILED = "Input validation failed";

    @ExceptionHandler(BeanValidationException.class)
    public ResponseEntity<ErrorDetail> handleErrorDetailException(
            final BeanValidationException bve) {
        final StringTokenizer errorsST = new StringTokenizer(bve.getMessage(),
                BeanValidationHelper.DELIMITER_STRING);
        final ErrorDetail errorDetail = new ErrorDetail();
        final OffsetDateTime now = getOffsetDateTime();
        errorDetail.setTitle(VALIDATION_FAILED);
        errorDetail.setDetail(INPUT_VALIDATION_FAILED);
        errorDetail.setTimeStamp(isoDate(now));
        errorDetail.setStatus(HttpStatus.BAD_REQUEST.value());
        errorDetail.setDeveloperMessage(RestExceptionHandler.class.getName());
        while (errorsST.hasMoreElements()) {
            errorDetail.addUserMessage(errorsST.nextToken());
        }
        return new ResponseEntity<>(errorDetail, null, HttpStatus.BAD_REQUEST);
    }
}
