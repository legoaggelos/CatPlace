package com.legoaggelos.catplace.cats.posts;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static com.legoaggelos.catplace.security.util.AdminCertifier.isAdmin;

@RestController
@RequestMapping("/catposts")
public class PostController {
    private final PostRepository repository;

    public PostController(PostRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{requestedId}")
    private ResponseEntity<Post> findById(@PathVariable long requestedId, Authentication authentication) {

        Optional<Post> post = repository.findById(requestedId);
        if (post.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (authentication == null) {
            return post.get().isApproved() ? ResponseEntity.ok(post.get()) : ResponseEntity.notFound().build();
        }
        String catOwner = post.get().userOwner();
        String username = authentication.getName();
        boolean admin = isAdmin(authentication);
        if ((catOwner.equals(username) || admin) || post.get().isApproved()) {
            return ResponseEntity.ok(post.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private List<Post> getAllApprovedPostsByCatId(long catId, Pageable pageable) {
        return repository.findByCatOwnerAndIsApproved(catId, true,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "uploadDate"))
                )).getContent();
    }

    private List<Post> getAllPostsByCatId(long catId, Pageable pageable) {
        return repository.findByCatOwner(catId,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "uploadDate"))
                )).getContent();
    }

    private List<Post> getAllApprovedPostsByUserId(String username, Pageable pageable) {
        return repository.findByUserOwnerAndIsApproved(username, true,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "uploadDate"))
                )).getContent();
    }

    private List<Post> getAllPostsByUserId(String username, Pageable pageable) {
        return repository.findByUserOwner(username,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "uploadDate"))
                )).getContent();
    }

    @GetMapping("/fromCatId/{requestedId}")
    private ResponseEntity<List<Post>> findByCatId(@PathVariable long requestedId, Pageable pageable, Authentication authentication) {
        if (authentication == null) {
            List<Post> posts = getAllApprovedPostsByCatId(requestedId, pageable);
            return !posts.isEmpty() ? ResponseEntity.ok(posts) : ResponseEntity.notFound().build();
        }


        boolean admin = isAdmin(authentication);
        //gotta do some extra stuff to check if the user owns the cat
        boolean owns = repository.existsByIdAndUserOwner(requestedId, authentication.getName());
        if (owns || admin) {
            List<Post> posts = getAllPostsByCatId(requestedId, pageable);
            return !posts.isEmpty() ? ResponseEntity.ok(posts) : ResponseEntity.notFound().build();
        }
        List<Post> posts = getAllApprovedPostsByCatId(requestedId, pageable);
        return !posts.isEmpty() ? ResponseEntity.ok(posts) : ResponseEntity.notFound().build();
    }

    @GetMapping("/fromOwnerId/{requestedId}")
    private ResponseEntity<List<Post>> findByOwnerId(@PathVariable String requestedId, Pageable pageable, Authentication authentication) {
        if (authentication == null) {
            List<Post> posts = getAllApprovedPostsByUserId(requestedId, pageable);
            return !posts.isEmpty() ? ResponseEntity.ok(posts) : ResponseEntity.notFound().build();
        }

        boolean admin = isAdmin(authentication);
        boolean owns = authentication.getName().equals(requestedId);
        if (admin || owns) {
            List<Post> posts = getAllPostsByUserId(requestedId, pageable);
            return !posts.isEmpty() ? ResponseEntity.ok(posts) : ResponseEntity.notFound().build();
        }
        List<Post> posts = getAllApprovedPostsByUserId(requestedId, pageable);
        return !posts.isEmpty() ? ResponseEntity.ok(posts) : ResponseEntity.notFound().build();
    }

    @GetMapping
    private ResponseEntity<List<Post>> findAll(Pageable pageable, Authentication authentication) {
        return findByOwnerId(authentication.getName(), pageable, authentication);
    }

    @PostMapping
    private ResponseEntity<Void> createPost(@RequestBody Post newPostRequest, UriComponentsBuilder ucb, Authentication authentication) {
        String username = authentication.getName();
        boolean admin = isAdmin(authentication);
        Post request = new Post(null,
                newPostRequest.image(),
                0L,
                newPostRequest.catOwner(),
                username,
                newPostRequest.desc(),
                Instant.now().atOffset(ZoneOffset.UTC),
                admin && newPostRequest.isApproved() ? true : null); //if the user isnt an admin, false, otherwise, their choice
        Post savedPost = repository.save(request);
        URI locationOfNewPost = ucb
                .path("/catposts/{id}")
                .buildAndExpand(savedPost.id())
                .toUri();
        return ResponseEntity.created(locationOfNewPost).build();
    }

    @DeleteMapping("/{requestedId}")
    private ResponseEntity<Void> deletePost(@PathVariable Long requestedId, Authentication authentication) {
        boolean admin = isAdmin(authentication);
        if (admin && repository.existsById(requestedId) || repository.existsByIdAndUserOwner(requestedId, authentication.getName())) {
            repository.deleteById(requestedId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/fromCatId/{requestedId}")
    private ResponseEntity<List<Void>> deletePostsFromCatOwner(@PathVariable Long requestedId, Authentication authentication) {
        boolean admin = isAdmin(authentication);

        //requestedId is catId. If a post exists by a cat and by the principal, that means the principal owns the cat.
        boolean owns = repository.existsByCatOwnerAndUserOwner(requestedId, authentication.getName());
        if (admin && repository.existsByCatOwner(requestedId) || owns) {
            Long amountDeleted = repository.deleteAllByCatOwner(requestedId);
            if (amountDeleted.equals(0)) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/fromOwnerId/{requestedId}")
    private ResponseEntity<Void> deletePostsFromUserOwner(@PathVariable String requestedId, Authentication authentication) {
        boolean admin = isAdmin(authentication);
        if (admin && repository.existsByUserOwner(requestedId) || requestedId.equals(authentication.getName())) {
            Long amountDeleted = repository.deleteAllByUserOwner(requestedId);
            if (amountDeleted.equals(0)) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    //Behavior: if not owner, update only like count, if owner, update only desc, if admin change everything the non-like Longs and userOwner, notably isApproved
    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> updatePost(@PathVariable Long requestedId, @RequestBody Post postUpdate, Authentication authentication) {
        Optional<Post> postOptional = repository.findById(requestedId);
        if (postOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        boolean admin = isAdmin(authentication);
        boolean owns = repository.existsByIdAndUserOwner(requestedId, authentication.getName());
        Post post = postOptional.get();
        Post update = new Post(requestedId,
                post.image(), //no one can update image. If an admin doesnt like it, they can delete it.
                (admin || (!owns && Math.abs(postUpdate.likeCount() - post.likeCount()) <= 1 && postUpdate.likeCount() >= 0)) ? postUpdate.likeCount() : post.likeCount(), //only admin and not-owner can update like count
                post.catOwner(),
                post.userOwner(),
                (owns) ? postUpdate.desc() : post.desc(), //admin shouldnt update post desc
                (admin ? postUpdate.uploadDate() : post.uploadDate()).withOffsetSameInstant(ZoneOffset.UTC), //admin can fix the upload date if it is wrong because of my potentially crappy code
                admin ? postUpdate.isApproved() : post.isApproved()
        );
        repository.save(update);
        return ResponseEntity.noContent().build();
    }
}
