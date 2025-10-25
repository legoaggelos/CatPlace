package com.legoaggelos.catplace.cats.posts;

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
public interface PostRepository extends CrudRepository<Post, Long>, PagingAndSortingRepository<Post, Long> {
    Page<Post> findByCatOwner(Long catOwner, Pageable pageable);
    Page<Post> findByUserOwner(String userOwner, Pageable pageable);
    Page<Post> findByCatOwnerAndIsApproved(Long catOwner, boolean isApproved, Pageable pageable);
    Page<Post> findByUserOwnerAndIsApproved(String userOwner, boolean isApproved, Pageable pageable);
    boolean existsById(Long id);
    boolean existsByIdAndUserOwner(Long id, String userOwner);

    boolean existsByCatOwnerAndUserOwner(Long catOwner, String userOwner);

    @Modifying
    @Query("delete from POST where USER_OWNER = :userOwner")
    Long deleteAllByUserOwner(@Param("userOwner")String userOwner);

    @Modifying
    @Query("delete from POST where CAT_OWNER = :catOwner")
    Long deleteAllByCatOwner(@Param("catOwner")Long catOwner);

    boolean existsByCatOwner(Long requestedId);

    boolean existsByUserOwner(String requestedId);

}
