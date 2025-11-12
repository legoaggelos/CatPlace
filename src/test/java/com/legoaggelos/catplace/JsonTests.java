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
import com.legoaggelos.catplace.cats.posts.comments.Comment;
import com.legoaggelos.catplace.likehandling.LikedComment;
import com.legoaggelos.catplace.likehandling.LikedPost;
import com.legoaggelos.catplace.security.users.CatPlaceUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import javax.sql.rowset.serial.SerialBlob;

@JsonTest
@AutoConfigureTestDatabase
public class JsonTests {
	@Autowired
    private JacksonTester<Cat> json;

	@Autowired
	private JacksonTester<CatPlaceUser> jsonUser;

	@Autowired
	private JacksonTester<Comment> jsonComment;

	@Autowired
	private JacksonTester<LikedComment> jsonLikedComment;

	@Autowired
	private JacksonTester<LikedPost> jsonLikedPost;

	@Autowired
	private JacksonTester<Post> jsonPost;
	private Cat[] cats;
	private CatPlaceUser[] users;
	private Post[] posts;
	private Comment[] comments;
	private LikedComment likedComment;
	private LikedPost likedPost;
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
				//new CatPlaceUser("legoaggelos", "legoaggelos", null, "Owner of site", "legoangel2010@gmail.com", true),
				//new CatPlaceUser("paul", "paul", null, "Owner of cats", "example@gmail.com"),
				//new CatPlaceUser("Katherine", "kat", null, "", null)
		};
		posts = new Post[] {
			new Post(5L, null, 0L, 5L, "paul", "Cute cat!!",sampleDate, true),
			new Post(2L, null, 0L, 5L, "paul", "Cuter cat!!",sampleDate.minusDays(1), false)
		};
		comments = new Comment[] {
				new Comment(48L, "ragebait fr", 0L, 4L, "kat", "kat", 4L, OffsetDateTime.parse("2025-04-08T02:45:45Z"), 3L)
		};
		likedComment = new LikedComment(5L, "kat", 4L);
		likedPost = new LikedPost(5L, "kat", 4L);
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
	}

	@Test
	void singleCommentSerializationTest() throws IOException {
		var writtenComment = jsonComment.write(comments[0]);
		assertThat(writtenComment).isStrictlyEqualToJson("single_comment.json");
		assertThat(writtenComment).hasJsonPathNumberValue("@.id");
		assertThat(writtenComment).extractingJsonPathNumberValue("@.id")
				.isEqualTo(48);
		assertThat(writtenComment).hasJsonPathNumberValue("@.postId");
		assertThat(writtenComment).extractingJsonPathNumberValue("@.postId")
				.isEqualTo(4);
		assertThat(writtenComment).hasJsonPathNumberValue("@.postCatPoster");
		assertThat(writtenComment).extractingJsonPathNumberValue("@.postCatPoster")
				.isEqualTo(4);
		assertThat(writtenComment).hasJsonPathNumberValue("@.replyingTo");
		assertThat(writtenComment).extractingJsonPathNumberValue("@.replyingTo")
				.isEqualTo(3);
		assertThat(writtenComment).hasJsonPathStringValue("@.content");
		assertThat(writtenComment).extractingJsonPathStringValue("@.content")
				.isEqualTo("ragebait fr");
		assertThat(writtenComment).hasJsonPathStringValue("@.poster");
		assertThat(writtenComment).extractingJsonPathStringValue("@.poster")
				.isEqualTo("kat");
		assertThat(writtenComment).hasJsonPathStringValue("@.postUserPoster");
		assertThat(writtenComment).extractingJsonPathStringValue("@.postUserPoster")
				.isEqualTo("kat");
		assertThat(writtenComment).hasJsonPathStringValue("@.postTime");
		assertThat(writtenComment).extractingJsonPathStringValue("@.postTime")
				.isEqualTo("2025-04-08T02:45:45Z");
	}

	@Test
	void singleLikedCommentSerializationTest() throws IOException {
		var writtenLikedComment = jsonLikedComment.write(likedComment);
		assertThat(writtenLikedComment).isStrictlyEqualToJson("single_liked_comment.json");
		assertThat(writtenLikedComment).hasJsonPathStringValue("@.username");
		assertThat(writtenLikedComment).extractingJsonPathStringValue("@.username")
						.isEqualTo("kat");
		assertThat(writtenLikedComment).hasJsonPathNumberValue("@.id");
		assertThat(writtenLikedComment).extractingJsonPathNumberValue("@.id")
				.isEqualTo(5);
		assertThat(writtenLikedComment).hasJsonPathNumberValue("@.commentLikedId");
		assertThat(writtenLikedComment).extractingJsonPathNumberValue("@.commentLikedId")
				.isEqualTo(4);
	}

	@Test
	void singleLikedPostSerializationTest() throws IOException {
		var writtenLikedPost= jsonLikedPost.write(likedPost);
		assertThat(writtenLikedPost).isStrictlyEqualToJson("single_liked_post.json");
		assertThat(writtenLikedPost).hasJsonPathStringValue("@.username");
		assertThat(writtenLikedPost).extractingJsonPathStringValue("@.username")
				.isEqualTo("kat");
		assertThat(writtenLikedPost).hasJsonPathNumberValue("@.id");
		assertThat(writtenLikedPost).extractingJsonPathNumberValue("@.id")
				.isEqualTo(5);
		assertThat(writtenLikedPost).hasJsonPathNumberValue("@.postLikedId");
		assertThat(writtenLikedPost).extractingJsonPathNumberValue("@.postLikedId")
				.isEqualTo(4);
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

	@Test
	void singleCommentDeserializationTest() throws IOException {
		String jsonString = """
				{
				"id": 48,
				  "content": "ragebait fr",
				  "postId": 4,
				  "likeCount": 0,
				  "poster": "kat",
				  "postUserPoster": "kat",
				  "postCatPoster": 4,
				  "postTime": "2025-04-08T02:45:45Z",
				  "replyingTo": 3
				}
				""";
		assertThat(jsonComment.parse(jsonString)).isEqualTo(comments[0]);
		var parsedObject = jsonComment.parseObject(jsonString);
		assertThat(parsedObject.id()).isEqualTo(48);
		assertThat(parsedObject.content()).isEqualTo("ragebait fr");
		assertThat(parsedObject.postId()).isEqualTo(4);
		assertThat(parsedObject.likeCount()).isEqualTo(0);
		assertThat(parsedObject.poster()).isEqualTo("kat");
		assertThat(parsedObject.postUserPoster()).isEqualTo("kat");
		assertThat(parsedObject.postCatPoster()).isEqualTo(4);
		assertThat(parsedObject.postTime()).isEqualTo(OffsetDateTime.parse("2025-04-08T02:45:45Z") );
		assertThat(parsedObject.replyingTo()).isEqualTo(3);
	}

	@Test
	void singleLikedCommentDeserializationTest() throws IOException {
		String jsonString =
				"""
						{
						  "id": 5,
						  "username": "kat",
						  "commentLikedId": 4
						}
						""";
		assertThat(jsonLikedComment.parse(jsonString)).isEqualTo(likedComment);
		var parsedObject = jsonLikedComment.parseObject(jsonString);
		assertThat(parsedObject.id()).isEqualTo(5);
		assertThat(parsedObject.commentLikedId()).isEqualTo(4);
		assertThat(parsedObject.username()).isEqualTo("kat");
	}

	@Test
	void singleLikedPostDeserializationTest() throws IOException {
		String jsonString =
				"""
						{
						  "id": 5,
						  "username": "kat",
						  "postLikedId": 4
						}
						""";
		assertThat(jsonLikedPost.parse(jsonString)).isEqualTo(likedPost);
		var parsedObject = jsonLikedPost.parseObject(jsonString);
		assertThat(parsedObject.id()).isEqualTo(5);
		assertThat(parsedObject.postLikedId()).isEqualTo(4);
		assertThat(parsedObject.username()).isEqualTo("kat");
	}
}
