package com.legoaggelos.catplace;

import static com.legoaggelos.catplace.CatPlaceApplicationTests.sampleDate;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

@JsonTest
public class JsonTests {
	@Autowired
    private JacksonTester<Cat> json;
	
	private Cat[] cats;
	@BeforeEach
	void setUp() {
		cats= new Cat[]{
			new Cat(1L,"psilos", sampleDate, "legoaggelos", null, "tall cat", true),
			new Cat(2L, "kontos", sampleDate, "legoaggelos", null, "short", true),
			new Cat(3L, "mesos", sampleDate, "legoaggelos", null, "average", true),
			new Cat(4L, "arabas", sampleDate, "kat", null, "arabian", true)
		};
	}
	
	@Test
	void singleSerializationTest() throws IOException {
		assertThat(json.write(cats[0])).isStrictlyEqualToJson("single.json");
		assertThat(json.write(cats[0])).isStrictlyEqualToJson("single.json");
        assertThat(json.write(cats[0])).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cats[0])).extractingJsonPathNumberValue("@.id")
                .isEqualTo(1);
        assertThat(json.write(cats[0])).hasJsonPathStringValue("@.name");
        assertThat(json.write(cats[0])).extractingJsonPathStringValue("@.name")
                .isEqualTo("psilos");
        assertThat(json.write(cats[0])).hasJsonPathStringValue("@.owner");
        assertThat(json.write(cats[0])).extractingJsonPathStringValue("@.owner")
                .isEqualTo("legoaggelos");
        assertThat(json.write(cats[0])).hasJsonPathStringValue("@.dateOfBirth");
        assertThat(json.write(cats[0])).extractingJsonPathStringValue("@.dateOfBirth")
        .isEqualTo("2025-04-08T02:30:30Z");
	}
	@Test
	void singleDeserializationTest() throws IOException {
		String jsonString = """
					{
						"id": 1,
				    	"name": "psilos",
				    	"dateOfBirth": "2025-04-08T02:30:30Z",
				    	"owner": "legoaggelos",
				    	"profilePicture": null,
				    	"bio": "tall cat",
				    	"isAlive": true
					}
				""";
		assertThat(json.parse(jsonString)).isEqualTo(new Cat(1L,"psilos",sampleDate,"legoaggelos", null, "tall cat", true));
		assertThat(json.parseObject(jsonString).id()).isEqualTo(1);
		assertThat(json.parseObject(jsonString).name()).isEqualTo("psilos");
		assertThat(json.parseObject(jsonString).dateOfBirth()).isEqualTo(sampleDate.toString());
		assertThat(json.parseObject(jsonString).owner()).isEqualTo("legoaggelos");
	}
}
