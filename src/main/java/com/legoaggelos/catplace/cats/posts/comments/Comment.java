package com.legoaggelos.catplace.cats.posts.comments;

import org.springframework.data.annotation.Id;

import java.time.OffsetDateTime;

public record Comment(@Id Long id,
                      String content,
                      Long likeCount,
                      Long postId,
                      String poster/*the one who posted the comment*/,
                      String postUserPoster/*the one who posted the post the comment belongs in*/,
                      Long postCatPoster/*the cat who posted the post the comment belongs in*/,
                      OffsetDateTime postTime,
                      Long replyingTo) {
}
