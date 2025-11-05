package com.legoaggelos.catplace;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.legoaggelos.catplace.cats.posts.Post;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
public class CatPostApplicationTests {
    @Autowired
    TestRestTemplate restTemplate;

    private static final Path testFile = Paths.get("img.png");

    protected static final OffsetDateTime sampleDate = OffsetDateTime.of(2025,4, 8, 2, 30, 30, 0, ZoneOffset.ofHours(0));


    @Test //while this is a test, this is mainly used for testing unapproved access behavior.
    @DirtiesContext
    void shouldDisapprovePost() {
        String uri = "/catposts/5";
        ResponseEntity<Post> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity(uri, Post.class);
        Post post5 = getResponse.getBody();
        assertThat(post5).isNotNull();

        Post newPost = new Post(5L, post5.image(), post5.likeCount(), post5.catOwner(), post5.userOwner(), post5.desc(), post5.uploadDate(), false);
        HttpEntity<Post> request = new HttpEntity<>(newPost);

        ResponseEntity<Void> putResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange(uri, HttpMethod.PUT, request, Void.class);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DirtiesContext
    void shouldNotBeAbleToGetOtherCatsUnapprovedPostsWhenGettingList() throws SQLException, IOException {
        shouldDisapprovePost();
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/catposts/fromCatId/5", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContextPage = JsonPath.parse(response.getBody());
        JSONArray page = documentContextPage.read("$[*]");

        assertThat(page.size()).isEqualTo(1); //cat 5 only has 1 approved post! one got disapproved!

        assertThat(((LinkedHashMap)page.get(0))/*safe because we asserted size is 1*/.getOrDefault("desc", "wrong")).isEqualTo("Cuter cat!!");
    }

    @Test
    @DirtiesContext
    void shouldNotBeAbleToGetOtherUsersUnapprovedPostsWhenGettingList() throws SQLException, IOException {
        shouldDisapprovePost();
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/catposts/fromOwnerId/paul", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContextPage = JsonPath.parse(response.getBody());
        JSONArray page = documentContextPage.read("$[*]");

        assertThat(page.size()).isEqualTo(2);

        assertThat(page.toJSONString()).doesNotContain("Cute cat!!");
    }

    @Test
    @DirtiesContext
    void shouldBeAbleToGetOtherCatsUnapprovedPostsWhenGettingListWhenAdmin() throws SQLException, IOException {
        shouldDisapprovePost();
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/catposts/fromCatId/5", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContextPage = JsonPath.parse(response.getBody());
        JSONArray page = documentContextPage.read("$[*]");

        assertThat(page.size()).isEqualTo(2);

        assertThat(page.toJSONString()).contains("Cute cat", "Cuter cat");
    }

    @Test
    @DirtiesContext
    void shouldBeAbleToGetOtherUsersUnapprovedPostsWhenGettingListWhenAdmin() throws SQLException, IOException {
        shouldDisapprovePost();
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/catposts/fromOwnerId/paul", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContextPage = JsonPath.parse(response.getBody());
        JSONArray page = documentContextPage.read("$[*]");

        assertThat(page.size()).isEqualTo(3);

        assertThat(page.toJSONString()).contains("Cute cat!!");
    }

    @Test
    @DirtiesContext
    void shouldBeAbleToGetOwnCatsUnapprovedPostsWhenGettingList() throws SQLException, IOException {
        shouldDisapprovePost();
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/catposts/fromCatId/5", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContextPage = JsonPath.parse(response.getBody());
        JSONArray page = documentContextPage.read("$[*]");

        assertThat(page.size()).isEqualTo(2);

        assertThat(page.toJSONString()).contains("Cute cat", "Cuter cat");
    }

    @Test
    @DirtiesContext
    void shouldBeAbleToGetOwnUnapprovedPostsWhenGettingList() throws SQLException, IOException {
        shouldDisapprovePost();
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/catposts/fromOwnerId/paul", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContextPage = JsonPath.parse(response.getBody());
        JSONArray page = documentContextPage.read("$[*]");

        assertThat(page.size()).isEqualTo(3);

        assertThat(page.toJSONString()).contains("Cute cat!!");
    }

    @Test
    @DirtiesContext
    void shouldNotBeAbleToGetOthersUnapprovedPost() throws SQLException, IOException {
        shouldDisapprovePost();
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/catposts/5", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNullOrEmpty();
    }

    @Test
    @DirtiesContext
    void shouldBeAbleToGetOwnUnapprovedPost() throws SQLException, IOException {
        shouldDisapprovePost();
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/catposts/5", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(5);

        var image = Base64.getDecoder().decode((String) documentContext.read("$.image"));
        assertThat(image).isEqualTo(Files.readAllBytes(testFile));

        Number catOwnerId = documentContext.read("$.catOwner");
        assertThat(catOwnerId).isEqualTo(5);

        String userOwnerId = documentContext.read("$.userOwner");
        assertThat(userOwnerId).isEqualTo("paul");

        String desc= documentContext.read("$.desc");
        assertThat(desc).isEqualTo("Cute cat!!");

        String uploadDate= documentContext.read("$.uploadDate");
        assertThat(uploadDate).isEqualTo("2025-04-08T02:30:30Z");

        Object isApproved = documentContext.read("$.isApproved");
        assertThat(isApproved).isEqualTo(false);
    }

    @Test
    void shouldBeAbleToGetPost() throws SQLException, IOException {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/catposts/5", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());

        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(5);

        var image = Base64.getDecoder().decode((String) documentContext.read("$.image"));
        assertThat(image).isEqualTo(Files.readAllBytes(testFile));

        Number catOwnerId = documentContext.read("$.catOwner");
        assertThat(catOwnerId).isEqualTo(5);

        String userOwnerId = documentContext.read("$.userOwner");
        assertThat(userOwnerId).isEqualTo("paul");

        String desc= documentContext.read("$.desc");
        assertThat(desc).isEqualTo("Cute cat!!");

        String uploadDate= documentContext.read("$.uploadDate");
        assertThat(uploadDate).isEqualTo("2025-04-08T02:30:30Z");

        Object isApproved = documentContext.read("$.isApproved");
        assertThat(isApproved).isEqualTo(true);
    }

    @Test
    void unauthorizedShouldBeAbleToGetPost() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/catposts/5", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldBeAbleToGetEachOthersPost() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/catposts/5", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void unauthenticatedShouldNotBeAbleToGetEachOthersPost() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "BAD-PASSWORD")
                .getForEntity("/catposts/5", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        response = restTemplate
                .withBasicAuth("BAD-USERNAME", "BAD-PASSWORD")
                .getForEntity("/catposts/5", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnPageFromCatOwnerId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/catposts/fromCatId/5", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(2);

        JSONArray names = documentContext.read("$..id");
        assertThat(names).containsExactly(5,2);

        JSONArray dates = documentContext.read("$..uploadDate");
        assertThat(dates).containsExactly("2025-04-08T02:30:30Z", "2025-04-08T01:30:30Z");
    }

    @Test
    void shouldReturnPageFromOwnerId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/catposts/fromOwnerId/paul", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(3);

        JSONArray names = documentContext.read("$..id");
        assertThat(names).containsExactly(3,5,2);

        JSONArray dates = documentContext.read("$..uploadDate");
        assertThat(dates).containsExactly("2025-04-09T02:30:30Z", "2025-04-08T02:30:30Z", "2025-04-08T01:30:30Z");
    }


    @Test
    void shouldReturnCustomPage() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/catposts/fromOwnerId/paul?page=0&size=1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(1);

        JSONArray names = documentContext.read("$..id");
        assertThat(names).containsExactly(3);

        JSONArray dates = documentContext.read("$..uploadDate");
        assertThat(dates).containsExactly("2025-04-09T02:30:30Z");
    }

    @Test
    void shouldReturnAllPostsFromPrincipal() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/catposts", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(3);

        JSONArray names = documentContext.read("$..id");
        assertThat(names).containsExactly(3,5,2);

        JSONArray dates = documentContext.read("$..uploadDate");
        assertThat(dates).containsExactly("2025-04-09T02:30:30Z", "2025-04-08T02:30:30Z", "2025-04-08T01:30:30Z");
    }

    @Test
    @DirtiesContext
    void shouldCreateNewPost() throws IOException, SQLException {
        SerialBlob testBlob = new SerialBlob(Files.readAllBytes(testFile));
        Post post = new Post(1L, testBlob, 0L, 5L, "paul", "Tall cat being tall", Instant.now().atOffset(sampleDate.getOffset()), false);
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .postForEntity("/catposts", post, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewPost = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity(locationOfNewPost, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        OffsetDateTime uploadTime = OffsetDateTime.parse(documentContext.read("$.uploadDate"));
        assertThat(-uploadTime.toEpochSecond()+Instant.now().atOffset(sampleDate.getOffset()).toEpochSecond()).isBetween(-1L, 60L /*up to 60 seconds time disparity is ok*/);

        int id = documentContext.read("$.id");
        assertThat(id).isEqualTo(1);

        byte[] image = Base64.getDecoder().decode((String)documentContext.read("$.image"));
        assertThat(image).isEqualTo(testBlob.getBinaryStream().readAllBytes());

        int likeCount = documentContext.read("$.likeCount");
        assertThat(likeCount).isEqualTo(0);

        int catOwner = documentContext.read("$.catOwner");
        assertThat(catOwner).isEqualTo(5);

        String userOwner = documentContext.read("$.userOwner");
        assertThat(userOwner).isEqualTo("paul");

        String desc = documentContext.read("$.desc");
        assertThat(desc).isEqualTo("Tall cat being tall");

        Object isApproved = documentContext.read("$.isApproved");
        assertThat(isApproved).isEqualTo(null);
    }

    @Test
    @DirtiesContext
    void regularUserShouldCreateNewPostWithoutOverridingUnoverridableDefaults() throws IOException, SQLException {
        SerialBlob testBlob = new SerialBlob(Files.readAllBytes(testFile));
        Post post = new Post(8L, testBlob, 25L, 5L, "kat", "Tall cat being tall", Instant.now().atOffset(sampleDate.getOffset()).minusYears(10), true);
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .postForEntity("/catposts", post, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewPost = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity(locationOfNewPost, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        OffsetDateTime uploadTime = OffsetDateTime.parse(documentContext.read("$.uploadDate"));
        assertThat(-uploadTime.toEpochSecond()+Instant.now().atOffset(sampleDate.getOffset()).toEpochSecond()).isBetween(-1L, 60L /*up to 60 seconds time disparity is ok*/);

        int id = documentContext.read("$.id");
        assertThat(id).isEqualTo(1);

        byte[] image = Base64.getDecoder().decode((String)documentContext.read("$.image"));
        assertThat(image).isEqualTo(testBlob.getBinaryStream().readAllBytes());

        int likeCount = documentContext.read("$.likeCount");
        assertThat(likeCount).isEqualTo(0);

        int catOwner = documentContext.read("$.catOwner");
        assertThat(catOwner).isEqualTo(5);

        String userOwner = documentContext.read("$.userOwner");
        assertThat(userOwner).isEqualTo("paul");

        String desc = documentContext.read("$.desc");
        assertThat(desc).isEqualTo("Tall cat being tall");

        Object isApproved = documentContext.read("$.isApproved");
        assertThat(isApproved).isEqualTo(null);
    }

    @Test
    @DirtiesContext
    void adminUserShouldCreateNewPostWithoutUnoverridableDefaultsExceptIsApproved() throws IOException, SQLException {
        SerialBlob testBlob = new SerialBlob(Files.readAllBytes(testFile));
        Post post = new Post(8L, testBlob, 25L, 5L, "kat", "Tall cat being tall", Instant.now().atOffset(sampleDate.getOffset()).minusYears(10), true);
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .postForEntity("/catposts", post, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewPost = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity(locationOfNewPost, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        OffsetDateTime uploadTime = OffsetDateTime.parse(documentContext.read("$.uploadDate"));
        assertThat(-uploadTime.toEpochSecond()+Instant.now().atOffset(sampleDate.getOffset()).toEpochSecond()).isBetween(-1L, 60L /*up to 60 seconds time disparity is ok*/);

        int id = documentContext.read("$.id");
        assertThat(id).isEqualTo(1);

        byte[] image = Base64.getDecoder().decode((String)documentContext.read("$.image"));
        assertThat(image).isEqualTo(testBlob.getBinaryStream().readAllBytes());

        int likeCount = documentContext.read("$.likeCount");
        assertThat(likeCount).isEqualTo(0);

        int catOwner = documentContext.read("$.catOwner");
        assertThat(catOwner).isEqualTo(5);

        String userOwner = documentContext.read("$.userOwner");
        assertThat(userOwner).isEqualTo("legoaggelos");

        String desc = documentContext.read("$.desc");
        assertThat(desc).isEqualTo("Tall cat being tall");

        boolean isApproved = documentContext.read("$.isApproved");
        assertThat(isApproved).isTrue();
    }

    @Test
    @DirtiesContext
    void unauthorizedShouldNotBeAbleToCreatePost() throws IOException, SQLException {
        SerialBlob testBlob = new SerialBlob(Files.readAllBytes(testFile));
        Post post = new Post(8L, testBlob, 25L, 5L, "kat", "Tall cat being tall", Instant.now().atOffset(sampleDate.getOffset()), true);
        ResponseEntity<Void> response = restTemplate
                .postForEntity("/catposts", post, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/catposts", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).isBlank();
    }

    @Test
    @DirtiesContext
    @Sql("/delete-comments-posts.sql")
    void shouldDeletePost() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/5", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/5", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/2"/*Paul's other post*/, HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/4"/*cat's other post*/, HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteNonExistentPost() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/catposts/513374", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteNonExistentUsersPosts() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/catposts/fromOwnerId/waufhuyafygyf", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteNonExistentCatsPosts() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/catposts/fromCatId/814381", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    @Sql("/delete-comments-posts.sql")
    void shouldDeletePostOthersPostIfAdmin() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/catposts/5", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/5", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/2"/*Paul's other post*/, HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/4"/*cat's other post*/, HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DirtiesContext
    void shouldNotDeletePostOthersPostIfNotAdmin() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/catposts/5", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/5", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DirtiesContext
    @Sql("/delete-comments-posts.sql")
    void shouldDeleteAllPostsByUser() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/fromOwnerId/paul", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/fromOwnerId/paul", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/4"/*cat's other post*/, HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DirtiesContext
    @Sql("/delete-comments-posts.sql")
    void shouldDeleteAllPostsByOtherUserIfAdmin() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/catposts/fromOwnerId/paul", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/fromOwnerId/paul", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/4"/*cat's other post*/, HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldNotDeleteAllPostsByOtherUserIfNotAdmin() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/catposts/fromOwnerId/paul", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/fromOwnerId/paul", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().length()).isGreaterThan(10000); //def over 10k chars, if not something is wrong
    }

    @Test
    @DirtiesContext
    @Sql("/delete-comments-posts.sql")
    void shouldDeleteAllPostsByCat() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/fromCatId/5", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/fromCatId/5", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/fromCatId/3"/*paul's other cat*/, HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DirtiesContext
    @Sql("/delete-comments-posts.sql")
    void shouldDeleteAllPostsByCatByOtherUserIfAdmin() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/catposts/fromCatId/5", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/fromCatId/5", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/fromCatId/3"/*paul's other cat*/, HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteAllPostsByCatByOtherUserIfNotAdmin() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/catposts/fromCatId/5", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/fromCatId/5", HttpMethod.GET, null, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().length()).isGreaterThan(10000); //def over 10k chars, if not something is wrong
    }

    @Test
    @DirtiesContext
    void shouldUpdateOnlyDescIfOwner() throws IOException, SQLException {
        byte[] bytes = new byte[] {1,2,3,4,5,6,7,1,2,3,4,45,65,53};
        SerialBlob newImage = new SerialBlob(bytes);
        Post newPost = new Post(4L, newImage, 2L,4L, "kat", "Ugly cat", Instant.now().atOffset(sampleDate.getOffset()), false);
        HttpEntity<Post> request = new HttpEntity<>(newPost);
        ResponseEntity<Void> updateResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/catposts/5", HttpMethod.PUT, request, Void.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/catposts/5", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        Integer id = documentContext.read("$.id");
        assertThat(id).isEqualTo(5);

        Integer likeCount = documentContext.read("$.likeCount");
        assertThat(likeCount).isEqualTo(0);

        Integer catOwner = documentContext.read("$.catOwner");
        assertThat(catOwner).isEqualTo(5);

        byte[] image = Base64.getDecoder().decode((String)documentContext.read("$.image"));
        assertThat(image).isEqualTo(Files.readAllBytes(testFile));

        String userOwner = documentContext.read("$.userOwner");
        assertThat(userOwner).isEqualTo("paul");

        String desc = documentContext.read("$.desc");
        assertThat(desc).isEqualTo("Ugly cat"); //only one that should be updated in this case

        String uploadDate = documentContext.read("$.uploadDate");
        assertThat(uploadDate).isEqualTo(sampleDate.toString());

        boolean isApproved = documentContext.read("$.isApproved");
        assertThat(isApproved).isEqualTo(true);
    }

    @Test
    @DirtiesContext
    void shouldUpdateOnlyLikeIfNonOwnerNonAdmin() throws IOException, SQLException {
        byte[] bytes = new byte[] {1,2,3,4,5,6,7,1,2,3,4,45,65,53};
        SerialBlob newImage = new SerialBlob(bytes);
        Post newPost = new Post(4L, newImage,1L, 4L, "kat", "Ugly cat", Instant.now().atOffset(sampleDate.getOffset()), false);
        HttpEntity<Post> request = new HttpEntity<>(newPost);
        ResponseEntity<Void> updateResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/catposts/5", HttpMethod.PUT, request, Void.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/catposts/5", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        Integer id = documentContext.read("$.id");
        assertThat(id).isEqualTo(5);

        Integer likeCount = documentContext.read("$.likeCount");
        assertThat(likeCount).isEqualTo(1);

        Integer catOwner = documentContext.read("$.catOwner");
        assertThat(catOwner).isEqualTo(5);

        byte[] image = Base64.getDecoder().decode((String)documentContext.read("$.image"));
        assertThat(image).isEqualTo(Files.readAllBytes(testFile));

        String userOwner = documentContext.read("$.userOwner");
        assertThat(userOwner).isEqualTo("paul");

        String desc = documentContext.read("$.desc");
        assertThat(desc).isEqualTo("Cute cat!!"); //should not be changed if not owner!

        String uploadDate = documentContext.read("$.uploadDate");
        assertThat(uploadDate).isEqualTo(sampleDate.toString());

        boolean isApproved = documentContext.read("$.isApproved");
        assertThat(isApproved).isEqualTo(true);
    }

    @Test
    @DirtiesContext
    void shouldNotUpdateLikeTooMuchIfNonOwnerNonAdmin() throws IOException, SQLException {
        byte[] bytes = new byte[] {1,2,3,4,5,6,7,1,2,3,4,45,65,53};
        SerialBlob newImage = new SerialBlob(bytes);
        Post newPost = new Post(4L, newImage,20L, 4L, "kat", "Ugly cat", Instant.now().atOffset(sampleDate.getOffset()), true);
        HttpEntity<Post> request = new HttpEntity<>(newPost);
        ResponseEntity<Void> updateResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/catposts/5", HttpMethod.PUT, request, Void.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/catposts/5", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        Integer likeCount = documentContext.read("$.likeCount");
        assertThat(likeCount).isEqualTo(0);
    }

    @Test
    @DirtiesContext
    void shouldNotUpdateLikeToNegative() throws IOException, SQLException {
        byte[] bytes = new byte[] {1,2,3,4,5,6,7,1,2,3,4,45,65,53};
        SerialBlob newImage = new SerialBlob(bytes);
        Post newPost = new Post(4L, newImage,-1L, 4L, "kat", "Ugly cat", Instant.now().atOffset(sampleDate.getOffset()), true);
        HttpEntity<Post> request = new HttpEntity<>(newPost);
        ResponseEntity<Void> updateResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/catposts/5", HttpMethod.PUT, request, Void.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/catposts/5", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        Integer likeCount = documentContext.read("$.likeCount");
        assertThat(likeCount).isEqualTo(0);
    }

    @Test
    @DirtiesContext
    void shouldUpdateOnlyDescIsApprovedAndLikeCountIfAdmin() throws IOException, SQLException {
        byte[] bytes = new byte[] {1,2,3,4,5,6,7,1,2,3,4,45,65,53};
        SerialBlob newImage = new SerialBlob(bytes);
        Post newPost = new Post(4L, newImage, 2L,4L, "kat", "Ugly cat", Instant.now().atOffset(sampleDate.getOffset()), false);
        HttpEntity<Post> request = new HttpEntity<>(newPost);
        ResponseEntity<Void> updateResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/catposts/5", HttpMethod.PUT, request, Void.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/catposts/5", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        Integer id = documentContext.read("$.id");
        assertThat(id).isEqualTo(5);

        Integer likeCount = documentContext.read("$.likeCount");
        assertThat(likeCount).isEqualTo(2);

        Integer catOwner = documentContext.read("$.catOwner");
        assertThat(catOwner).isEqualTo(5);

        byte[] image = Base64.getDecoder().decode((String)documentContext.read("$.image"));
        assertThat(image).isEqualTo(Files.readAllBytes(testFile));

        String userOwner = documentContext.read("$.userOwner");
        assertThat(userOwner).isEqualTo("paul");

        String desc = documentContext.read("$.desc");
        assertThat(desc).isEqualTo("Cute cat!!");

        String uploadDate = documentContext.read("$.uploadDate");
        assertThat(Instant.now().getEpochSecond()-OffsetDateTime.parse(uploadDate).toEpochSecond()).isLessThan(60);

        Object isApproved = documentContext.read("$.isApproved");
        assertThat(isApproved).isEqualTo(false);
    }

    @Test
    @DirtiesContext
    void shouldIgnoreUpdateBoundsIfAdmin() throws IOException, SQLException {
        byte[] bytes = new byte[] {1,2,3,4,5,6,7,1,2,3,4,45,65,53};
        SerialBlob newImage = new SerialBlob(bytes);
        Post newPost = new Post(4L, newImage, -22L,4L, "kat", "Ugly cat", Instant.now().atOffset(sampleDate.getOffset()), true);
        HttpEntity<Post> request = new HttpEntity<>(newPost);
        ResponseEntity<Void> updateResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/catposts/5", HttpMethod.PUT, request, Void.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/catposts/5", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        Integer likeCount = documentContext.read("$.likeCount");
        assertThat(likeCount).isEqualTo(-22);
    }

    @Test
    @DirtiesContext
    void shouldNotUpdateIfUnauthorized() throws IOException, SQLException {
        byte[] bytes = new byte[] {1,2,3,4,5,6,7,1,2,3,4,45,65,53};
        SerialBlob newImage = new SerialBlob(bytes);
        Post newPost = new Post(4L, newImage, 2L,4L, "kat", "Ugly cat", Instant.now().atOffset(sampleDate.getOffset()), true);
        HttpEntity<Post> request = new HttpEntity<>(newPost);
        ResponseEntity<Void> updateResponse = restTemplate
                .exchange("/catposts/5", HttpMethod.PUT, request, Void.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/catposts/5", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        Integer id = documentContext.read("$.id");
        assertThat(id).isEqualTo(5);

        Integer likeCount = documentContext.read("$.likeCount");
        assertThat(likeCount).isEqualTo(0);

        Integer catOwner = documentContext.read("$.catOwner");
        assertThat(catOwner).isEqualTo(5);

        byte[] image = Base64.getDecoder().decode((String)documentContext.read("$.image"));
        assertThat(image).isEqualTo(Files.readAllBytes(testFile));

        String userOwner = documentContext.read("$.userOwner");
        assertThat(userOwner).isEqualTo("paul");

        String desc = documentContext.read("$.desc");
        assertThat(desc).isEqualTo("Cute cat!!");

        String uploadDate = documentContext.read("$.uploadDate");
        assertThat(uploadDate).isEqualTo(sampleDate.toString());

        Object isApproved = documentContext.read("$.isApproved");
        assertThat(isApproved).isEqualTo(true);
    }
}
