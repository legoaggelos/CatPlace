package com.legoaggelos.catplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;


@SpringBootApplication
public class CatPlaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatPlaceApplication.class, args);
    }

}
