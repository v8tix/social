package com.v8tix.katix.social;

import com.v8tix.katix.social.model.Post;
import com.v8tix.katix.social.utils.CommonConstants;
import com.v8tix.katix.social.utils.CommonRestTestsHelper;
import com.v8tix.katix.social.utils.CommonValidatorsHelper;
import com.v8tix.katix.social.util.MathHelper;
import com.v8tix.katix.social.util.MessageSourceHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static com.v8tix.katix.social.util.MessageSourceHelper.getMessage;
import static com.v8tix.katix.social.util.RestHelper.*;
import static java.lang.String.valueOf;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@ActiveProfiles("dev")
public class PostControllerTests
    implements CommonRestTestsHelper<Post>,
        CommonValidatorsHelper<Post>,
        MessageSourceHelper,
        CommonConstants {

  private static final long PAGE = 1;
  private static final long PER_PAGE = 5;

  @Autowired private WebTestClient webTestClient;

  @Autowired private MongoOperations operations;

  @Autowired private MessageSource messageSource;

  @Value("${rest.posts.path}")
  private String postsPath;

  @Value("${rest.comments.path}")
  private String commentsPath;

  private Post post;
  private List<Post> posts;

  @Before
  public void init() {
    posts = operations.findAll(Post.class);
    post = (Post) MathHelper.getRandomObject(posts);
  }

  @Test
  public void getAllPostsWithoutComments() {
    final String postsPageablePath = createPageablePath(postsPath, PAGE, PER_PAGE);
    posts = getPageablePosts();
    getAll(webTestClient, postsPageablePath, posts, Post.class, this::validateListTypes);
  }

  @Test
  public void getAllPostsWithComments() {
    final String postsPageablePath = createPageablePath(postsPath, PAGE, PER_PAGE);
    final String postsPageableEmbedPath = createEmbedPath(postsPageablePath, EMBED_COMMENTS);
    posts = getPageablePosts();
    getAll(webTestClient, postsPageableEmbedPath, posts, Post.class, this::validateListTypes);
  }

  private List<Post> getPageablePosts() {
    return posts.stream()
        .sorted(comparing(Post::getCreatedAtEpoch).reversed())
        .skip(PER_PAGE * (PAGE - 1))
        .limit(PER_PAGE)
        .collect(toList());
  }

  @Test
  public void shouldFindPost() {
    final String id = post.getId();
    getById(webTestClient, postsPath, id, post, Post.class, this::equalsByTypeId);
  }

  @Test
  public void shouldNotFindPost() {
    getByFakeId(webTestClient, postsPath, FAKE_STRING);
  }

  @Test
  public void shouldDeletePost() {
    deleteById(webTestClient, postsPath, post.getId());
    final List<Post> posts = operations.findAll(Post.class);
    assertEquals(this.posts.size() - 1, posts.size());
  }

  @Test
  public void shouldNotDeletePost() {
    deleteByFakeId(webTestClient, postsPath, FAKE_STRING);
    final List<Post> posts = operations.findAll(Post.class);
    assertEquals(this.posts.size(), posts.size());
  }

  @Test
  public void shouldNotSavePostEmptyFields() {
    final int propertiesNumber = 4;
    postInvalidType(
        webTestClient,
        postsPath,
        Post.class,
        new Post(),
        valueOf(propertiesNumber),
        this::validateErrorsArraySize);
  }

  @Test
  public void shouldNotSavePostInvalidContent() {
    post.setContent(null);
    final String expectedMessage = getMessage(messageSource, CONTENT_NOT_EMPTY, null);
    postInvalidType(
        webTestClient, postsPath, Post.class, post, expectedMessage, this::validateExpectedMessage);
  }

  @Test
  public void shouldSavePost() {
    postValidType(webTestClient, postsPath, Post.class, post);
  }

  @Test
  public void shouldNotUpdatePostInvalidContent() {
    final String expectedMessage = getMessage(messageSource, CONTENT_NOT_EMPTY, null);
    post.setContent(null);
    putIdInvalidType(
        webTestClient,
        postsPath,
        post.getId(),
        Post.class,
        post,
        expectedMessage,
        this::validateExpectedMessage);
  }

  @Test
  public void shouldNotUpdatePostInvalidId() {
    putInvalidIdValidType(webTestClient, postsPath, FAKE_STRING, Post.class, post);
  }

  @Test
  public void shouldUpdatePost() {
    final Post postToUpdate = posts.get(FIRST_INDEX);
    postToUpdate.setContent(FAKE_STRING);
    putIdValidType(webTestClient, postsPath, postToUpdate.getId(), Post.class, postToUpdate);
    final List<Post> updatedPostList = operations.findAll(Post.class);
    for (Post post : updatedPostList) {
      if (post.getId().equals(postToUpdate.getId())) {
        assertEquals(post.getContent(), postToUpdate.getContent());
      }
    }
  }
}
