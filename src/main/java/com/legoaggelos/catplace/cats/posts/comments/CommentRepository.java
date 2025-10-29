package com.legoaggelos.catplace.cats.posts.comments;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long>, PagingAndSortingRepository<Comment, Long> {
    Page<Comment> findByPostId(Long postId, Pageable pageable);
    Page<Comment> findByPoster(String poster, Pageable pageable);
    Optional<Comment> findById(Long commentId);

    boolean existsByIdAndPoster(Long requestedId, String name);

    @Modifying
    @Query("delete from COMMENT where POSTER = :poster")
    Long deleteAllByPoster(@Param("poster")String poster);

    @Modifying
    @Query("delete from COMMENT where POST_POSTER = :postPoster")
    Long deleteAllByPostUserPoster(@Param("postPoster")String postPoster);

    @Modifying
    @Query("delete from COMMENT where POST_CAT_POSTER = :postPoster")
    Long deleteAllByPostCatPoster(@Param("postPoster")Long postPoster);

    @Modifying
    @Query("delete from COMMENT where REPLYING_TO = :replying")
    Long deleteAllByReplyingTo(@Param("replying")Long replying);

    boolean existsByPostPosterAndPostCatPoster(String poster, Long postCatPoster);

    Page<Comment> findByPostCatPoster(Long postCatPoster, Pageable pageable);

    Page<Comment> findByReplyingTo(Long replyingTo, Pageable pageable);

    Page<Comment> findByPostPoster(String postPoster, Pageable pageable);

    ;
}
