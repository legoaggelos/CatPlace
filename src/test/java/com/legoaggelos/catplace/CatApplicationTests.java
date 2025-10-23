package com.legoaggelos.catplace;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.LinkedHashMap;

import com.legoaggelos.catplace.cats.Cat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

import javax.sql.rowset.serial.SerialBlob;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatApplicationTests {
	@Autowired
    TestRestTemplate restTemplate;
    private static final Path testFile = Paths.get("4.jpg");
    

    protected static final OffsetDateTime sampleDate = OffsetDateTime.of(2025,4, 8, 2, 30, 30, 0, ZoneOffset.ofHours(0));

    @Test
    void shouldReturnACatWhenDataIsSaved() throws IOException {
        shouldCreateANewCatAndHaveDefaultPfp();
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/cats/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(1);

        String name = documentContext.read("$.name");
        assertThat(name).isEqualTo("pekos");
        
        OffsetDateTime dateOfBirth = OffsetDateTime.parse(documentContext.read("$.dateOfBirth"));
        assertThat(dateOfBirth).isEqualTo(sampleDate);

        String bio = documentContext.read("$.bio");
        assertThat(bio).isEqualTo("random");

        var x = documentContext.read("$.profilePicture");
        assertThat(x).isNotNull();
        assertThat((Base64.getDecoder().decode((String) x))).isEqualTo(Files.readAllBytes(testFile));
    }

    @Test
    void shouldReturnACatWhenDataIsSavedAndTheyDoNotOwnTheCat() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/cats/5", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(5);

        String name = documentContext.read("$.name");
        assertThat(name).isEqualTo("psilos");
        
        String dateOfBirth = documentContext.read("$.dateOfBirth");
        assertThat(dateOfBirth).isEqualTo(null);

        String pfp = documentContext.read("$.profilePicture");
        assertThat(pfp).isEqualTo(null);
        /*assertThat(x).isNotNull(); excluded because it needs to be created via code for the default to work, not the default data
        assertThat((Base64.getDecoder().decode((String) x))).isEqualTo(Files.readAllBytes(testFile));*/
    }
    @Test
    void shouldNotReturnACatWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/cats/1021300", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }
    @Test
    void shouldReturnAllCatsWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/cats", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        int catCount = documentContext.read("$.length()");
        assertThat(catCount).isEqualTo(3);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(5,6,3);

        JSONArray names = documentContext.read("$..name");
        assertThat(names).containsExactlyInAnyOrder("psilos","kontos","mesos");
        
        JSONArray dateOfBirth = documentContext.read("$..dateOfBirth");
        assertThat(dateOfBirth).containsExactlyInAnyOrder(null, null, null);

        JSONArray x = documentContext.read("$..profilePicture");
        assertThat(x).isNotNull();
        //assertThat(x).containsExactlyInAnyOrder(Files.readAllBytes(testFile)); excluded because it needs to be created via code for the default to work, not the default data
    }

    @Test
    void shouldReturnAPageOfCats() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/cats?page=0&size=1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnASortedPageOfCats() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/cats?page=0&size=1&sort=name,desc", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray read = documentContext.read("$[*]");
        assertThat(read.size()).isEqualTo(1);

        String name = documentContext.read("$[0].name");
        assertThat(name).isEqualTo("psilos");
    }

    @Test
    void shouldReturnASortedPageOfCatsWithNoParametersAndUseDefaultValues() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/cats", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(3);

        JSONArray names = documentContext.read("$..name");
        assertThat(names).containsExactly("kontos","mesos","psilos");
    }

    @Test
    void shouldNotReturnACatWhenUsingBadCredentials() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("BAD-USER", "abc123")
                .getForEntity("/cats/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        response = restTemplate
                .withBasicAuth("paul", "BAD-PASSWORD")
                .getForEntity("/cats/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldNotRejectUsersWhoAreNotCatOwners() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("hank-owns-no-cats", "qrs456")
                .getForEntity("/cats/4", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldAllowEveryoneToSeeCats() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("/cats/4", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldAllowAccessToCatsTheyDoNotOwn() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/cats/4", String.class); // kat's data
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldNotCreateANewCatWhenUserIsNotAuthenticated() throws IOException {
        Cat newCat = new Cat(null, "pekos", sampleDate, null, null, "random", true);
        ResponseEntity<Void> createResponse = restTemplate
                .postForEntity("/cats", newCat, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
    @Test
    @DirtiesContext
    void shouldCreateANewCatAndHaveNewPfp() throws IOException, SQLException {
        var testPfp = new SerialBlob(Files.readAllBytes(testFile));
        testPfp.truncate(500);
        Cat newCat = new Cat(null, "pekos", sampleDate, null, testPfp, "random", true);
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .postForEntity("/cats", newCat, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewCat = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity(locationOfNewCat, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        OffsetDateTime dateOfBirth = OffsetDateTime.parse(documentContext.read("$.dateOfBirth"));
        String name = documentContext.read("$.name");
        String bio = documentContext.read("$.bio");
        byte[] pfp = Base64.getDecoder().decode((String) documentContext.read("$.profilePicture"));

        assertThat(pfp).isEqualTo(testPfp.getBinaryStream().readAllBytes());
        assertThat(bio).isEqualTo("random");
        assertThat(id).isNotNull();
        assertThat(dateOfBirth).isEqualTo(sampleDate);
        assertThat(name).isEqualTo("pekos");
    }

    @Test
    @DirtiesContext
    void shouldCreateANewCatAndHaveDefaultPfp() throws IOException {
        Cat newCat = new Cat(null, "pekos", sampleDate, null, null, "random", true);
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .postForEntity("/cats", newCat, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
       
        URI locationOfNewCat = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity(locationOfNewCat, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        OffsetDateTime dateOfBirth = OffsetDateTime.parse(documentContext.read("$.dateOfBirth"));
        String name = documentContext.read("$.name");
        String bio = documentContext.read("$.bio");
        byte[] pfp = Base64.getDecoder().decode((String) documentContext.read("$.profilePicture"));

        assertThat(pfp).isEqualTo(Files.readAllBytes(testFile));
        assertThat(bio).isEqualTo("random");
        assertThat(id).isNotNull();
        assertThat(dateOfBirth).isEqualTo(sampleDate);
        assertThat(name).isEqualTo("pekos");
    }

    @Test
    @DirtiesContext
    void shouldCreateAndKillANewCat() {
        Cat newCat = new Cat(null, "pekos", sampleDate.minusYears(50), null, null, "random", true);
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .postForEntity("/cats", newCat, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewCat = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity(locationOfNewCat, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        boolean isAlive = documentContext.read("$.isAlive");

        assertThat(isAlive).isFalse();
    }

    @Test
    @DirtiesContext
    void shouldCreateAndNotKillANewCatIfAdmin() {
        Cat newCat = new Cat(null, "pekos", sampleDate.minusYears(50), null, null, "random", true);
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .postForEntity("/cats", newCat, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewCat = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity(locationOfNewCat, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        boolean isAlive = documentContext.read("$.isAlive");

        assertThat(isAlive).isTrue();
    }

    @Test
    void shouldNotUpdateACatWhenUserIsNotAuthenticated() {
        Cat catUpdate = new Cat(null, "mesos v2", null,null, null, "average v2", true);
        HttpEntity<Cat> request = new HttpEntity<>(catUpdate);
        ResponseEntity<Void> createResponse = restTemplate
                .exchange("/cats/5", HttpMethod.PUT, request, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DirtiesContext
    void shouldUpdateAnExistingCat() throws IOException, SQLException {
        shouldCreateANewCatAndHaveDefaultPfp();
        SerialBlob samplePfp = new SerialBlob(Files.readAllBytes(Paths.get("img.png")));
        OffsetDateTime newDate = sampleDate.minusHours(2);
    	Cat catUpdate = new Cat(null, "mesos v2", newDate,null, samplePfp, "average v2", true);
    	HttpEntity<Cat> request = new HttpEntity<>(catUpdate);
        assertThat(request.getBody().profilePicture()).isNotNull();
        assertThat(request.getBody().dateOfBirth()).isNotNull();

        ResponseEntity<String> responsePage = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/cats", String.class);
        assertThat(responsePage.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContextPage = JsonPath.parse(responsePage.getBody());
        JSONArray page = documentContextPage.read("$[*]");
        System.out.println(123456);
        long[] idArr = {5};
        page.forEach(catJSON ->
        {
            if (!(catJSON instanceof LinkedHashMap<?,?>)) {
                return;
            }
            if (((LinkedHashMap)catJSON).getOrDefault("name", null).equals("pekos")) {
                idArr[0] = (int) ((LinkedHashMap)catJSON).getOrDefault("id",5);
            }
        });
        String requestString = "/cats/" + idArr[0];
    	ResponseEntity<Void> response = restTemplate
    			.withBasicAuth("paul", "abc123")
    			.exchange(requestString, HttpMethod.PUT, request, Void.class);
    	assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    	ResponseEntity<String> getResponse = restTemplate.withBasicAuth("paul", "abc123")
    			.getForEntity(requestString, String.class);
    	assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    	DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
    	
    	Number id = documentContext.read("$.id");
    	assertThat(id).isEqualTo((int)idArr[0]);

    	OffsetDateTime dateOfBirth = OffsetDateTime.parse(documentContext.read("$.dateOfBirth"));
    	assertThat(dateOfBirth).isEqualTo(newDate); //can update date of birth
    	
    	String name = documentContext.read("$.name");
    	assertThat(name).isEqualTo("mesos v2");

        String bio = documentContext.read("$.bio");
        assertThat(bio).isEqualTo("average v2");

        SerialBlob pfp = new SerialBlob(Base64.getDecoder().decode((String) documentContext.read("$.profilePicture")));

        assertThat(pfp.getBinaryStream().readAllBytes()).isEqualTo(samplePfp.getBinaryStream().readAllBytes());

        boolean isAlive = documentContext.read("$.isAlive");
        if (OffsetDateTime.now().getYear()-sampleDate.getYear()<30) {
            assertThat(isAlive).isTrue();
        } else {
            assertThat(isAlive).isFalse();
        }
    }

    @Test
    @DirtiesContext
    void shouldUpdateAnExistingCatWholeyWhenAdmin() throws IOException, SQLException {
        shouldCreateANewCatAndHaveDefaultPfp();
        SerialBlob samplePfp = new SerialBlob(Files.readAllBytes(Paths.get("img.png")));
        OffsetDateTime newDate = sampleDate.minusYears(35);
        Cat catUpdate = new Cat(null, "mesos v2", newDate,null, samplePfp, "average v2", true);
        HttpEntity<Cat> request = new HttpEntity<>(catUpdate);
        assertThat(request.getBody().profilePicture()).isNotNull();
        assertThat(request.getBody().dateOfBirth()).isNotNull();

        ResponseEntity<String> responsePage = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .getForEntity("/cats/fromOwner/paul", String.class);
        assertThat(responsePage.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContextPage = JsonPath.parse(responsePage.getBody());
        JSONArray page = documentContextPage.read("$[*]");
        System.out.println(123456);
        long[] idArr = {5};
        page.forEach(catJSON ->
        {
            if (!(catJSON instanceof LinkedHashMap<?,?>)) {
                return;
            }
            if (((LinkedHashMap)catJSON).getOrDefault("name", null).equals("pekos")) {
                idArr[0] = (int) ((LinkedHashMap)catJSON).getOrDefault("id",5);
            }
        });
        String requestString = "/cats/" + idArr[0];
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange(requestString, HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate.withBasicAuth("paul", "abc123")
                .getForEntity(requestString, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo((int)idArr[0]);

        OffsetDateTime dateOfBirth = OffsetDateTime.parse(documentContext.read("$.dateOfBirth"));
        assertThat(dateOfBirth).isEqualTo(newDate); //want to update date of birth

        String name = documentContext.read("$.name");
        assertThat(name).isEqualTo("mesos v2");

        String bio = documentContext.read("$.bio");
        assertThat(bio).isEqualTo("average v2");

        SerialBlob pfp = new SerialBlob(Base64.getDecoder().decode((String) documentContext.read("$.profilePicture")));

        assertThat(pfp.getBinaryStream().readAllBytes()).isEqualTo(samplePfp.getBinaryStream().readAllBytes());

        boolean isAlive = documentContext.read("$.isAlive");
        assertThat(isAlive).isTrue();

    }

    @Test
    @DirtiesContext
    void shouldUpdateAndKillAnExistingCatUponRequest() throws IOException, SQLException {
        SerialBlob samplePfp = new SerialBlob(Files.readAllBytes(Paths.get("img.png")));
        OffsetDateTime newDate = sampleDate.minusHours(2);
        Cat catUpdate = new Cat(null, "mesos v2", newDate,null, samplePfp, "average v2", false);
        HttpEntity<Cat> request = new HttpEntity<>(catUpdate);
        assertThat(request.getBody().profilePicture()).isNotNull();
        assertThat(request.getBody().dateOfBirth()).isNotNull();

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/cats/5", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate.withBasicAuth("paul", "abc123")
                .getForEntity("/cats/5", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(5);

        String dateOfBirth = documentContext.read("$.dateOfBirth");
        assertThat(dateOfBirth).isEqualTo(newDate.toString());

        String name = documentContext.read("$.name");
        assertThat(name).isEqualTo("mesos v2");

        String bio = documentContext.read("$.bio");
        assertThat(bio).isEqualTo("average v2");

        SerialBlob pfp = new SerialBlob(Base64.getDecoder().decode((String) documentContext.read("$.profilePicture")));

        assertThat(pfp.getBinaryStream().readAllBytes()).isEqualTo(samplePfp.getBinaryStream().readAllBytes());

        boolean isAlive = documentContext.read("$.isAlive");
        assertThat(isAlive).isFalse();
    }

    @Test
    @DirtiesContext
    void shouldUpdateAndKillAnExistingCatThatIsOld() throws IOException, SQLException {
        SerialBlob samplePfp = new SerialBlob(Files.readAllBytes(Paths.get("img.png")));
        OffsetDateTime newDate = sampleDate.minusYears(35);
        Cat catUpdate = new Cat(null, "mesos v2", newDate,null, samplePfp, "average v2", true);
        HttpEntity<Cat> request = new HttpEntity<>(catUpdate);
        assertThat(request.getBody().profilePicture()).isNotNull();
        assertThat(request.getBody().dateOfBirth()).isNotNull();

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/cats/5", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate.withBasicAuth("paul", "abc123")
                .getForEntity("/cats/5", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(5);

        String dateOfBirth = documentContext.read("$.dateOfBirth");
        assertThat(dateOfBirth).isEqualTo(newDate.toString());

        String name = documentContext.read("$.name");
        assertThat(name).isEqualTo("mesos v2");

        String bio = documentContext.read("$.bio");
        assertThat(bio).isEqualTo("average v2");

        SerialBlob pfp = new SerialBlob(Base64.getDecoder().decode((String) documentContext.read("$.profilePicture")));

        assertThat(pfp.getBinaryStream().readAllBytes()).isEqualTo(samplePfp.getBinaryStream().readAllBytes());

        boolean isAlive = documentContext.read("$.isAlive");
        assertThat(isAlive).isFalse();
    }

    @Test
    @DirtiesContext
    void shouldUpdateAndNotKillAnExistingCatThatIsOldWhenAdmin() throws IOException, SQLException {
        SerialBlob samplePfp = new SerialBlob(Files.readAllBytes(Paths.get("img.png")));
        OffsetDateTime newDate = sampleDate.minusYears(35);
        Cat catUpdate = new Cat(null, "mesos v2", newDate,null, samplePfp, "average v2", true);
        HttpEntity<Cat> request = new HttpEntity<>(catUpdate);
        assertThat(request.getBody().profilePicture()).isNotNull();
        assertThat(request.getBody().dateOfBirth()).isNotNull();

        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/cats/5", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate.withBasicAuth("paul", "abc123")
                .getForEntity("/cats/5", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(5);

        String dateOfBirth = documentContext.read("$.dateOfBirth");
        assertThat(dateOfBirth).isEqualTo(newDate.toString());

        String name = documentContext.read("$.name");
        assertThat(name).isEqualTo("mesos v2");

        String bio = documentContext.read("$.bio");
        assertThat(bio).isEqualTo("average v2");

        SerialBlob pfp = new SerialBlob(Base64.getDecoder().decode((String) documentContext.read("$.profilePicture")));

        assertThat(pfp.getBinaryStream().readAllBytes()).isEqualTo(samplePfp.getBinaryStream().readAllBytes());

        boolean isAlive = documentContext.read("$.isAlive");
        assertThat(isAlive).isTrue();
    }

    @Test
    void shouldNotUpdateACatThatDoesNotExist() {
        Cat unknownCat = new Cat(null, "mesos v2",null,null, null, null, true);
        HttpEntity<Cat> request = new HttpEntity<>(unknownCat);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/cats/99999", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotUpdateACatThatIsOwnedBySomeoneElse() {
        Cat katsCat = new Cat(null, "mesos v2",null,null, null, null, true);
        HttpEntity<Cat> request = new HttpEntity<>(katsCat);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/cats/4", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    @Test
    @DirtiesContext
    void shouldDeleteAnExistingCat() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/cats/5", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        ResponseEntity<String> getResponse = restTemplate
        		.withBasicAuth("paul", "abc123")
                .getForEntity("/cats/5", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    @Test
    void shouldNotDeleteACatThatDoesNotExist() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/cats/99999", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotDeleteCatsFromOwnerThatDoesNotExist() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/cats/fromOwner/safafgyhasfhjashf", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotGetCatsFromOwnerThatDoesNotExist() {
        ResponseEntity<Void> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/cats/fromOwner/safafgyhasfhjashf", HttpMethod.GET, null, Void.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotDeleteACatWhenNotAuthenticated() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .exchange("/cats/4", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/cats/4", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    @Test
    void shouldNotAllowDeletionOfCatsTheyDoNotOwn() {
   
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/cats/4", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ResponseEntity<String> getResponse = restTemplate
        .withBasicAuth("kat", "xyz789")
        .getForEntity("/cats/4", String.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DirtiesContext
    void shouldAllowDeletionOfCatsTheyDoNotOwnWhenAdmin() {

        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/cats/4", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/cats/4", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldDeleteAllUsersCats() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .exchange("/cats/fromOwner/paul", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/cats", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldDeleteAllUsersCatsIfAdmin() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("legoaggelos", "admin")
                .exchange("/cats/fromOwner/paul", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/cats", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldNotDeleteAllUsersCatsIfNotAdmin() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .exchange("/cats/fromOwner/paul", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/cats", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnAllCatsFromCertainUserWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paul", "abc123")
                .getForEntity("/cats/fromOwner/paul", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        int catCount = documentContext.read("$.length()");
        assertThat(catCount).isEqualTo(3);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(5,6,3);

        JSONArray names = documentContext.read("$..name");
        assertThat(names).containsExactlyInAnyOrder("psilos","kontos","mesos");

        JSONArray dateOfBirth = documentContext.read("$..dateOfBirth");
        assertThat(dateOfBirth).containsExactlyInAnyOrder(null, null, null);

        JSONArray x = documentContext.read("$..profilePicture");
        assertThat(x).isNotNull();
        //assertThat(x).containsExactlyInAnyOrder(Files.readAllBytes(testFile)); excluded because it needs to be created via code for the default to work, not the default data
    }

    @Test
    void shouldReturnAllCatsFromCertainUserWhenListIsRequestedAndTheyAreAnotherUser() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/cats/fromOwner/paul", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        int catCount = documentContext.read("$.length()");
        assertThat(catCount).isEqualTo(3);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(5,6,3);

        JSONArray names = documentContext.read("$..name");
        assertThat(names).containsExactlyInAnyOrder("psilos","kontos","mesos");

        JSONArray dateOfBirth = documentContext.read("$..dateOfBirth");
        assertThat(dateOfBirth).containsExactlyInAnyOrder(null, null, null);

        JSONArray x = documentContext.read("$..profilePicture");
        assertThat(x).isNotNull();
        //assertThat(x).containsExactlyInAnyOrder(Files.readAllBytes(testFile)); excluded because it needs to be created via code for the default to work, not the default data
    }
}
