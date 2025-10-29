package com.legoaggelos.catplace;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.legoaggelos.catplace.cats.posts.comments.Comment;
import net.minidev.json.JSONArray;
import org.apache.coyote.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
public class CommentTests {
    @Autowired
    TestRestTemplate restTemplate;

    protected static final OffsetDateTime sampleDate = OffsetDateTime.of(2025,4, 8, 2, 30, 30, 0, ZoneOffset.ofHours(0));

    @Test
    void shouldGetCommentFromId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/comments/46", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int id = documentContext.read("$.id");
        assertThat(id).isEqualTo(46);

        int postId = documentContext.read("$.postId");
        assertThat(postId).isEqualTo(4);

        int likeCount = documentContext.read("$.likeCount");
        assertThat(likeCount).isEqualTo(0);

        int postCatPoster = documentContext.read("$.postCatPoster");
        assertThat(postCatPoster).isEqualTo(4);

        String content = documentContext.read("$.content");
        assertThat(content).isEqualTo("Love your cat");

        String poster = documentContext.read("$.poster");
        assertThat(poster).isEqualTo("paul");

        String postPoster = documentContext.read("$.postPoster");
        assertThat(postPoster).isEqualTo("kat");

        String postTime = documentContext.read("$.postTime");
        assertThat(postTime).isEqualTo("2025-04-08T02:45:30Z");
    }

    @Test
    void shouldAllowSinglePostAccessToAnyone() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/comments/46", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldGetAllCommentsFromPostSorted() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/comments/fromPostId/4", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContextPage = JsonPath.parse(response.getBody());
        JSONArray page = documentContextPage.read("$[*]");

        assertThat(page.size()).isEqualTo(3);

        assertThat(page.toJSONString()).contains("ragebait", "Your cat is so cute!!", "Love your cat");

        JSONArray postTimes = documentContextPage.read("$..postTime");
        assertThat(postTimes).containsExactly("2025-04-08T02:45:45Z", "2025-04-08T02:45:30Z", "2025-04-08T02:40:30Z");
    }

    @Test
    void shouldGetAllCommentsFromUser() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/comments/fromPoster/kat", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContextPage = JsonPath.parse(response.getBody());
        JSONArray page = documentContextPage.read("$[*]");

        assertThat(page.size()).isEqualTo(3);

        assertThat(page.toJSONString()).contains("Your car is so cute!!", "Love your car", "ragebait");
    }

    @Test
    void shouldGetAllCommentsFromPost() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/comments/fromPostId/4", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContextPage = JsonPath.parse(response.getBody());
        JSONArray page = documentContextPage.read("$[*]");

        assertThat(page.size()).isEqualTo(3);

        assertThat(page.toJSONString()).contains("Your cat is so cute!!", "Love your cat", "ragebait");
    }

    @Test
    void shouldGetAllCommentsFromPostSortedByLikesWhenRequested() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/comments/fromPostId/4?sort=likeCount,desc", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContextPage = JsonPath.parse(response.getBody());
        JSONArray page = documentContextPage.read("$[*]");

        assertThat(page.size()).isEqualTo(3);

        assertThat(page.toJSONString()).contains("Your cat is so cute!!", "Love your cat", "ragebait");

        JSONArray likes = documentContextPage.read("$..likeCount");
        assertThat(likes).containsExactly(1, 0, 0);
    }

    @Test
    @DirtiesContext
    void shouldCreateComment() {
        Comment comment = new Comment(47L, "I wanna eat it!!", 50L, 4L, "afehae", "kat", 4L, sampleDate, null);
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .postForEntity("/comments", comment, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> getResponse = restTemplate
                .getForEntity("/comments/1", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        int id = documentContext.read("$.id");
        assertThat(id).isEqualTo(1);

        int postId = documentContext.read("$.postId");
        assertThat(postId).isEqualTo(4);

        int likeCount = documentContext.read("$.likeCount");
        assertThat(likeCount).isEqualTo(0);

        int postCatPoster = documentContext.read("$.postCatPoster");
        assertThat(postCatPoster).isEqualTo(4);

        String content = documentContext.read("$.content");
        assertThat(content).isEqualTo("I wanna eat it!!");

        String poster = documentContext.read("$.poster");
        assertThat(poster).isEqualTo("legoaggelos");

        String postPoster = documentContext.read("$.postPoster");
        assertThat(postPoster).isEqualTo("kat");

        OffsetDateTime postTime = OffsetDateTime.parse(documentContext.read("$.postTime"));
        assertThat(Instant.now().getEpochSecond()-postTime.toEpochSecond()).isLessThan(60);
    }

    @Test
    @DirtiesContext
    void shouldNotCreateCommentIfUnauthorized() {
        Comment comment = new Comment(47L, "I wanna eat it!!", 50L, 4L, "afehae", "kat", 4L, sampleDate, null);
        ResponseEntity<Void> createResponse = restTemplate
                .postForEntity("/comments", comment, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> getResponse = restTemplate
                .getForEntity("/comments/1", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(getResponse.getBody()).isNullOrEmpty();
    }

    @Test
    @DirtiesContext
    void shouldUpdateOnlyContentIfOwnerNotAdmin() {
        Comment update = new Comment(13L, "Love your cat \n edit: thanks for 0 likes!!", 131L, 414L, "kego", "someone", 3123L, sampleDate, 41412L);
        HttpEntity<Comment> entity = new HttpEntity<>(update);

        ResponseEntity<Void> putResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/comments/46", HttpMethod.PUT, entity, Void.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/comments/46", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int id = documentContext.read("$.id");
        assertThat(id).isEqualTo(46);

        int postId = documentContext.read("$.postId");
        assertThat(postId).isEqualTo(4);

        int likeCount = documentContext.read("$.likeCount");
        assertThat(likeCount).isEqualTo(0);

        int postCatPoster = documentContext.read("$.postCatPoster");
        assertThat(postCatPoster).isEqualTo(4);

        int replying = documentContext.read("$.replyingTo");
        assertThat(replying).isEqualTo(3);

        String content = documentContext.read("$.content");
        assertThat(content).isEqualTo("Love your cat \n edit: thanks for 0 likes!!");

        String poster = documentContext.read("$.poster");
        assertThat(poster).isEqualTo("paul");

        String postPoster = documentContext.read("$.postPoster");
        assertThat(postPoster).isEqualTo("kat");

        String postTime = documentContext.read("$.postTime");
        assertThat(postTime).isEqualTo("2025-04-08T02:45:30Z");
    }

    @Test
    @DirtiesContext
    void shouldUpdateOnlyLikeCountIfNotOwnerNotAdmin() {
        Comment update = new Comment(13L, "Love your cat \n edit: thanks for 0 likes!!", 1L, 414L, "kego", "someone", 3123L, sampleDate, 4141L);
        HttpEntity<Comment> entity = new HttpEntity<>(update);

        ResponseEntity<Void> putResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/46", HttpMethod.PUT, entity, Void.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/comments/46", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int id = documentContext.read("$.id");
        assertThat(id).isEqualTo(46);

        int postId = documentContext.read("$.postId");
        assertThat(postId).isEqualTo(4);

        int likeCount = documentContext.read("$.likeCount");
        assertThat(likeCount).isEqualTo(1);

        int postCatPoster = documentContext.read("$.postCatPoster");
        assertThat(postCatPoster).isEqualTo(4);

        int replying = documentContext.read("$.replyingTo");
        assertThat(replying).isEqualTo(3);

        String content = documentContext.read("$.content");
        assertThat(content).isEqualTo("Love your cat");

        String poster = documentContext.read("$.poster");
        assertThat(poster).isEqualTo("paul");

        String postPoster = documentContext.read("$.postPoster");
        assertThat(postPoster).isEqualTo("kat");

        String postTime = documentContext.read("$.postTime");
        assertThat(postTime).isEqualTo("2025-04-08T02:45:30Z");
    }

    @Test
    @DirtiesContext
    void shouldNotOverlyUpdateLikeCountIfNotOwnerNotAdmin() {
        Comment update = new Comment(13L, "Love your cat \n edit: thanks for 0 likes!!", 10L, 414L, "kego", "someone", 3123L, sampleDate, 444L);
        HttpEntity<Comment> entity = new HttpEntity<>(update);

        ResponseEntity<Void> putResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/46", HttpMethod.PUT, entity, Void.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/comments/46", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int likeCount = documentContext.read("$.likeCount");
        assertThat(likeCount).isEqualTo(0);
    }

    @Test
    @DirtiesContext
    void shouldUpdateOnlyLikeCountIfAdmin() {
        Comment update = new Comment(13L, "Love your cat \n edit: thanks for 0 likes!!", 11L, 414L, "kego", "someone", 3123L, sampleDate, 124319L);
        HttpEntity<Comment> entity = new HttpEntity<>(update);

        ResponseEntity<Void> putResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/46", HttpMethod.PUT, entity, Void.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/comments/46", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int id = documentContext.read("$.id");
        assertThat(id).isEqualTo(46);

        int postId = documentContext.read("$.postId");
        assertThat(postId).isEqualTo(4);

        int likeCount = documentContext.read("$.likeCount");
        assertThat(likeCount).isEqualTo(11);

        int postCatPoster = documentContext.read("$.postCatPoster");
        assertThat(postCatPoster).isEqualTo(4);

        int replying = documentContext.read("$.replyingTo");
        assertThat(replying).isEqualTo(3);

        String content = documentContext.read("$.content");
        assertThat(content).isEqualTo("Love your cat");

        String poster = documentContext.read("$.poster");
        assertThat(poster).isEqualTo("paul");

        String postPoster = documentContext.read("$.postPoster");
        assertThat(postPoster).isEqualTo("kat");

        String postTime = documentContext.read("$.postTime");
        assertThat(postTime).isEqualTo("2025-04-08T02:45:30Z");
    }

    @Test
    @DirtiesContext
    void shouldNotUpdateIfUnauthorized() {
        Comment update = new Comment(13L, "Love your cat \n edit: thanks for 0 likes!!", 131L, 414L, "kego", "someone", 3123L, sampleDate, 444L);
        HttpEntity<Comment> entity = new HttpEntity<>(update);

        ResponseEntity<Void> putResponse = restTemplate
                .exchange("/comments/46", HttpMethod.PUT, entity, Void.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/comments/46", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        int id = documentContext.read("$.id");
        assertThat(id).isEqualTo(46);

        int postId = documentContext.read("$.postId");
        assertThat(postId).isEqualTo(4);

        int likeCount = documentContext.read("$.likeCount");
        assertThat(likeCount).isEqualTo(0);

        int postCatPoster = documentContext.read("$.postCatPoster");
        assertThat(postCatPoster).isEqualTo(4);

        int replying = documentContext.read("$.replyingTo");
        assertThat(replying).isEqualTo(3);

        String content = documentContext.read("$.content");
        assertThat(content).isEqualTo("Love your cat");

        String poster = documentContext.read("$.poster");
        assertThat(poster).isEqualTo("paul");

        String postPoster = documentContext.read("$.postPoster");
        assertThat(postPoster).isEqualTo("kat");

        String postTime = documentContext.read("$.postTime");
        assertThat(postTime).isEqualTo("2025-04-08T02:45:30Z");
    }

    @Test
    @DirtiesContext
    void shouldDeleteOwnComment() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/2", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/2", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).isNullOrEmpty();
    }

    @Test
    @DirtiesContext
    void shouldDeleteOthersCommentIfAdmin() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/2", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/2", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).isNullOrEmpty();
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteOthersCommentIfNotAdmin() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/comments/2", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/2", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotEmpty();
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteOthersCommentIfUnauthed() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .exchange("/comments/2", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/2", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotEmpty();
    }

    @Test
    @DirtiesContext
    void shouldDeleteOwnComments() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/deleteByPoster/kat", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/fromPoster/kat", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).isNullOrEmpty();

        ResponseEntity<String> otherGets = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/fromPoster/paul", HttpMethod.GET, null, String.class);
        assertThat(otherGets.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(otherGets.getBody()).read("$[*]")).size()).isEqualTo(2);
    }

    @Test
    @DirtiesContext
    void shouldDeleteOthersCommentsIfAdmin() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/deleteByPoster/kat", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/fromPoster/kat", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).isNullOrEmpty();

        ResponseEntity<String> otherGets = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/fromPoster/paul", HttpMethod.GET, null, String.class);
        assertThat(otherGets.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(otherGets.getBody()).read("$[*]")).size()).isEqualTo(2);
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteOthersCommentsIfNotAdmin() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/comments/deleteByPoster/kat", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/fromPoster/kat", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(getResponse.getBody()).read("$[*]")).size()).isEqualTo(3);

        ResponseEntity<String> otherGets = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/fromPoster/paul", HttpMethod.GET, null, String.class);
        assertThat(otherGets.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(otherGets.getBody()).read("$[*]")).size()).isEqualTo(2);
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteCommentsIfUnauthed() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .exchange("/comments/deleteByPoster/kat", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/fromPoster/kat", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(getResponse.getBody()).read("$[*]")).size()).isEqualTo(3);

        ResponseEntity<String> otherGets = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/fromPoster/paul", HttpMethod.GET, null, String.class);
        assertThat(otherGets.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(otherGets.getBody()).read("$[*]")).size()).isEqualTo(2);
    }

    @Test
    @DirtiesContext
    void shouldDeleteCommentsOnOwnPosts() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/deleteByPostUserPoster/kat", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromPostUserPoster/kat", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).isNullOrEmpty();

        ResponseEntity<String> otherGets = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromPostUserPoster/paul", HttpMethod.GET, null, String.class);
        assertThat(otherGets.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(otherGets.getBody()).read("$[*]")).size()).isEqualTo(2);
    }

    @Test
    @DirtiesContext
    void shouldDeleteCommentsOnOthersPostsIfAdmin() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/deleteByPostUserPoster/kat", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromPostUserPoster/kat", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).isNullOrEmpty();

        ResponseEntity<String> otherGets = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromPostUserPoster/paul", HttpMethod.GET, null, String.class);
        assertThat(otherGets.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(otherGets.getBody()).read("$[*]")).size()).isEqualTo(2);
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteCommentsOnOthersPostsIfNotAdmin() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/comments/deleteByPostUserPoster/kat", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromPostUserPoster/kat", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(getResponse.getBody()).read("$[*]")).size()).isEqualTo(3);

        ResponseEntity<String> otherGets = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromPostUserPoster/paul", HttpMethod.GET, null, String.class);
        assertThat(otherGets.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(otherGets.getBody()).read("$[*]")).size()).isEqualTo(2);
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteCommentsOnOthersPostsIfNotAuthed() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .exchange("/comments/deleteByPostUserPoster/kat", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromPostUserPoster/kat", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(getResponse.getBody()).read("$[*]")).size()).isEqualTo(3);

        ResponseEntity<String> otherGets = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromPostUserPoster/paul", HttpMethod.GET, null, String.class);
        assertThat(otherGets.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(otherGets.getBody()).read("$[*]")).size()).isEqualTo(2);
    }

    @Test
    @DirtiesContext
    void shouldDeleteCommentsOnOwnCatsPosts() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/deleteByPostCatPoster/4", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromCatPoster/4", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).isNullOrEmpty();

        ResponseEntity<String> otherGets = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromCatPoster/5", HttpMethod.GET, null, String.class);
        assertThat(otherGets.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(otherGets.getBody()).read("$[*]")).size()).isEqualTo(2);
    }

    @Test
    @DirtiesContext
    void shouldDeleteCommentsOnOthersCatsPostsIfAdmin() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/deleteByPostCatPoster/4", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromCatPoster/4", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).isNullOrEmpty();

        ResponseEntity<String> otherGets = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromCatPoster/5", HttpMethod.GET, null, String.class);
        assertThat(otherGets.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(otherGets.getBody()).read("$[*]")).size()).isEqualTo(2);
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteCommentsOnOthersCatsPostsIfNotAdmin() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/comments/deleteByPostCatPoster/4", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromCatPoster/4", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(getResponse.getBody()).read("$[*]")).size()).isEqualTo(3);
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteCommentsOnOthersCatsPostsIfNotAuthed() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .exchange("/comments/deleteByPostCatPoster/4", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromCatPoster/4", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(getResponse.getBody()).read("$[*]")).size()).isEqualTo(3);
    }

    @Test
    @DirtiesContext
    void shouldDeleteRepliesInOwnComment() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/comments/deleteByParentComment/3", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromReplyingTo/3", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).isNullOrEmpty();

        ResponseEntity<String> otherGets = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/fromPoster/kat", HttpMethod.GET, null, String.class);
        assertThat(otherGets.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(otherGets.getBody()).read("$[*]")).size()).isEqualTo(2); //one less than usual
    }

    @Test
    @DirtiesContext
    void shouldDeleteRepliesInOthersCommentIfAdmin() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/deleteByParentComment/3", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromReplyingTo/3", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).isNullOrEmpty();

        ResponseEntity<String> otherGets = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/fromPoster/kat", HttpMethod.GET, null, String.class);
        assertThat(otherGets.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(otherGets.getBody()).read("$[*]")).size()).isEqualTo(2); //one less than usual
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteRepliesInOthersCommentIfNotAdmin() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/comments/deleteByParentComment/3", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromReplyingTo/3", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> otherGets = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/fromPoster/kat", HttpMethod.GET, null, String.class);
        assertThat(otherGets.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(otherGets.getBody()).read("$[*]")).size()).isEqualTo(3); //not one less than usual this time
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteRepliesInOthersCommentIfNotAuthed() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .exchange("/comments/deleteByParentComment/3", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/getFromReplyingTo/3", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> otherGets = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/comments/fromPoster/kat", HttpMethod.GET, null, String.class);
        assertThat(otherGets.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((JSONArray)JsonPath.parse(otherGets.getBody()).read("$[*]")).size()).isEqualTo(3); //not one less than usual this time
    }

    @Test
    void unauthedShouldBeAbleToGetFromReplyingTo() {
        ResponseEntity<String> getResponse = restTemplate
                .exchange("/comments/getFromReplyingTo/3", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotEmpty();
    }

    @Test
    void anyoneShouldBeAbleToGetFromReplyingTo() {
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123") //doesnt own the replyingTo3 comments
                .exchange("/comments/getFromReplyingTo/3", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotEmpty();
    }

    @Test
    void shouldNotAllowDeleteByPosterInOtherHttpMethods() {
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos","admin") //doesnt own the replyingTo3 comments
                .exchange("/comments/deleteByPoster/kat", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(getResponse.getBody()).isNullOrEmpty();

        ResponseEntity<String> postResponse = restTemplate
                .withBasicAuth("legoaggelos","admin") //doesnt own the replyingTo3 comments
                .exchange("/comments/deleteByPoster/kat", HttpMethod.POST, null, String.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(postResponse.getBody()).isNullOrEmpty();

        ResponseEntity<String> putResponse = restTemplate
                .withBasicAuth("legoaggelos","admin") //doesnt own the replyingTo3 comments
                .exchange("/comments/deleteByPoster/kat", HttpMethod.PUT, null, String.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(putResponse.getBody()).isNullOrEmpty();
    }

    @Test
    void shouldNotAllowDeleteByParentCommentInOtherHttpMethods() {
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos","admin") //doesnt own the replyingTo3 comments
                .exchange("/comments/deleteByParentComment/3", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(getResponse.getBody()).isNullOrEmpty();

        ResponseEntity<String> postResponse = restTemplate
                .withBasicAuth("legoaggelos","admin") //doesnt own the replyingTo3 comments
                .exchange("/comments/deleteByParentComment/3", HttpMethod.POST, null, String.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(postResponse.getBody()).isNullOrEmpty();

        ResponseEntity<String> putResponse = restTemplate
                .withBasicAuth("legoaggelos","admin") //doesnt own the replyingTo3 comments
                .exchange("/comments/deleteByParentComment/3", HttpMethod.PUT, null, String.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(putResponse.getBody()).isNullOrEmpty();
    }

    @Test
    void shouldNotAllowDeleteByPostCatPosterInOtherHttpMethods() {
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos","admin") //doesnt own the replyingTo3 comments
                .exchange("/comments/deleteByPostCatPoster/4", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(getResponse.getBody()).isNullOrEmpty();

        ResponseEntity<String> postResponse = restTemplate
                .withBasicAuth("legoaggelos","admin") //doesnt own the replyingTo3 comments
                .exchange("/comments/deleteByPostCatPoster/4", HttpMethod.POST, null, String.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(postResponse.getBody()).isNullOrEmpty();

        ResponseEntity<String> putResponse = restTemplate
                .withBasicAuth("legoaggelos","admin") //doesnt own the replyingTo3 comments
                .exchange("/comments/deleteByPostCatPoster/4", HttpMethod.PUT, null, String.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(putResponse.getBody()).isNullOrEmpty();
    }

    @Test
    void shouldNotAllowDeleteByPostUserPosterInOtherHttpMethods() {
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos","admin") //doesnt own the replyingTo3 comments
                .exchange("/comments/deleteByPostUserPoster/kat", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(getResponse.getBody()).isNullOrEmpty();

        ResponseEntity<String> postResponse = restTemplate
                .withBasicAuth("legoaggelos","admin") //doesnt own the replyingTo3 comments
                .exchange("/comments/deleteByPostUserPoster/kat", HttpMethod.POST, null, String.class);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(postResponse.getBody()).isNullOrEmpty();

        ResponseEntity<String> putResponse = restTemplate
                .withBasicAuth("legoaggelos","admin") //doesnt own the replyingTo3 comments
                .exchange("/comments/deleteByPostUserPoster/kat", HttpMethod.PUT, null, String.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(putResponse.getBody()).isNullOrEmpty();
    }

    @Test
    void shouldNotAllowRegularUserToUseAdminGetFromCatPoster() {
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/comments/getFromCatPoster/4", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).isNullOrEmpty();
    }

    @Test
    void shouldNotAllowRegularUserToUseAdminGetFromUserPoster() {
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/comments/getFromPostUserPoster/kat", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).isNullOrEmpty();
    }
}
