package com.legoaggelos.catplace.likehandling.repositories;

import com.legoaggelos.catplace.likehandling.LikedPost;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LikedPostRepository extends CrudRepository<LikedPost, Long> {
    boolean existsByUsernameAndPostLikedId(String username, Long likedId);

    @Modifying
    @Query("DELETE FROM LIKED_POST WHERE POST_LIKED_ID = :likedId AND USERNAME = :username")
    void deleteByUsernameAndPostLikedId(@Param("username")String username, @Param("likedId")Long likedId);
}
