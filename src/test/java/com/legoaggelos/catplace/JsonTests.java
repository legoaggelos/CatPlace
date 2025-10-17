package com.legoaggelos.catplace;

import static com.legoaggelos.catplace.CatApplicationTests.sampleDate;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;

import com.legoaggelos.catplace.cats.Cat;
import com.legoaggelos.catplace.security.users.CatPlaceUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

@JsonTest
public class JsonTests {
	@Autowired
    private JacksonTester<Cat> json;

	@Autowired
	private JacksonTester<CatPlaceUser> jsonUser;
	private Cat[] cats;
	private CatPlaceUser[] users;
	@BeforeEach
	void setUp() {
		cats= new Cat[]{
			new Cat(1L,"psilos", sampleDate, "paul", null, "tall cat", true),
			new Cat(2L, "kontos", sampleDate, "paul", null, "short", true),
			new Cat(3L, "mesos", sampleDate, "paul", null, "average", true),
			new Cat(4L, "arabas", sampleDate, "kat", null, "arabian", true)
		};
		users = new CatPlaceUser[]{
				new CatPlaceUser("legoaggelos", "legoaggelos", null, "Owner of site", "legoangel2010@gmail.com", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), true),
				new CatPlaceUser("paul", "paul", null, "Owner of cats", "example@gmail.com"),
				new CatPlaceUser("Katherine", "kat", null, "", null)
		};
	}
	
	@Test
	void singleCatSerializationTest() throws IOException {
		assertThat(json.write(cats[0])).isStrictlyEqualToJson("single_cat.json");
		assertThat(json.write(cats[0])).isStrictlyEqualToJson("single_cat.json");
        assertThat(json.write(cats[0])).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cats[0])).extractingJsonPathNumberValue("@.id")
                .isEqualTo(1);
        assertThat(json.write(cats[0])).hasJsonPathStringValue("@.name");
        assertThat(json.write(cats[0])).extractingJsonPathStringValue("@.name")
                .isEqualTo("psilos");
        assertThat(json.write(cats[0])).hasJsonPathStringValue("@.owner");
        assertThat(json.write(cats[0])).extractingJsonPathStringValue("@.owner")
                .isEqualTo("paul");
        assertThat(json.write(cats[0])).hasJsonPathStringValue("@.dateOfBirth");
        assertThat(json.write(cats[0])).extractingJsonPathStringValue("@.dateOfBirth")
        .isEqualTo("2025-04-08T02:30:30Z");
	}

	@Test
	void singleUserSerializationTest() throws IOException {
		//paul
		assertThat(jsonUser.write(users[1])).isStrictlyEqualToJson("single_user.json");
		assertThat(jsonUser.write(users[1])).isStrictlyEqualToJson("single_user.json");
		assertThat(jsonUser.write(users[1])).hasJsonPathStringValue("@.displayName");
		assertThat(jsonUser.write(users[1])).extractingJsonPathStringValue("@.displayName")
				.isEqualTo("paul");
		assertThat(jsonUser.write(users[1])).hasJsonPathStringValue("@.username");
		assertThat(jsonUser.write(users[1])).extractingJsonPathStringValue("@.username")
				.isEqualTo("paul");
		assertThat(jsonUser.write(users[1])).hasJsonPathStringValue("@.bio");
		assertThat(jsonUser.write(users[1])).extractingJsonPathStringValue("@.bio")
				.isEqualTo("Owner of cats");
		assertThat(jsonUser.write(users[1])).hasJsonPathStringValue("@.email");
		assertThat(jsonUser.write(users[1])).extractingJsonPathStringValue("@.email")
				.isEqualTo("example@gmail.com");
		assertThat(jsonUser.write(users[1])).hasJsonPathArrayValue("@.likedPosts");
		assertThat(jsonUser.write(users[1])).extractingJsonPathArrayValue("@.likedPosts")
				.isEqualTo(new ArrayList<>());
		assertThat(jsonUser.write(users[1])).hasJsonPathArrayValue("@.likedComments");
		assertThat(jsonUser.write(users[1])).extractingJsonPathArrayValue("@.likedComments")
				.isEqualTo(new ArrayList<>());
		assertThat(jsonUser.write(users[1])).hasJsonPathArrayValue("@.likedReplies");
		assertThat(jsonUser.write(users[1])).extractingJsonPathArrayValue("@.likedReplies")
				.isEqualTo(new ArrayList<>());
	}

	@Test
	void singleUserDeserializationTest() throws IOException {
		String jsonString = """
					{
					 "displayName": "paul",
				    "username": "paul",
				    "id": "paul",
				    "profilePicture": null,
				    "bio": "Owner of cats",
				    "email": "example@gmail.com",
				    "likedPosts": [],
				    "likedComments": [],
				    "likedReplies": [],
				    "admin": false,
				    "new": true
					}
				""";
		assertThat(jsonUser.parseObject(jsonString).getDisplayName()).isEqualTo("paul");
		assertThat(jsonUser.parseObject(jsonString).getUsername()).isEqualTo("paul");
		assertThat(jsonUser.parseObject(jsonString).getProfilePicture()).isEqualTo(null);
		assertThat(jsonUser.parseObject(jsonString).getBio()).isEqualTo("Owner of cats");
		assertThat(jsonUser.parseObject(jsonString).getEmail()).isEqualTo("example@gmail.com");
		assertThat(jsonUser.parseObject(jsonString).isAdmin()).isEqualTo(false);
		assertThat(jsonUser.parseObject(jsonString).getLikedComments()).isEqualTo(new ArrayList<>());
		assertThat(jsonUser.parseObject(jsonString).getLikedComments()).isEqualTo(new ArrayList<>());
		assertThat(jsonUser.parseObject(jsonString).getLikedReplies()).isEqualTo(new ArrayList<>());
	}

	@Test
	void singleCatDeserializationTest() throws IOException {
		String jsonString = """
					{
						"id": 1,
				    	"name": "psilos",
				    	"dateOfBirth": "2025-04-08T02:30:30Z",
				    	"owner": "paul",
				    	"profilePicture": null,
				    	"bio": "tall cat",
				    	"isAlive": true
					}
				""";
		assertThat(json.parse(jsonString)).isEqualTo(new Cat(1L,"psilos",sampleDate,"paul", null, "tall cat", true));
		assertThat(json.parseObject(jsonString).id()).isEqualTo(1);
		assertThat(json.parseObject(jsonString).name()).isEqualTo("psilos");
		assertThat(json.parseObject(jsonString).dateOfBirth()).isEqualTo(sampleDate.toString());
		assertThat(json.parseObject(jsonString).owner()).isEqualTo("paul");
	}
}
