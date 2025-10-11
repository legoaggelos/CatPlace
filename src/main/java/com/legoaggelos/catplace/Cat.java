package com.legoaggelos.catplace;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.data.annotation.Id;

import javax.sql.rowset.serial.SerialBlob;

public record Cat(@Id Long id, String name, Integer ageInMonths, String owner, @JsonDeserialize(using = SerialBlobDeserializer.class) SerialBlob profilePicture) {

}
