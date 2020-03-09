package com.v8tix.katix.social;

import com.v8tix.katix.social.utils.CommonConstants;
import com.v8tix.katix.social.util.LinkRelation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static com.v8tix.katix.social.util.RestHelper.*;
import static com.v8tix.katix.social.util.StringHelper.*;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@ActiveProfiles("dev")
public class PostPaginationTests implements CommonConstants {

  private static final Logger LOGGER = LoggerFactory.getLogger(PostPaginationTests.class);
  private static final long PER_PAGE = 5;

  @Value("${rest.posts.path}")
  private String postsPath;

  @Test
  public void shouldGenerateNextLink() {
    final long numberOfElements = 21;
    final long numberOfPages = getNumberOfPages(numberOfElements, PER_PAGE) - 1;
    for (int i = 0; i < numberOfPages; i++) {
      long actualPage = i + 1;
      long nextPage = actualPage + 1;
      final String expectedLink =
          getWebLink(
              nextPage,
              LinkRelation.NEXT.getRel(),
              LinkRelation.NEXT.getTitle(),
              LinkRelation.NEXT.getType());
      LOGGER.info(concatStrings(COLON, "Expected link", expectedLink));
      final String nextLink = createNextWebLink(postsPath, numberOfElements, actualPage, PER_PAGE);
      LOGGER.info(concatStrings(COLON, "Generated link", expectedLink));
      assertEquals(expectedLink, nextLink);
    }
  }

  @Test
  public void shouldNotGenerateNextLinkActualPageEqualsToZero() {
    final long actualPage = 0;
    final long numberOfElements = 21;
    final String nextLink = createNextWebLink(postsPath, numberOfElements, actualPage, PER_PAGE);
    assertEquals(EMPTY_STRING, nextLink);
  }

  @Test
  public void shouldNotGenerateNextLinkActualPageHigherThanNumberOfPages() {
    final long actualPage = 7;
    final long numberOfElements = 21;
    final String nextLink = createNextWebLink(postsPath, numberOfElements, actualPage, PER_PAGE);
    assertEquals(EMPTY_STRING, nextLink);
  }

  @Test
  public void shouldNotGenerateNextLinkActualPageEqualsToNumberOfPages() {
    final long actualPage = 5;
    final long numberOfElements = 21;
    final String nextLink = createNextWebLink(postsPath, numberOfElements, actualPage, PER_PAGE);
    assertEquals(EMPTY_STRING, nextLink);
  }

  @Test
  public void shouldNotGenerateNextLinkNumberOfPagesEqualsToZero() {
    final long actualPage = 5;
    final long numberOfElements = 0;
    final String nextLink = createNextWebLink(postsPath, numberOfElements, actualPage, PER_PAGE);
    assertEquals(EMPTY_STRING, nextLink);
  }

  @Test
  public void shouldGeneratePreviousLink() {
    final long numberOfElements = 21;
    final long numberOfPages = getNumberOfPages(numberOfElements, PER_PAGE);
    for (long i = numberOfPages; i > 1; i--) {
      long previousPage = i - 1;
      final String expectedLink =
          getWebLink(
              previousPage,
              LinkRelation.PREV.getRel(),
              LinkRelation.PREV.getTitle(),
              LinkRelation.PREV.getType());
      LOGGER.info(concatStrings(COLON, "Expected link", expectedLink));
      final String nextLink = createPreviousWebLink(postsPath, numberOfElements, i, PER_PAGE);
      LOGGER.info(concatStrings(COLON, "Generated link", expectedLink));
      assertEquals(expectedLink, nextLink);
    }
  }

  @Test
  public void shouldNotGeneratePreviousLinkActualPageEqualsToZero() {
    final long actualPage = 0;
    final long numberOfElements = 21;
    final String nextLink =
        createPreviousWebLink(postsPath, numberOfElements, actualPage, PER_PAGE);
    assertEquals(EMPTY_STRING, nextLink);
  }

  @Test
  public void shouldNotGeneratePreviousLinkActualPageHigherThanNumberOfPages() {
    final long actualPage = 7;
    final long numberOfElements = 21;
    final String nextLink =
        createPreviousWebLink(postsPath, numberOfElements, actualPage, PER_PAGE);
    assertEquals(EMPTY_STRING, nextLink);
  }

  @Test
  public void shouldNotGeneratePreviousLinkNumberOfPagesEqualsToZero() {
    final long actualPage = 5;
    final long numberOfElements = 0;
    final String nextLink =
        createPreviousWebLink(postsPath, numberOfElements, actualPage, PER_PAGE);
    assertEquals(EMPTY_STRING, nextLink);
  }

  @Test
  public void shouldNotGeneratePreviousLinkActualPageEqualsToOne() {
    final long actualPage = 1;
    final long numberOfElements = 21;
    final String nextLink =
        createPreviousWebLink(postsPath, numberOfElements, actualPage, PER_PAGE);
    assertEquals(EMPTY_STRING, nextLink);
  }

  private String getWebLink(
      final long actualPage, final String rel, final String title, final String type) {
    final String pageableUri = format(PAGEABLE_PART, actualPage, PER_PAGE);
    return concatStrings(
        EMPTY_STRING,
        "<",
        postsPath,
        pageableUri,
        ">;",
        "rel=\"",
        rel,
        "\";",
        "title=\"",
        title,
        "\";",
        "type=\"",
        type,
        "\"");
  }
}
