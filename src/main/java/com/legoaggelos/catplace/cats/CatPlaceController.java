package com.legoaggelos.catplace.cats;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.sql.rowset.serial.SerialBlob;

@RestController
@RequestMapping("/cats")
public class CatPlaceController {


    private final CatPlaceRepository repository;

    private CatPlaceController(CatPlaceRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    ResponseEntity<List<Cat>> findAll(Pageable pageable, Principal principal) {
        Page<Cat> page = repository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "name"))
                ));
        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/{requestedId}")
    ResponseEntity<Cat> findById(@PathVariable Long requestedId) {
        //everyone should be able to see each other's cats.
        Optional<Cat> cat = findCat(requestedId);
        if (cat.isPresent()) {
            return ResponseEntity.ok(cat.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private Optional<Cat> findCat(Long requestedId) {
        return repository.findById(requestedId);
    }

    private Cat findCat(Long requestedId, Principal principal) {
        return repository.findByIdAndOwner(requestedId, principal.getName());
    }

    private Cat findCat(Long requestedId, Authentication authentication) {
        return repository.findByIdAndOwner(requestedId, authentication.getName());
    }

    @PostMapping
    private ResponseEntity<Void> createCat(@RequestBody Cat newCatRequest, UriComponentsBuilder ucb, Authentication authentication, OutputStream outputStream){
        SerialBlob profilePicture = newCatRequest.profilePicture();
        try {
            if (profilePicture == null) {
                profilePicture = new SerialBlob(Files.readAllBytes(Path.of("4.jpg")));
            }
        } catch (SQLException | IOException e) {
            System.out.println("Error: couldn't read default profile picture. This is a major bug."); //TODO proper logging
        }
        boolean isAlive = OffsetDateTime.now().getYear()-newCatRequest.dateOfBirth().getYear()<30 && newCatRequest.isAlive(); //if the cat is over 30, realistically, it is dead.;
        if (authentication.getAuthorities().toString().contains("ADMIN")) {
            isAlive = newCatRequest.isAlive();
        }
        Cat catWithOwner = new Cat(null,
                newCatRequest.name(),
                newCatRequest.dateOfBirth(),
                authentication.getName(),
                profilePicture,
                newCatRequest.bio(),
                isAlive
                );
        Cat savedCat = repository.save(catWithOwner);
        URI locationOfNewCat = ucb
                .path("/cats/{id}")
                .buildAndExpand(savedCat.id())
                .toUri();
        return ResponseEntity.created(locationOfNewCat).build();
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> putCat(@PathVariable Long requestedId, @RequestBody Cat catUpdate, Authentication authentication) {
        boolean admin = authentication.getAuthorities().toString().contains("ADMIN");
        Cat cat = findCat(requestedId, authentication);

        OffsetDateTime dateOfBirth = null; //cant update date of birth!
        boolean isAlive = true; //if the cat is over 30, realistically, it is dead.

        //Here, if it is an admin, it allows them to override anything, while the regular user can't always
        if (admin) {
            //the cat variable & isAlive variable can be set to whatever an admin wants
            Optional<Cat> catFromId = findCat(requestedId);
            if (catFromId.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            dateOfBirth = catUpdate.dateOfBirth();
            isAlive =  catUpdate.isAlive();
        } else { //not admin
            if (cat == null) {
                return ResponseEntity.notFound().build();
            }
            dateOfBirth = catUpdate.dateOfBirth(); //cant update date of birth!
            isAlive = OffsetDateTime.now().getYear()-catUpdate.dateOfBirth().getYear()<30 && catUpdate.isAlive(); //if the cat is over 30, realistically, it is dead.
        }

        Cat update = new Cat(requestedId,
                catUpdate.name(),
                dateOfBirth,
                authentication.getName(),
                catUpdate.profilePicture(),
                catUpdate.bio(),
                isAlive);
        repository.save(update);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteCat(@PathVariable Long id, Authentication authentication) {
        boolean admin = authentication.getAuthorities().toString().contains("ADMIN");
        if (repository.existsByIdAndOwner(id, authentication.getName()) || admin) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
