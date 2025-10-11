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
        try {
            System.out.println(Arrays.toString(new SerialBlob(Files.readAllBytes(Path.of("4.jpg"))).getBinaryStream().readAllBytes()).substring(0,1000));

        } catch (SQLException | IOException e) {
            throw new RuntimeException("File could not be read", e);
        }

    }

}
