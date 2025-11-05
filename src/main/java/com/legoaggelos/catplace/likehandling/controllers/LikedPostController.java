package com.legoaggelos.catplace.likehandling.controllers;

import com.legoaggelos.catplace.likehandling.LikedComment;
import com.legoaggelos.catplace.likehandling.LikedPost;
import com.legoaggelos.catplace.likehandling.repositories.LikedPostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

import static com.legoaggelos.catplace.security.util.AdminCertifier.isAdmin;

@RestController
@RequestMapping("/likedPost")
public class LikedPostController {
    private final LikedPostRepository repository;

    public LikedPostController(LikedPostRepository repository) {
        this.repository = repository;
    }
    @GetMapping("/fromId/{requestedId}")
    private ResponseEntity<LikedPost> getById(@PathVariable Long requestedId, Authentication authentication) {
        if (authentication == null || !isAdmin(authentication)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<LikedPost> infoHolder = repository.findById(requestedId);
        if (infoHolder.isPresent()) {
            return ResponseEntity.ok(infoHolder.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/fromId/{requestedId}/user/{user}")
    private ResponseEntity<Void> existsByIdAndOwner(@PathVariable Long requestedId, @PathVariable String user, Authentication authentication) {
        if (authentication == null || !isAdmin(authentication)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return repository.existsByUsernameAndPostLikedId(user, requestedId) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    @GetMapping("/{requestedId}")
    private ResponseEntity<Void> existsById(@PathVariable Long requestedId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.notFound().build();
        }
        return repository.existsByUsernameAndPostLikedId(authentication.getName(), requestedId) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping
    private ResponseEntity<Void> likePost(@RequestBody Long requestedId, UriComponentsBuilder ucb, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } else if (repository.existsByUsernameAndPostLikedId(authentication.getName(), requestedId)) {
            return ResponseEntity.badRequest().header("message", "Post "+requestedId+" has already been liked by user "+authentication.getName()+".").build();
        }
        var liked = new LikedPost(null, authentication.getName(), requestedId);
        var savedLiked = repository.save(liked);
        URI locationOfLiked = ucb
                .path("/likedPost/{id}")
                .buildAndExpand(savedLiked.id())
                .toUri();
        return ResponseEntity.created(locationOfLiked).build();
    }

    @DeleteMapping("/{requestedId}")
    private ResponseEntity<Void> deletePost(@PathVariable Long requestedId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean shouldDelete = repository.existsByUsernameAndPostLikedId(authentication.getName(), requestedId);
        if (shouldDelete) {
            repository.deleteByUsernameAndPostLikedId(authentication.getName(), requestedId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{requestedId}/user/{username}")
    private ResponseEntity<Void> deletePost(@PathVariable Long requestedId, @PathVariable String username, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean shouldDelete = repository.existsByUsernameAndPostLikedId(username, requestedId);
        if (shouldDelete) {
            repository.deleteByUsernameAndPostLikedId(username, requestedId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
