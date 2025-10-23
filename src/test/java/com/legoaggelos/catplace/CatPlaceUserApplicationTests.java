package com.legoaggelos.catplace;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.legoaggelos.catplace.cats.Cat;
import com.legoaggelos.catplace.security.users.CatPlaceUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CatPlaceUserApplicationTests {
    @Autowired
    TestRestTemplate restTemplate;

    private static final Path testFile = Paths.get("4.jpg");

    @Test
    @DirtiesContext
    void shouldCreateNewUserWithNewPfp() throws IOException, SQLException {
        var testPfp = new SerialBlob(Files.readAllBytes(testFile));
        testPfp.truncate(500);
        CatPlaceUser newCatPlaceUserRequest = new CatPlaceUser("examplename", "exampleusername", testPfp, "example bio", "examplemail@gmail.com", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false);
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .postForEntity("/users", newCatPlaceUserRequest, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);


        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/users/exampleusername", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String username = documentContext.read("$.username");
        assertThat(username).isEqualTo("exampleusername");

        String displayName = documentContext.read("$.displayName");
        assertThat(displayName).isEqualTo("examplename");

        byte[] profilePicture = Base64.getDecoder().decode((String) documentContext.read("$.profilePicture"));
        assertThat(profilePicture).isEqualTo(testPfp.getBinaryStream().readAllBytes());

        String bio = documentContext.read("$.bio");
        assertThat(bio).isEqualTo("example bio");

        String email = documentContext.read("$.email");
        assertThat(email).isEqualTo("examplemail@gmail.com");

        boolean isAdmin = documentContext.read("$.admin");
        assertThat(isAdmin).isFalse();

        List<List<Long>> likedLists = List.of(documentContext.read("$.likedPosts"), documentContext.read("$.likedComments"), documentContext.read("$.likedReplies"));
        for (List<Long> likedList : likedLists) {
            assertThat(likedList).isEmpty();
        }
    }
    @Test
    public void canFindUser() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/users/paul", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String username = documentContext.read("$.username");
        assertThat(username).isEqualTo("paul");

        String email =  documentContext.read("$.email");
        assertThat(email).isEqualTo("example@gmail.com");

        String bio = documentContext.read("$.bio");
        assertThat(bio).isEqualTo("Owner of cats");

        boolean isAdmin = documentContext.read("$.admin");
        assertThat(isAdmin).isFalse();

        List<List<Long>> likedLists = List.of(documentContext.read("$.likedPosts"), documentContext.read("$.likedComments"), documentContext.read("$.likedReplies"));
        for (List<Long> likedList : likedLists) {
            assertThat(likedList).isEmpty();
        }
    }

    @Test
    void canUnauthenticatedUsersGetUser() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/users/paul", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldNotReturnAUserWhenUsingBadCredentials() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("BAD-USER", "abc123")
                .getForEntity("/users/paul", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        response = restTemplate
                .withBasicAuth("paul", "BAD-PASSWORD")
                .getForEntity("/users/paul", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DirtiesContext
    void shouldCreateNewUser() throws IOException {
        CatPlaceUser newCatPlaceUserRequest = new CatPlaceUser("examplename", "exampleusername", null, "example bio", "examplemail@gmail.com", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false);
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .postForEntity("/users", newCatPlaceUserRequest, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);


        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/users/exampleusername", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String username = documentContext.read("$.username");
        assertThat(username).isEqualTo("exampleusername");

        String displayName = documentContext.read("$.displayName");
        assertThat(displayName).isEqualTo("examplename");

        byte[] profilePicture = Base64.getDecoder().decode((String) documentContext.read("$.profilePicture"));
        assertThat(profilePicture).isEqualTo(Files.readAllBytes(testFile));

        String bio = documentContext.read("$.bio");
        assertThat(bio).isEqualTo("example bio");

        String email = documentContext.read("$.email");
        assertThat(email).isEqualTo("examplemail@gmail.com");

        boolean isAdmin = documentContext.read("$.admin");
        assertThat(isAdmin).isFalse();

        List<List<Long>> likedLists = List.of(documentContext.read("$.likedPosts"), documentContext.read("$.likedComments"), documentContext.read("$.likedReplies"));
        for (List<Long> likedList : likedLists) {
            assertThat(likedList).isEmpty();
        }
    }
    @Test
    @DirtiesContext
    void shouldCreateNewUserWhenUnauthenticated() {
        CatPlaceUser newCatPlaceUserRequest = new CatPlaceUser("examplename", "exampleusername", null, "example bio", "examplemail@gmail.com", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false);
        ResponseEntity<Void> createResponse = restTemplate
                .postForEntity("/users", newCatPlaceUserRequest, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);


        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/users/exampleusername", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String username = documentContext.read("$.username");
        assertThat(username).isEqualTo("exampleusername");
    }

    @Test
    @DirtiesContext
    void shouldNotCreateDuplicateUser() {
        CatPlaceUser newCatPlaceUserRequest = new CatPlaceUser("examplename", "kat", null, "example bio", "examplemail@gmail.com", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false);
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .postForEntity("/users", newCatPlaceUserRequest, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(createResponse.getHeaders().getOrEmpty("message").get(0)).isEqualTo("Username kat is already taken.");
    }

    @Test
    @DirtiesContext
    void shouldDeleteUser() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/users/kat", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Void> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/users/kat", Void.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotDeleteNonExistentUser() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/users/qwuFAHFUwuihhaidhui", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotDeleteOtherUser() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/users/legoaggelos", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldDeleteOtherUserWhenAdmin() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/users/kat", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Void> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/users/kat", Void.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotAllowAccessToUsersWhenNotAuthorized() {
        ResponseEntity<Void> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "BAD-PASSWORD")
                .getForEntity("/users/kat", Void.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        getResponse = restTemplate
                .withBasicAuth("BAD-USER", "admin")
                .getForEntity("/users/kat", Void.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DirtiesContext
    void shouldUpdateUser() {
        CatPlaceUser update = new CatPlaceUser("paul 2", "paul", null, "Owner of cats 2", "example@gmail.com 2");
        HttpEntity<CatPlaceUser> request = new HttpEntity<>(update);
        ResponseEntity<Void> putResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/users/paul", HttpMethod.PUT, request, Void.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/users/paul", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String username = documentContext.read("$.username");
        assertThat(username).isEqualTo("paul");

        String displayName = documentContext.read("$.displayName");
        assertThat(displayName).isEqualTo("paul 2");

        String bio = documentContext.read("$.bio");
        assertThat(bio).isEqualTo("Owner of cats 2");

        String email= documentContext.read("$.email");
        assertThat(email).isEqualTo("example@gmail.com 2");

        List<Long> likedPosts = documentContext.read("$.likedPosts");
        assertThat(likedPosts).isEmpty();

        boolean isAdmin = documentContext.read("$.admin");
        assertThat(isAdmin).isFalse();
    }

    @Test
    @DirtiesContext
    void shouldNotUpdateAnotherUser() {
        CatPlaceUser update = new CatPlaceUser("paul 2", "paul", null, "Owner of cats 2", "example@gmail.com 2");
        HttpEntity<CatPlaceUser> request = new HttpEntity<>(update);
        ResponseEntity<Void> putResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/users/paul", HttpMethod.PUT, request, Void.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/users/paul", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String username = documentContext.read("$.username");
        assertThat(username).isEqualTo("paul");

        String displayName = documentContext.read("$.displayName");
        assertThat(displayName).isEqualTo("paul");

        String bio = documentContext.read("$.bio");
        assertThat(bio).isEqualTo("Owner of cats");

        String email= documentContext.read("$.email");
        assertThat(email).isEqualTo("example@gmail.com");

        List<Long> likedPosts = documentContext.read("$.likedPosts");
        assertThat(likedPosts).isEmpty();

        boolean isAdmin = documentContext.read("$.admin");
        assertThat(isAdmin).isFalse();
    }

    @Test
    @DirtiesContext
    void shouldUpdateAnotherUserWhenAdmin() {
        CatPlaceUser update = new CatPlaceUser("paul 2", "paul", null, "Owner of cats 2", "example@gmail.com 2");
        HttpEntity<CatPlaceUser> request = new HttpEntity<>(update);
        ResponseEntity<Void> putResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/users/paul", HttpMethod.PUT, request, Void.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/users/paul", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        String username = documentContext.read("$.username");
        assertThat(username).isEqualTo("paul");

        String displayName = documentContext.read("$.displayName");
        assertThat(displayName).isEqualTo("paul 2");

        String bio = documentContext.read("$.bio");
        assertThat(bio).isEqualTo("Owner of cats 2");

        String email= documentContext.read("$.email");
        assertThat(email).isEqualTo("example@gmail.com 2");

        List<Long> likedPosts = documentContext.read("$.likedPosts");
        assertThat(likedPosts).isEmpty();

        boolean isAdmin = documentContext.read("$.admin");
        assertThat(isAdmin).isFalse();
    }

    @Test
    @DirtiesContext
    void shouldNotUpdateNonExistentUser() {
        CatPlaceUser update = new CatPlaceUser("paul 2", "paul", null, "Owner of cats 2", "example@gmail.com 2");
        HttpEntity<CatPlaceUser> request = new HttpEntity<>(update);
        ResponseEntity<Void> putResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/users/SUfTHEWuhtiwfaFHUIWgrfa", HttpMethod.PUT, request, Void.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/users/SUfTHEWuhtiwfaFHUIWgrfa", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(null);
    }
}
