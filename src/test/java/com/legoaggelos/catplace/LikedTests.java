package com.legoaggelos.catplace;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.legoaggelos.catplace.likehandling.LikedPost;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
public class LikedTests {
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void checkCorrectTableAccess() {
        ResponseEntity<String> postGet = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/likedPost/fromId/4", String.class);
        assertThat(postGet.getBody()).isNotNull();
        assertThat(postGet.getBody()).contains("postLikedId");
        postGet = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/likedComment/fromId/4", String.class);
        assertThat(postGet.getBody()).isNotNull();
        assertThat(postGet.getBody()).contains("commentLikedId");
    }

    @Test
    void shouldHaveAccessToLikedPostIfAdmin() {
        ResponseEntity<String> postGet = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/likedPost/fromId/4", String.class);
        assertThat(postGet.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(postGet.getBody());

        int id = documentContext.read("$.id");
        assertThat(id).isEqualTo(4);

        int postId = documentContext.read("$.postLikedId");
        assertThat(postId).isEqualTo(5);

        String liker = documentContext.read("$.username");
        assertThat(liker).isEqualTo("kat");
    }

    @Test
    void shouldNotHaveAccessToNonexistentLikedPostEvenIfAdmin() {
        ResponseEntity<String> postGet = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/likedPost/fromId/90", String.class);
        assertThat(postGet.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(postGet.getBody()).isNullOrEmpty();
    }

    @Test
    void shouldNotHaveAccessToLikedPostIfNotAdmin() {
        ResponseEntity<Void> postGet = restTemplate
                .getForEntity("/likedPost/fromId/4", Void.class);
        assertThat(postGet.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldGetIfLikedPostExistsFromIdAndOwnerIfAdmin() {
        ResponseEntity<Void> postGet = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/likedPost/fromId/5/user/paul", Void.class);
        assertThat(postGet.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldGetIfLikedPostExistsIfNotExistsFromIdAndOwnerIfAdmin() {
        ResponseEntity<Void> postGet = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/likedPost/fromId/4/user/legoaggelos", Void.class);
        assertThat(postGet.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        postGet = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/likedComment/fromId/12312/user/paul", Void.class);
        assertThat(postGet.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotHaveAccessToLikedPostFromIdAndOwnerIfNotAdmin() {
        ResponseEntity<Void> postGet = restTemplate
                .getForEntity("/likedPost/fromId/4/user/paul", Void.class);
        assertThat(postGet.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldNotHaveAccessToNonexistentLikedCommentIfAdmin() {
        ResponseEntity<String> postGet = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/likedComment/fromId/90", String.class);
        assertThat(postGet.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(postGet.getBody()).isNullOrEmpty();
    }

    @Test
    void shouldHaveAccessToLikedCommentIfAdmin() {
        ResponseEntity<String> postGet = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/likedComment/fromId/4", String.class);
        assertThat(postGet.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(postGet.getBody());

        int id = documentContext.read("$.id");
        assertThat(id).isEqualTo(4);

        int postId = documentContext.read("$.commentLikedId");
        assertThat(postId).isEqualTo(4);

        String liker = documentContext.read("$.username");
        assertThat(liker).isEqualTo("paul");
    }

    @Test
    void shouldNotHaveAccessToLikedCommentIfNotAdmin() {
        ResponseEntity<Void> postGet = restTemplate
                .getForEntity("/likedComment/fromId/4", Void.class);
        assertThat(postGet.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldGetIfLikedCommentExistsFromIdAndOwnerIfAdmin() {
        ResponseEntity<Void> postGet = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/likedComment/fromId/46/user/paul", Void.class);
        assertThat(postGet.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldGetIfLikedCommentExistsIfNotExistsFromIdAndOwnerIfAdmin() {
        ResponseEntity<Void> postGet = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/likedComment/fromId/46/user/legoaggelos", Void.class);
        assertThat(postGet.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        postGet = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/likedComment/fromId/12312/user/paul", Void.class);
        assertThat(postGet.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotHaveAccessToLikedFromIdAndOwnerCommentIfNotAdmin() {
        ResponseEntity<Void> postGet = restTemplate
                .getForEntity("/likedComment/fromId/46/user/paul", Void.class);
        assertThat(postGet.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldCheckIfOwnCommentLikeExists() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/likedComment/46", Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldCheckIfOwnPostLikeExists() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/likedPost/4", Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldCheckIfOwnCommentLikeExistsIfItDoesNot() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/likedComment/48", Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldCheckIfOwnPostLikeExistsIfItDoesNot() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/likedPost/3", Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    @Test
    void shouldDenyCommentPutRequest() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/likedComment/34", HttpMethod.PUT, new HttpEntity<>(new LikedPost(1L,"2",1L)), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
    @Test
    void shouldDenyPostPutRequest() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/likedPost/34", HttpMethod.PUT, new HttpEntity<>(new LikedPost(1L,"2",1L)), Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DirtiesContext
    void shouldLikeComment() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .postForEntity("/likedComment", 48L, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Void> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/likedComment/48", Void.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DirtiesContext
    void shouldLikePost() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .postForEntity("/likedPost", 2L, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Void> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/likedPost/2", Void.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldNotRelikeComment() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .postForEntity("/likedComment", 46L, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getHeaders().getOrEmpty("message").toString().replace('[', ' ').replace(']', ' ').trim()).isEqualTo("Comment 46 has already been liked by user kat.");

        ResponseEntity<Void> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/likedComment/46", Void.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldNotRelikePost() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .postForEntity("/likedPost", 5L, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getHeaders().getOrEmpty("message").toString().replace('[', ' ').replace(']', ' ').trim()).isEqualTo("Post 5 has already been liked by user kat.");

        ResponseEntity<Void> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/likedPost/5", Void.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DirtiesContext
    void shouldDeleteCommentLike() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/likedComment/46", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Void> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/likedComment/46", Void.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldDeletePostLike() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/likedPost/4", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Void> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/likedPost/4", Void.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteNonExistentCommentLike() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/likedComment/4632", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteNonExistentPostLike() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/likedPost/4134", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldDeleteOthersCommentLikeIfAdmin() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/likedComment/46/user/kat", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Void> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/likedComment/46", Void.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldDeleteOthersPostLikeIfAdmin() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/likedPost/4/user/kat", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Void> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/likedPost/4", Void.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteOthersCommentLikeIfNotAdmin() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/likedComment/46/user/kat", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<Void> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/likedComment/46", Void.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteOthersPostLikeIfNotAdmin() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/likedPost/4/user/kat", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<Void> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/likedPost/4", Void.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteCommentLikeIfNotAuthed() {
        ResponseEntity<Void> response = restTemplate
                .exchange("/likedComment/46/user/kat", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        response = restTemplate
                .exchange("/likedComment/46", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<Void> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/likedComment/46", Void.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DirtiesContext
    void shouldNotDeletePostLikeIfNotAuthed() {
        ResponseEntity<Void> response = restTemplate
                .exchange("/likedPost/4/user/kat", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        response = restTemplate
                .exchange("/likedPost/4", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<Void> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/likedPost/4", Void.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
