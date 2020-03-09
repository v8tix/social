package com.v8tix.katix.social.model;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

import static com.v8tix.katix.social.util.DateHelper.*;
import static com.v8tix.katix.social.util.StringHelper.toJson;

@Data
@Document
public class User {

    private static final Logger LOGGER = LoggerFactory.getLogger(User.class);

    @Id
    private String id;

    @NotEmpty(message = "{user.first.name.not.empty}")
    @Size(min = 3, max = 20, message = "{user.first.name.size}")
    private String firstName;

    @NotEmpty(message = "{user.last.name.not.empty}")
    @Size(min = 3, max = 20, message = "{user.last.name.size}")
    private String lastName;

    @NotEmpty(message = "{user.email.not.empty}")
    @Email(message = "{invalid.email}")
    private String email;

    @NotEmpty(message = "{user.gender.not.empty}")
    @Size(min = 4, max = 6, message = "{user.gender.size}")
    private String gender;

    @NotEmpty(message = "{user.profile.pic.not.empty}")
    private String profilePicture;

    @NotEmpty(message = "{createdAtISO.not.empty}")
    private String createdAtISO;

    private long createdAtEpoch;

    public User() {
    }

    public User(final String firstName,
                final String lastName,
                final String email,
                final String gender,
                final String profilePicture) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.gender = gender;
        this.profilePicture = profilePicture;
        OffsetDateTime offsetDateTime = getOffsetDateTime();
        this.createdAtISO = isoDate(offsetDateTime);
        this.createdAtEpoch = epochMilli(offsetDateTime);
    }

    public void makeCopyOf(final User user) {
        this.setId(user.getId());
        this.setGender(user.getGender());
        this.setFirstName(user.getFirstName());
        this.setLastName(user.getLastName());
        this.setEmail(user.getEmail());
        this.setProfilePicture(user.getProfilePicture());
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (getClass() == otherObject.getClass()) {
            User otherUser = (User) otherObject;
            return id.equals(otherUser.id);
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
