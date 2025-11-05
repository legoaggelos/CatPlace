package com.legoaggelos.catplace.likehandling.controllers;

import com.legoaggelos.catplace.likehandling.LikedComment;
import com.legoaggelos.catplace.likehandling.repositories.LikedCommentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

import static com.legoaggelos.catplace.security.util.AdminCertifier.isAdmin;

@RestController
@RequestMapping("/likedComment")
public class LikedCommentController {
    private final LikedCommentRepository repository;

    public LikedCommentController(LikedCommentRepository repository) {
        this.repository = repository;
    }
    @GetMapping("/fromId/{requestedId}")
    private ResponseEntity<LikedComment> getById(@PathVariable Long requestedId, Authentication authentication) {
        if (authentication == null || !isAdmin(authentication)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<LikedComment> infoHolder = repository.findById(requestedId);
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
        return repository.existsByUsernameAndCommentLikedId(user, requestedId) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    @GetMapping("/{requestedId}")
    private ResponseEntity<Void> existsById(@PathVariable Long requestedId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.notFound().build();
        }
        return repository.existsByUsernameAndCommentLikedId(authentication.getName(), requestedId) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    @PostMapping
    private ResponseEntity<Void> likeComment(@RequestBody Long requestedId, UriComponentsBuilder ucb, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } else if (repository.existsByUsernameAndCommentLikedId(authentication.getName(), requestedId)) {
            return ResponseEntity.badRequest().header("message", "Comment "+requestedId+" has already been liked by user "+authentication.getName()+".").build();
        }
        var liked = new LikedComment(null, authentication.getName(), requestedId);
        var savedLiked = repository.save(liked);
        URI locationOfLiked = ucb
                .path("/likedComment/{id}")
                .buildAndExpand(savedLiked.id())
                .toUri();
        return ResponseEntity.created(locationOfLiked).build();
    }
    @DeleteMapping("/{requestedId}")
    private ResponseEntity<Void> deleteComment(@PathVariable Long requestedId, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean shouldDelete = repository.existsByUsernameAndCommentLikedId(authentication.getName(), requestedId);
        if (shouldDelete) {
            repository.deleteByUsernameAndCommentLikedId(authentication.getName(), requestedId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{requestedId}/user/{username}")
    private ResponseEntity<Void> deleteComment(@PathVariable Long requestedId, @PathVariable String username, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean shouldDelete = repository.existsByUsernameAndCommentLikedId(username, requestedId);
        if (shouldDelete) {
            repository.deleteByUsernameAndCommentLikedId(username, requestedId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
