package com.v8tix.katix.social.model;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;
import java.time.OffsetDateTime;

import static com.v8tix.katix.social.util.DateHelper.*;
import static com.v8tix.katix.social.util.StringHelper.toJson;

@Data
@Document
public class Comment {

    private static final Logger LOGGER = LoggerFactory.getLogger(Comment.class);

    @Id
    private String id;

    @NotEmpty(message = "{comment.not.empty}")
    private String content;

    @NotEmpty(message = "{userId.not.empty}")
    private String userId;

    @NotEmpty(message = "{postId.not.empty}")
    private String postId;

    @NotEmpty(message = "{createdAtISO.not.empty}")
    private String createdAtISO;

    private long createdAtEpoch;

    public Comment() {
    }

    public Comment(final String content, final String userId, final String postId) {
        this.content = content;
        this.userId = userId;
        this.postId = postId;
        OffsetDateTime offsetDateTime = getOffsetDateTime();
        this.createdAtISO = isoDate(offsetDateTime);
        this.createdAtEpoch = epochMilli(offsetDateTime);
    }

    public void makeCopyOf(final Comment comment) {
        this.setUserId(comment.getUserId());
        this.setPostId(comment.getPostId());
        this.setContent(comment.getContent());
        this.setCreatedAtISO(comment.getCreatedAtISO());
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (getClass() == otherObject.getClass()) {
            Comment otherComment = (Comment) otherObject;
            return id.equals(otherComment.id);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return toJson(this);
    }
}
