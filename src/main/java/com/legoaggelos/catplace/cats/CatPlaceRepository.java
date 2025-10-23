package com.legoaggelos.catplace.cats;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CatPlaceRepository extends CrudRepository<Cat, Long>, PagingAndSortingRepository<Cat, Long>{
	Cat findByIdAndOwner(Long id, String owner);

    Page<Cat> findByOwner(String owner, PageRequest pageRequest);

    boolean existsByIdAndOwner(Long id, String owner);

    @Modifying
    @Query("delete from cat where OWNER = :owner")
    void deleteAllByOwner(@Param("owner")String owner);
}
