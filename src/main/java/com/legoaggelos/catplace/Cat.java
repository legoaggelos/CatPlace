package com.legoaggelos.catplace;

import org.springframework.data.annotation.Id;

import java.io.InputStream;

public record Cat(@Id Long id, String name, Integer ageInMonths, String owner, InputStream profilePicture) {

}
