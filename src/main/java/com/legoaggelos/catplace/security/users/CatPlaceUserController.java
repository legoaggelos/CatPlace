package com.legoaggelos.catplace.security.users;

import com.legoaggelos.catplace.cats.Cat;
import com.legoaggelos.catplace.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.ArrayList;
import java.util.Optional;

import static com.legoaggelos.catplace.security.util.AdminCertifier.isAdmin;

@RestController
@RequestMapping("/users")
public class CatPlaceUserController {
    private final CatPlaceUserRepository catPlaceUserRepository;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtUtil jwtUtils;

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
        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add((GrantedAuthority) () -> "USER");
        if (newCatPlaceUserRequest.isAdmin()) {
            authorities.add((GrantedAuthority) () -> "ADMIN");
        }
        CatPlaceUser user = new CatPlaceUser(newCatPlaceUserRequest.getDisplayName(), newCatPlaceUserRequest.getUsername(), profilePicture, newCatPlaceUserRequest.getBio(), newCatPlaceUserRequest.getEmail(), false, passwordEncoder.encode(newCatPlaceUserRequest.getPassword()), authorities);
        CatPlaceUser savedCatPlaceUser = catPlaceUserRepository.save(user);
        URI locationOfNewCat = ucb
                .path("/users/" + newCatPlaceUserRequest.getUsername())
                .buildAndExpand(savedCatPlaceUser.getUsername())
                .toUri();
        return ResponseEntity.created(locationOfNewCat).build();
    }

    @DeleteMapping("/{requestedUsername}")
    private ResponseEntity<Void> deleteCatPlaceUser(@PathVariable String requestedUsername, Authentication authentication) {
        boolean admin = isAdmin(authentication);
        if (catPlaceUserRepository.existsByUsername(requestedUsername) && (requestedUsername.equals(authentication.getName()) || admin)) {
            catPlaceUserRepository.deleteById(requestedUsername);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{requestedUsername}")
    private ResponseEntity<Void> putCatPlaceUser(@PathVariable String requestedUsername, @RequestBody CatPlaceUser newCatPlaceUserRequest, UriComponentsBuilder ucb, Authentication authentication) {
        boolean admin = isAdmin(authentication);
        Optional<CatPlaceUser> catPlaceUser = catPlaceUserRepository.findByUsername(requestedUsername);
        if (catPlaceUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else if ((!admin && !authentication.getName().equals(catPlaceUser.get().getUsername()))) {
            return ResponseEntity.notFound().build();
        }
        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add((GrantedAuthority) () -> "USER");
        if (newCatPlaceUserRequest.isAdmin() && admin) {
            authorities.add((GrantedAuthority) () -> "ADMIN");
        }
        CatPlaceUser newCatPlaceUser = new CatPlaceUser(newCatPlaceUserRequest.getDisplayName(), requestedUsername, newCatPlaceUserRequest.getProfilePicture(), (admin) ? catPlaceUser.get().getBio()/*should not let admin update bio*/ : newCatPlaceUserRequest.getBio(), newCatPlaceUserRequest.getEmail(), newCatPlaceUserRequest.isAdmin() && admin, false, passwordEncoder.encode(newCatPlaceUserRequest.getPassword()), authorities);
        catPlaceUserRepository.save(newCatPlaceUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/signin")
    public String authenticateUser(@RequestBody CatPlaceUser user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword()
                )
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtUtils.generateToken(userDetails.getUsername());
    }
}
