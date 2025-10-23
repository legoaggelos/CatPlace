package com.legoaggelos.catplace;

import static com.legoaggelos.catplace.CatApplicationTests.sampleDate;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

import com.legoaggelos.catplace.cats.Cat;
import com.legoaggelos.catplace.cats.posts.Post;
import com.legoaggelos.catplace.security.users.CatPlaceUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import javax.sql.rowset.serial.SerialBlob;

@JsonTest
public class JsonTests {
	@Autowired
    private JacksonTester<Cat> json;

	@Autowired
	private JacksonTester<CatPlaceUser> jsonUser;

	@Autowired
	private JacksonTester<Post> jsonPost;
	private Cat[] cats;
	private CatPlaceUser[] users;
	private Post[] posts;
	private OffsetDateTime sampleDate = OffsetDateTime.of(2025,4, 8, 2, 30, 30, 0, ZoneOffset.ofHours(3));

    public JsonTests() throws IOException, SQLException {
    }

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
		posts = new Post[] {
			new Post(5L, null, 0L, 5L, "paul", "Cute cat!!",sampleDate, true),
			new Post(2L, null, 0L, 5L, "paul", "Cuter cat!!",sampleDate.minusDays(1), false)
		};
	}
	@Test
	void singlePostSerializationTest() throws IOException {
		var json = jsonPost.write(posts[0]);
		assertThat(json).isStrictlyEqualToJson("single_post.json");

		assertThat(json).hasJsonPathNumberValue("@.id");
		assertThat(json).extractingJsonPathNumberValue("@.id")
				.isEqualTo(5);
		assertThat(json).hasJsonPathNumberValue("@.likeCount");
		assertThat(json).extractingJsonPathNumberValue("@.likeCount")
				.isEqualTo(0);
		assertThat(json).hasJsonPathNumberValue("@.catOwner");
		assertThat(json).extractingJsonPathNumberValue("@.catOwner")
				.isEqualTo(5);

		assertThat(json).hasJsonPathStringValue("@.userOwner");
		assertThat(json).extractingJsonPathStringValue("@.userOwner")
				.isEqualTo("paul");

		assertThat(json).hasJsonPathStringValue("@.desc");
		assertThat(json).extractingJsonPathStringValue("@.desc")
				.isEqualTo("Cute cat!!");

		assertThat(json).hasJsonPathStringValue("@.uploadDate");
		assertThat(json).extractingJsonPathStringValue("@.uploadDate")
				.isEqualTo("2025-04-08T02:30:30+03:00");

		assertThat(json).hasJsonPathBooleanValue("@.isApproved");
		assertThat(json).extractingJsonPathBooleanValue("@.isApproved")
				.isEqualTo(true);
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
        .isEqualTo("2025-04-08T02:30:30+03:00");
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
	void singlePostDeserializationTest() throws IOException {
		String jsonString = """
				{
				  "id": 5,
				  "image": null,
				  "likeCount": 0,
				  "catOwner": 5,
				  "userOwner": "paul",
				  "desc": "Cute cat!!",
				  "uploadDate": "2025-04-08T02:30:30+03:00",
				   "isApproved": true
				}
				""";
		var parsed = jsonPost.parseObject(jsonString);
		assertThat(parsed.id()).isEqualTo(5);
		assertThat(parsed.catOwner()).isEqualTo(5);
		assertThat(parsed.desc()).isEqualTo("Cute cat!!");
		assertThat(parsed.image()).isEqualTo(null);
		assertThat(parsed.isApproved()).isEqualTo(true);
		assertThat(parsed.likeCount()).isEqualTo(0);
		assertThat(parsed.uploadDate()).isEqualTo("2025-04-08T02:30:30+03:00");
		assertThat(parsed.userOwner()).isEqualTo("paul");
	}

	@Test
	void singleCatDeserializationTest() throws IOException {
		String jsonString = """
					{
						"id": 1,
				    	"name": "psilos",
				    	"dateOfBirth": "2025-04-08T02:30:30+03:00",
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
