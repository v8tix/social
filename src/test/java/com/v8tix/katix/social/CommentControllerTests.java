package com.v8tix.katix.social;

import com.v8tix.katix.social.model.Comment;
import com.v8tix.katix.social.model.Post;
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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static com.v8tix.katix.social.util.MessageSourceHelper.getMessage;
import static com.v8tix.katix.social.util.RestHelper.createPageablePath;
import static com.v8tix.katix.social.util.StringHelper.COLON;
import static com.v8tix.katix.social.util.StringHelper.concatStrings;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@ActiveProfiles("dev")
public class CommentControllerTests
    implements CommonRestTestsHelper<Comment>,
        CommonValidatorsHelper<Comment>,
        MessageSourceHelper,
        CommonConstants {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommentControllerTests.class);
  private static final long PER_PAGE = 5;

  @Autowired private WebTestClient webTestClient;

  @Autowired private MongoOperations operations;

  @Autowired private MessageSource messageSource;

  @Value("${rest.comments.path}")
  private String commentsPath;

  private Post post;
  private Comment comment;
  private Comment newComment;

  @Before
  public void init() {
    final List<Post> posts = operations.findAll(Post.class);
    final List<User> users = operations.findAll(User.class);
    final List<Comment> comments = operations.findAll(Comment.class);
    comment = (Comment) MathHelper.getRandomObject(comments);
    post = (Post) MathHelper.getRandomObject(posts);
    final User user = (User) MathHelper.getRandomObject(users);
    final String userId = user.getId();
    final String postId = post.getId();
    newComment = new Comment(FAKE_CONTENT, userId, postId);
  }

  @Test
  public void getAllComments() {
    final int page = 1;
    final String commentsUri = createPageablePath(commentsPath, page, PER_PAGE);
    final String postId = post.getId();
    final List<Comment> comments = getCommentsByPostId(postId, operations);
    getAll(
        webTestClient,
        format(commentsUri, postId),
        comments,
        Comment.class,
        this::validateTypesByHashCode);
  }

  @Test
  public void shouldFindComment() {
    final String postId = post.getId();
    final Comment comment = getRandomCommentByPostId(postId, operations);
    getById(
        webTestClient,
        format(commentsPath, postId),
        comment.getId(),
        comment,
        Comment.class,
        this::equalsByHashCode);
  }

  private Comment getRandomCommentByPostId(String postId, MongoOperations operations) {
    final List<Comment> tempComments = getCommentsByPostId(postId, operations);
    return (Comment) MathHelper.getRandomObject(tempComments);
  }

  private List<Comment> getCommentsByPostId(String postId, MongoOperations operations) {
    final Criteria criteriaDefinition = Criteria.where("postId").is(postId);
    return operations.find(Query.query(criteriaDefinition), Comment.class);
  }

  @Test
  public void shouldNotFindCommentBadPostId() {
    getByFakeId(webTestClient, format(commentsPath, FAKE_STRING), comment.getId());
  }

  @Test
  public void shouldNotFindCommentBadCommentId() {
    getByFakeId(webTestClient, format(commentsPath, post.getId()), FAKE_STRING);
  }

  @Test
  public void shouldNotFindCommentBadParameters() {
    getByFakeId(webTestClient, format(commentsPath, FAKE_STRING), FAKE_STRING);
  }

  @Test
  public void shouldNotSaveCommentEmptyFields() {
    final String postId = post.getId();
    final int propertiesNumber = 4;
    postInvalidType(
        webTestClient,
        format(commentsPath, postId),
        Comment.class,
        new Comment(),
        valueOf(propertiesNumber),
        this::validateErrorsArraySize);
  }

  @Test
  public void shouldNotSaveCommentInvalidContent() {
    final String postId = post.getId();
    comment.setContent(null);
    final String expectedMessage = getMessage(messageSource, COMMENT_NOT_EMPTY, null);
    postInvalidType(
        webTestClient,
        format(commentsPath, postId),
        Comment.class,
        comment,
        expectedMessage,
        this::validateExpectedMessage);
  }

  @Test
  public void shouldSaveComment() {
    postValidType(
        webTestClient, format(commentsPath, newComment.getPostId()), Comment.class, newComment);
  }

  @Test
  public void shouldNotSaveCommentBadPostIdParameter() {
    final String commentsUriWithPostId = format(commentsPath, FAKE_STRING);
    postSingleError(
        webTestClient,
        commentsUriWithPostId,
        Comment.class,
        comment,
        getMessage(messageSource, POST_ID_NOT_EQUALS_ERROR_MSG, null));
  }

  @Test
  public void shouldNotSaveCommentPostIdDoesNotExistParameter() {
    final String commentsUriWithPostId = format(commentsPath, FAKE_STRING);
    comment.setPostId(FAKE_STRING);
    postSingleError(
        webTestClient,
        commentsUriWithPostId,
        Comment.class,
        comment,
        getMessage(messageSource, UPSERT_INVALID_PARAMS_USER, null));
  }

  @Test
  public void shouldNotSaveCommentUserIdDoesNotExistParameter() {
    final String commentsUriWithPostId = format(commentsPath, comment.getPostId());
    comment.setUserId(FAKE_STRING);
    postSingleError(
        webTestClient,
        commentsUriWithPostId,
        Comment.class,
        comment,
        getMessage(messageSource, UPSERT_INVALID_PARAMS_USER, null));
  }

  @Test
  public void shouldNotUpdateCommentInvalidContent() {
    final String postId = post.getId();
    final Comment comment = getRandomCommentByPostId(postId, operations);
    final String expectedMessage = getMessage(messageSource, COMMENT_NOT_EMPTY, null);
    LOGGER.info(concatStrings(COLON, "Existing Comment", comment.toString()));
    comment.setContent(null);
    putIdInvalidType(
        webTestClient,
        format(commentsPath, postId),
        comment.getId(),
        Comment.class,
        comment,
        expectedMessage,
        this::validateExpectedMessage);
  }

  @Test
  public void shouldNotUpdateCommentPostIdDoesNotExists() {
    final String expectedMessage = getMessage(messageSource, UPSERT_INVALID_PARAMS_USER, null);
    comment.setPostId(FAKE_STRING);
    putSingleError(
        webTestClient,
        format(commentsPath, comment.getPostId()),
        comment.getId(),
        Comment.class,
        comment,
        expectedMessage,
        this::validateExpectedMessage);
  }

  @Test
  public void shouldNotUpdateCommentCommentIdDoesNotExists() {
    final String expectedMessage = getMessage(messageSource, UPSERT_INVALID_PARAMS_USER, null);
    comment.setId(FAKE_STRING);
    putSingleError(
        webTestClient,
        format(commentsPath, comment.getPostId()),
        comment.getId(),
        Comment.class,
        comment,
        expectedMessage,
        this::validateExpectedMessage);
  }

  @Test
  public void shouldNotUpdateCommentUserIdDoesNotExists() {
    final String expectedMessage = getMessage(messageSource, UPSERT_INVALID_PARAMS_USER, null);
    comment.setUserId(FAKE_STRING);
    putSingleError(
        webTestClient,
        format(commentsPath, comment.getPostId()),
        comment.getId(),
        Comment.class,
        comment,
        expectedMessage,
        this::validateExpectedMessage);
  }

  @Test
  public void shouldUpdateComment() {
    final String postId = post.getId();
    final Comment commentToUpdate = getRandomCommentByPostId(postId, operations);
    commentToUpdate.setContent(FAKE_STRING);
    putIdValidType(
        webTestClient,
        format(commentsPath, postId),
        commentToUpdate.getId(),
        Comment.class,
        commentToUpdate);
    final List<Comment> updatedCommentList = operations.findAll(Comment.class);
    for (Comment comment : updatedCommentList) {
      if (comment.getId().equals(commentToUpdate.getId())) {
        assertEquals(comment.getContent(), commentToUpdate.getContent());
      }
    }
  }

  @Test
  public void shouldDeleteComment() {
    final String postId = post.getId();
    final Comment comment = getRandomCommentByPostId(postId, operations);
    deleteById(webTestClient, format(commentsPath, postId), comment.getId());
  }

  @Test
  public void shouldNotDeleteComment() {
    final String postId = post.getId();
    deleteByFakeId(webTestClient, format(commentsPath, postId), FAKE_STRING);
  }
}
