package com.legoaggelos.catplace;

import org.springframework.data.annotation.Id;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;

public record Cat(@Id Long id, String name, Integer ageInMonths, String owner, SerialBlob profilePicture) {

}
