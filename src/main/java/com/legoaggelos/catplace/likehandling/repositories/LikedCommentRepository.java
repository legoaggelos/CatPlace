package com.legoaggelos.catplace.likehandling.repositories;

import com.legoaggelos.catplace.likehandling.LikedComment;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LikedCommentRepository extends CrudRepository<LikedComment, Long> {
    boolean existsByUsernameAndCommentLikedId(String username, Long likedId);
    @Modifying
    @Query("DELETE FROM LIKED_COMMENT WHERE COMMENT_LIKED_ID = :likedId AND USERNAME = :username")
    void deleteByUsernameAndCommentLikedId(@Param("username")String username, @Param("likedId")Long likedId);
}
