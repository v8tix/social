package com.v8tix.katix.social.model;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.v8tix.katix.social.util.DateHelper.*;
import static com.v8tix.katix.social.util.StringHelper.toJson;

@Data
@Document
public class Post {

    private static final Logger LOGGER = LoggerFactory.getLogger(Post.class);

    @Id
    private String id;

    private List<Comment> comments = new ArrayList<>();

    @NotEmpty(message = "{content.not.empty}")
    private String content;

    @NotEmpty(message = "{createdAtISO.not.empty}")
    private String createdAtISO;

    private long createdAtEpoch;

    @NotEmpty(message = "{userId.not.empty}")
    private String userId;

    @NotEmpty(message = "{user.profile.pic.not.empty}")
    private String profilePicture;

    public Post() {
    }

    public Post(final String content, final String userId) {
        this.content = content;
        OffsetDateTime offsetDateTime = getOffsetDateTime();
        this.createdAtISO = isoDate(offsetDateTime);
        this.createdAtEpoch = epochMilli(offsetDateTime);
        this.userId = userId;
    }

    public void makeCopyOf(final Post post) {
        this.setUserId(post.getUserId());
        this.setContent(post.getContent());
        this.setCreatedAtISO(post.getCreatedAtISO());
    }

    public void addComments(final List<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (getClass() == otherObject.getClass()) {
            Post otherPost = (Post) otherObject;
            return id.equals(otherPost.id);
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
