package com.legoaggelos.catplace;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CatPlaceRepository extends CrudRepository<Cat, Long>, PagingAndSortingRepository<Cat, Long>{
	Cat findByIdAndOwner(Long id, String owner);

    Page<Cat> findByOwner(String owner, PageRequest pageRequest);

    boolean existsByIdAndOwner(Long id, String owner);
}
