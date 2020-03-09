package com.v8tix.katix.social;

import com.v8tix.katix.social.model.User;
import com.v8tix.katix.social.utils.CommonConstants;
import com.v8tix.katix.social.utils.CommonRestTestsHelper;
import com.v8tix.katix.social.utils.CommonValidatorsHelper;
import com.v8tix.katix.social.util.MathHelper;
import com.v8tix.katix.social.util.MessageSourceHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static com.v8tix.katix.social.util.RestHelper.createPageablePath;
import static com.v8tix.katix.social.util.StringHelper.*;
import static java.lang.String.valueOf;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@ActiveProfiles("dev")
public class UserControllerTests
    implements CommonRestTestsHelper<User>,
        CommonValidatorsHelper<User>,
        MessageSourceHelper,
        CommonConstants {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserControllerTests.class);
  private static final long PAGE = 1;
  private static final long PER_PAGE = 5;

  @Autowired private WebTestClient webTestClient;

  @Autowired private MongoOperations operations;

  @Autowired private MessageSource messageSource;

  @Value("${rest.users.path}")
  private String usersPath;

  private List<User> users;
  private User user;

  @Before
  public void init() {
    users = operations.findAll(User.class);
    user = (User) MathHelper.getRandomObject(users);
  }

  @Test
  public void getAllUsers() {
    final String getAllUsersPageableUri = createPageablePath(usersPath, PAGE, PER_PAGE);
    users = getPageableUsers();
    getAll(webTestClient, getAllUsersPageableUri, users, User.class, this::validateListTypes);
  }

  private List<User> getPageableUsers() {
    return users.stream()
        .sorted(comparing(User::getCreatedAtEpoch).reversed())
        .skip(PER_PAGE * (PAGE - 1))
        .limit(PER_PAGE)
        .collect(toList());
  }

  @Test
  public void shouldFindUser() {
    getById(webTestClient, usersPath, user.getId(), user, User.class, this::equalsByTypeId);
  }

  @Test
  public void shouldNotFindUser() {
    getByFakeId(webTestClient, usersPath, FAKE_STRING);
  }

  @Test
  public void shouldDeleteUser() {
    deleteById(webTestClient, usersPath, user.getId());
    final List<User> users = operations.findAll(User.class);
    assertEquals(this.users.size() - 1, users.size());
  }

  @Test
  public void shouldNotDeleteUser() {
    deleteByFakeId(webTestClient, usersPath, FAKE_STRING);
    final List<User> users = operations.findAll(User.class);
    assertEquals(this.users.size(), users.size());
  }

  @Test
  public void shouldNotSaveUserEmptyFields() {
    final int propertiesNumber = 6;
    postInvalidType(
        webTestClient,
        usersPath,
        User.class,
        new User(),
        valueOf(propertiesNumber),
        this::validateErrorsArraySize);
  }

  @Test
  public void shouldNotSaveUserInvalidProfilePic() {
    user.setProfilePicture(null);
    final String expectedMessage = messageSource.getMessage(PROFILE_PIC_NOT_EMPTY, null, null);
    postInvalidType(
        webTestClient, usersPath, User.class, user, expectedMessage, this::validateExpectedMessage);
  }

  @Test
  public void shouldNotSaveUserEmptyProfilePic() {
    user.setProfilePicture(EMPTY_STRING);
    final String expectedMessage = messageSource.getMessage(PROFILE_PIC_NOT_EMPTY, null, null);
    postInvalidType(
        webTestClient, usersPath, User.class, user, expectedMessage, this::validateExpectedMessage);
  }

  @Test
  public void shouldNotSaveUserInvalidEmail() {
    user.setEmail(FAKE_STRING);
    final String expectedMessage = messageSource.getMessage(INVALID_EMAIL, null, null);

    postInvalidType(
        webTestClient, usersPath, User.class, user, expectedMessage, this::validateExpectedMessage);
  }

  @Test
  public void shouldNotSaveUserEmptyEmail() {
    user.setEmail(null);
    final String expectedMessage = messageSource.getMessage(EMAIL_NOT_EMPTY, null, null);
    postInvalidType(
        webTestClient, usersPath, User.class, user, expectedMessage, this::validateExpectedMessage);
  }

  @Test
  public void shouldNotSaveUserInvalidFirstNameMinLength() {
    user.setFirstName(FAKE_STRING);
    final String expectedMessage = messageSource.getMessage(USER_FIRST_NAME_SIZE, null, null);
    postInvalidType(
        webTestClient, usersPath, User.class, user, expectedMessage, this::validateExpectedMessage);
  }

  @Test
  public void shouldNotSaveUserInvalidFirstNameMaxLength() {
    user.setFirstName("Maaaaaaaaaaaaaaaaaaaa");
    final String expectedMessage = messageSource.getMessage(USER_FIRST_NAME_SIZE, null, null);
    postInvalidType(
        webTestClient, usersPath, User.class, user, expectedMessage, this::validateExpectedMessage);
  }

  @Test
  public void shouldNotSaveUserEmptyFirstName() {
    user.setFirstName(null);
    final String expectedMessage = messageSource.getMessage(FIRST_NAME_NOT_EMPTY, null, null);
    postInvalidType(
        webTestClient, usersPath, User.class, user, expectedMessage, this::validateExpectedMessage);
  }

  @Test
  public void shouldNotSaveUserInvalidLastNameMinLength() {
    user.setLastName(FAKE_STRING);
    final String expectedMessage = messageSource.getMessage(USER_LAST_NAME_SIZE, null, null);
    postInvalidType(
        webTestClient, usersPath, User.class, user, expectedMessage, this::validateExpectedMessage);
  }

  @Test
  public void shouldNotSaveUserInvalidLastNameMaxLength() {
    user.setLastName("Almeidaaaaaaaaaaaaaaa");
    final String expectedMessage = messageSource.getMessage(USER_LAST_NAME_SIZE, null, null);
    postInvalidType(
        webTestClient, usersPath, User.class, user, expectedMessage, this::validateExpectedMessage);
  }

  @Test
  public void shouldNotSaveUserEmptyLastName() {
    user.setLastName(null);
    final String expectedMessage = messageSource.getMessage(LAST_NAME_NOT_EMPTY, null, null);
    postInvalidType(
        webTestClient, usersPath, User.class, user, expectedMessage, this::validateExpectedMessage);
  }

  @Test
  public void shouldNotSaveUserInvalidGenderMinLength() {
    user.setGender(FAKE_STRING);
    final String expectedMessage = messageSource.getMessage(USER_GENDER_SIZE, null, null);
    postInvalidType(
        webTestClient, usersPath, User.class, user, expectedMessage, this::validateExpectedMessage);
  }

  @Test
  public void shouldNotSaveUserInvalidGenderMaxLength() {
    user.setGender("Maleeee");
    final String expectedMessage = messageSource.getMessage(USER_GENDER_SIZE, null, null);
    postInvalidType(
        webTestClient, usersPath, User.class, user, expectedMessage, this::validateExpectedMessage);
  }

  @Test
  public void shouldNotSaveUserEmptyGender() {
    user.setGender(null);
    final String expectedMessage = messageSource.getMessage(GENDER_NOT_EMPTY, null, null);
    postInvalidType(
        webTestClient, usersPath, User.class, user, expectedMessage, this::validateExpectedMessage);
  }

  @Test
  public void shouldSaveUser() {
    postValidType(webTestClient, usersPath, User.class, user);
  }

  @Test
  public void shouldNotUpdateUserValidIdInvalidUser() {
    final String expectedMessage = messageSource.getMessage(USER_GENDER_SIZE, null, null);
    LOGGER.info(concatStrings(COLON, "Existing User", user.toString()));
    user.setGender(FAKE_STRING);
    putIdInvalidType(
        webTestClient,
        usersPath,
        user.getId(),
        User.class,
        user,
        expectedMessage,
        this::validateExpectedMessage);
  }

  @Test
  public void shouldNotUpdateUserInvalidIdValidUser() {
    putInvalidIdValidType(webTestClient, usersPath, FAKE_STRING, User.class, user);
  }

  @Test
  public void shouldUpdateUserValidIdValidUser() {
    LOGGER.info(concatStrings(COLON, "Existing User", user.toString()));
    user.setEmail(FAKE_MAIL);
    LOGGER.info(concatStrings(COLON, "User with fake mail", user.toString()));
    putIdValidType(webTestClient, usersPath, user.getId(), User.class, user);
    final List<User> updatedUserList = operations.findAll(User.class);
    for (User updatedUser : updatedUserList) {
      if (updatedUser.getId().equals(user.getId())) {
        LOGGER.info(concatStrings(COLON, "Updated User", updatedUser.toString()));
        assertEquals(updatedUser.getEmail(), user.getEmail());
      }
    }
  }
}
