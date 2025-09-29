package com.legoaggelos.catplace;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatPlaceApplicationTests {
	@Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldReturnACatWhenDataIsSaved() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("legoaggelos", "abc123")
                .getForEntity("/cats/5", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(5);

        String name = documentContext.read("$.name");
        assertThat(name).isEqualTo("psilos");
        
        Number ageInMonths = documentContext.read("$.ageInMonths");
        assertThat(ageInMonths).isEqualTo(4);
        
    }
    
    @Test
    void shouldReturnACatWhenDataIsSavedAndTheyDoNotOwnTheCard() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("kat", "xyz789")
                .getForEntity("/cats/5", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(5);

        String name = documentContext.read("$.name");
        assertThat(name).isEqualTo("psilos");
        
        Number ageInMonths = documentContext.read("$.ageInMonths");
        assertThat(ageInMonths).isEqualTo(4);
        
    }
    @Test
    void shouldNotReturnACatWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("legoaggelos", "abc123")
                .getForEntity("/cats/1021300", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }
    @Test
    void shouldReturnAllCatsWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("legoaggelos", "abc123")
                .getForEntity("/cats", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        int catCount = documentContext.read("$.length()");
        assertThat(catCount).isEqualTo(3);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(5,6,3);

        JSONArray names = documentContext.read("$..name");
        assertThat(names).containsExactlyInAnyOrder("psilos","kontos","mesos");
        
        JSONArray ageInMonths = documentContext.read("$..ageInMonths");
        assertThat(ageInMonths).containsExactlyInAnyOrder(4, 69, 31);
    }

    @Test
    void shouldReturnAPageOfCats() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("legoaggelos", "abc123")
                .getForEntity("/cats?page=0&size=1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnASortedPageOfCats() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("legoaggelos", "abc123")
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
                .withBasicAuth("legoaggelos", "abc123")
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
                .withBasicAuth("legoaggelos", "BAD-PASSWORD")
                .getForEntity("/cats/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectUsersWhoAreNotCardOwners() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("hank-owns-no-cats", "qrs456")
                .getForEntity("/Cats/4", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldAllowAccessToCatsTheyDoNotOwn() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("legoaggelos", "abc123")
                .getForEntity("/cats/4", String.class); // kat's data
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    @Test
    @DirtiesContext
    void shouldCreateANewCat() {
        Cat newCat = new Cat(null, "pekos", 12, null, null);
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("legoaggelos", "abc123")
                .postForEntity("/cats", newCat, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
       
        URI locationOfNewCat = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("legoaggelos", "abc123")
                .getForEntity(locationOfNewCat, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        Integer ageInMonths = documentContext.read("$.ageInMonths");
        String name = documentContext.read("$.name");

        assertThat(id).isNotNull();
        assertThat(ageInMonths).isEqualTo(12);
        assertThat(name).isEqualTo("pekos");
    }
    @Test
    @DirtiesContext
    void shouldUpdateAnExistingCat() {
    	Cat catUpdate = new Cat(null, "mesos v2",5,null, null);
    	HttpEntity<Cat> request = new HttpEntity<>(catUpdate);
    	ResponseEntity<Void> response = restTemplate
    			.withBasicAuth("legoaggelos", "abc123")
    			.exchange("/cats/5", HttpMethod.PUT, request, Void.class);
    	assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    	
    	ResponseEntity<String> getResponse = restTemplate.withBasicAuth("legoaggelos", "abc123")
    			.getForEntity("/cats/5", String.class);
    	assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    	DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
    	
    	Number id = documentContext.read("$.id");
    	assertThat(id).isEqualTo(5);
    	
    	Integer ageInMonths = documentContext.read("$.ageInMonths");
    	assertThat(ageInMonths).isEqualTo(4);
    	
    	String name = documentContext.read("$.name");
    	assertThat(name).isEqualTo("mesos v2");
    }
    @Test
    void shouldNotUpdateACatThatDoesNotExist() {
        Cat unknownCat = new Cat(null, "mesos v2",5,null, null);
        HttpEntity<Cat> request = new HttpEntity<>(unknownCat);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("legoaggelos", "abc123")
                .exchange("/cats/99999", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotUpdateACatThatIsOwnedBySomeoneElse() {
        Cat katsCat = new Cat(null, "mesos v2",5,null, null);
        HttpEntity<Cat> request = new HttpEntity<>(katsCat);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("legoaggelos", "abc123")
                .exchange("/cats/4", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    @Test
    @DirtiesContext
    void shouldDeleteAnExistingCat() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("legoaggelos", "abc123")
                .exchange("/cats/5", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        ResponseEntity<String> getResponse = restTemplate
        		.withBasicAuth("legoaggelos", "abc123")
                .getForEntity("/cats/5", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    @Test
    void shouldNotDeleteACatThatDoesNotExist() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("legoaggelos", "abc123")
                .exchange("/cats/99999", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    @Test
    void shouldNotAllowDeletionOfCatsTheyDoNotOwn() {
   
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("legoaggelos", "abc123")
                .exchange("/cats/4", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ResponseEntity<String> getResponse = restTemplate
        .withBasicAuth("kat", "xyz789")
        .getForEntity("/cats/4", String.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
