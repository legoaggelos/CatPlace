package com.legoaggelos.catplace;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.data.annotation.Id;

import javax.sql.rowset.serial.SerialBlob;
import java.time.OffsetDateTime;

public record Cat(@Id Long id,
                  String name,
                  @JsonDeserialize(using = OffsetDateTimeDeserializer.class) OffsetDateTime dateOfBirth,
                  String owner,
                  @JsonDeserialize(using = SerialBlobDeserializer.class) SerialBlob profilePicture,
                  String bio,
                  boolean isAlive
) {

}
