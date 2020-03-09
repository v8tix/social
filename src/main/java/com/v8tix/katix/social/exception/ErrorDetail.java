package com.v8tix.katix.social.exception;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.v8tix.katix.social.util.StringHelper.toJson;

@Data
public class ErrorDetail {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorDetail.class);
    private String title;
    private int status;
    private String detail;
    private String timeStamp;
    private String developerMessage;
    private List<String> userMessages = new ArrayList<>();

    public ErrorDetail() {
    }

    void addUserMessage(String userMessage) {
        userMessages.add(userMessage);
    }

    @Override
    public String toString() {
        return toJson(this);
    }
}
