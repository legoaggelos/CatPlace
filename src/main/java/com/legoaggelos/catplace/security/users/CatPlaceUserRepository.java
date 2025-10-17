package com.legoaggelos.catplace.security.users;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatPlaceUserRepository extends CrudRepository<CatPlaceUser, String>, PagingAndSortingRepository<CatPlaceUser, String> {
    Optional<CatPlaceUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
