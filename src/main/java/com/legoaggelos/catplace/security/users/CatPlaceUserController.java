package com.legoaggelos.catplace.security.users;

import com.legoaggelos.catplace.cats.Cat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class CatPlaceUserController {
    private final CatPlaceUserRepository catPlaceUserRepository;

    public CatPlaceUserController(CatPlaceUserRepository catPlaceUserRepository) {
        this.catPlaceUserRepository = catPlaceUserRepository;
    }
    @GetMapping("/{requestedUsername}")
    private ResponseEntity<CatPlaceUser> findByUsername(@PathVariable String requestedUsername) {
        Optional<CatPlaceUser> catPlaceUser = catPlaceUserRepository.findByUsername(requestedUsername);
        if (catPlaceUser.isPresent()) {
            return ResponseEntity.ok(catPlaceUser.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    private ResponseEntity<Void> createCatPlaceUser(@RequestBody CatPlaceUser newCatPlaceUserRequest, UriComponentsBuilder ucb, OutputStream outputStream) {
        if (catPlaceUserRepository.existsByUsername(newCatPlaceUserRequest.getUsername())) {
            return ResponseEntity.badRequest().header("message", "Username "+newCatPlaceUserRequest.getUsername()+" is already taken.").build();
        }
        SerialBlob profilePicture = newCatPlaceUserRequest.getProfilePicture();
        try {
            if (profilePicture == null) {
                profilePicture = new SerialBlob(Files.readAllBytes(Path.of("4.jpg")));
            }
        } catch (SQLException | IOException e) {
            System.out.println("Error: couldn't read default profile picture. This is a major bug."); //TODO proper logging
        }
        CatPlaceUser user = new CatPlaceUser(newCatPlaceUserRequest.getDisplayName(), newCatPlaceUserRequest.getUsername(), profilePicture, newCatPlaceUserRequest.getBio(), newCatPlaceUserRequest.getEmail(), newCatPlaceUserRequest.getLikedPosts(), newCatPlaceUserRequest.getLikedComments(), newCatPlaceUserRequest.getLikedReplies(), newCatPlaceUserRequest.isAdmin());
        CatPlaceUser savedCatPlaceUser = catPlaceUserRepository.save(user);
        URI locationOfNewCat = ucb
                .path("/users/" + newCatPlaceUserRequest.getUsername())
                .buildAndExpand(savedCatPlaceUser.getUsername())
                .toUri();
        return ResponseEntity.created(locationOfNewCat).build();
    }

    @DeleteMapping("/{requestedUsername}")
    private ResponseEntity<Void> deleteCatPlaceUser(@PathVariable String requestedUsername, Authentication authentication) {
        boolean admin = authentication.getAuthorities().toString().contains("ADMIN");
        if (catPlaceUserRepository.existsByUsername(requestedUsername) && (requestedUsername.equals(authentication.getName()) || admin)) {
            catPlaceUserRepository.deleteById(requestedUsername);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{requestedUsername}")
    private ResponseEntity<Void> putCatPlaceUser(@PathVariable String requestedUsername, @RequestBody CatPlaceUser newCatPlaceUserRequest, UriComponentsBuilder ucb, Authentication authentication) {
        boolean admin = authentication.getAuthorities().toString().contains("ADMIN");
        Optional<CatPlaceUser> catPlaceUser = catPlaceUserRepository.findByUsername(requestedUsername);
        if (catPlaceUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else if ((!admin && !authentication.getName().equals(catPlaceUser.get().getUsername()))) {
            return ResponseEntity.notFound().build();
        }
        CatPlaceUser newCatPlaceUser = new CatPlaceUser(newCatPlaceUserRequest.getDisplayName(), requestedUsername, newCatPlaceUserRequest.getProfilePicture(), (admin) ? catPlaceUser.get().getBio()/*should not let admin update bio*/ : newCatPlaceUserRequest.getBio(), newCatPlaceUserRequest.getEmail(), newCatPlaceUserRequest.getLikedPosts(), newCatPlaceUserRequest.getLikedComments(), newCatPlaceUserRequest.getLikedReplies(), newCatPlaceUserRequest.isAdmin() && admin, false);
        catPlaceUserRepository.save(newCatPlaceUser);
        return ResponseEntity.noContent().build();
    }
}
