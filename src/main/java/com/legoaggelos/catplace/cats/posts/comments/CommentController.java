package com.legoaggelos.catplace.cats.posts.comments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

@RestController
@RequestMapping("/comments")
public class CommentController {
    @Autowired
    CommentRepository repository;

    public CommentController(CommentRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/getFromCatPoster/{requestedId}")
    private ResponseEntity<List<Comment>> getCommentsFromCatPoster(@PathVariable Long requestedId, Pageable pageable, Authentication authentication) {
        if (!authentication.getAuthorities().toString().contains("ADMIN")) {
            return ResponseEntity.notFound().build();
        }
        Page<Comment> page = repository.findByPostCatPoster(requestedId,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "postTime"))
                )
        );
        if (page.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/getFromPostUserPoster/{requestedId}")
    private ResponseEntity<List<Comment>> getCommentsFromPostUserPoster(@PathVariable String requestedId, Pageable pageable, Authentication authentication) {
        if (!authentication.getAuthorities().toString().contains("ADMIN")) {
            return ResponseEntity.notFound().build();
        }
        Page<Comment> page = repository.findByPostPoster(requestedId,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "postTime"))
                )
        );
        if (page.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/getFromReplyingTo/{requestedId}")
    private ResponseEntity<List<Comment>> getCommentsFromReplyingTo(@PathVariable Long requestedId, Pageable pageable, Authentication authentication) {
        Page<Comment> page = repository.findByReplyingTo(requestedId,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "postTime"))
                )
        );
        if (page.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/{requestedId}")
    private ResponseEntity<Comment> getComment(@PathVariable Long requestedId) {
        Optional<Comment> comment = repository.findById(requestedId);
        if (comment.isPresent()) {
            return ResponseEntity.ok(comment.get());
        }
        return ResponseEntity.notFound().build();
    }


    @GetMapping("/fromPostId/{requestedPostId}")
    private ResponseEntity<List<Comment>> getCommentsFromPost(@PathVariable Long requestedPostId, Pageable pageable) {
        Page<Comment> page = repository.findByPostId(requestedPostId,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "postTime"))
                ));
        if (page.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/fromPoster/{userOwner}")
    private ResponseEntity<List<Comment>> getCommentsFromUser(@PathVariable String userOwner, Pageable pageable) {
        Page<Comment> page = repository.findByPoster(userOwner,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "postTime"))
                ));
        if (page.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(page.getContent());
    }

    @PostMapping
    private ResponseEntity<Void> postComment(@RequestBody Comment newCommentRequest, UriComponentsBuilder ucb, Authentication authentication) {
        String username = authentication.getName();
        Comment request = new Comment(null, newCommentRequest.content(), 0L, newCommentRequest.postId(), username, newCommentRequest.postPoster(), newCommentRequest.postCatPoster(), Instant.now().atOffset(ZoneOffset.UTC), newCommentRequest.replyingTo());
        Comment savedComment = repository.save(request);
        URI locationOfNewComment = ucb
                .path("/comments/{id}")
                .buildAndExpand(savedComment.id())
                .toUri();
        return ResponseEntity.created(locationOfNewComment).build();
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> updateComment(@PathVariable Long requestedId, @RequestBody Comment commentUpdate, Authentication authentication) {
        Optional<Comment> commentOptional = repository.findById(requestedId);
        if (commentOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        boolean admin = authentication.getAuthorities().toString().contains("ADMIN");
        boolean owns = repository.existsByIdAndPoster(requestedId, authentication.getName());
        Comment comment = commentOptional.get();
        Comment update = new Comment(requestedId,
                (owns ? commentUpdate.content() : comment.content()),
                (admin || (!owns && Math.abs(commentUpdate.likeCount() - comment.likeCount()) <= 1 && commentUpdate.likeCount() >= 0)) ? commentUpdate.likeCount() : comment.likeCount(), //only admin and not-owner can update like count)
                comment.postId(),
                comment.poster(),
                comment.postPoster(),
                comment.postCatPoster(),
                comment.postTime(),
                comment.replyingTo()
        );
        repository.save(update);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{requestedId}")
    private ResponseEntity<Void> deleteComment(@PathVariable Long requestedId, Authentication authentication) {
        if (authentication.getAuthorities().toString().contains("ADMIN") && repository.existsById(requestedId)) {
            repository.deleteById(requestedId);
            return ResponseEntity.noContent().build();
        }
        if (repository.existsByIdAndPoster(requestedId, authentication.getName())) {
            repository.deleteById(requestedId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/deleteByPostUserPoster/{requestedId}")
    private ResponseEntity<Void> deleteCommentsByPostUserPoster(@PathVariable String requestedId, Authentication authentication) {
        if (authentication.getName().equals(requestedId) /*when users delete their account, they delete all comments in posts they posted.*/ || authentication.getAuthorities().toString().contains("ADMIN")) {
            repository.deleteAllByPostUserPoster(requestedId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/deleteByPostCatPoster/{requestedId}")
    private ResponseEntity<Void> deleteCommentsByPostCatPoster(@PathVariable Long requestedId, Authentication authentication) {
        if (repository.existsByPostPosterAndPostCatPoster(authentication.getName(), requestedId)/*if a comment exists, and the post poster and cat poster match the requested cat and user, that means the cat belongs to the user, so they can delete all comments in the post*/ || authentication.getAuthorities().toString().contains("ADMIN")) { //when users delete a c, they delete all comments in posts the cat posted.
            repository.deleteAllByPostCatPoster(requestedId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/deleteByPoster/{requestedId}")
    private ResponseEntity<Void> deleteCommentsByPoster(@PathVariable String requestedId, Authentication authentication) {
        if (authentication.getName().equals(requestedId) || authentication.getAuthorities().toString().contains("ADMIN")) {
            repository.deleteAllByPoster(requestedId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/deleteByParentComment/{requestedId}")
    private ResponseEntity<Void> deleteCommentsByParentComment(@PathVariable Long requestedId, Authentication authentication) {
        if (repository.existsByIdAndPoster(requestedId/*parent comment*/, authentication.getName())/*if the comment with the requested id is owned by the user, they can delete all replies*/ || authentication.getAuthorities().toString().contains("ADMIN")) {
            repository.deleteAllByReplyingTo(requestedId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
