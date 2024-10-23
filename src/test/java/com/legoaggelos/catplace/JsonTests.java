package com.legoaggelos.catplace;

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
			new Cat(1L,"psilos", 4, "legoaggelos"),
			new Cat(2L, "kontos", 69, "legoaggelos"),
			new Cat(3L, "mesos", 31, "legoaggelos"),
			new Cat(4L, "arabas", 1, "kat")
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
        assertThat(json.write(cats[0])).hasJsonPathNumberValue("@.ageInMonths");
        assertThat(json.write(cats[0])).extractingJsonPathNumberValue("@.ageInMonths")
        .isEqualTo(4);
	}
	@Test
	void singleDeserializationTest() throws IOException {
		String jsonString = """
					{
						"id": 1,
						"name": "psilos",
						"ageInMonths": 4,
						"owner": "legoaggelos"
					}
				""";
		assertThat(json.parse(jsonString)).isEqualTo(new Cat(1L,"psilos",4,"legoaggelos"));
		assertThat(json.parseObject(jsonString).id()).isEqualTo(1);
		assertThat(json.parseObject(jsonString).name()).isEqualTo("psilos");
		assertThat(json.parseObject(jsonString).ageInMonths()).isEqualTo(4);
		assertThat(json.parseObject(jsonString).owner()).isEqualTo("legoaggelos");
	}
}
