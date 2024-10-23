package com.legoaggelos.catplace;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cats")
public class CatPlaceController {
	private final CatPlaceRepository repository;
	private CatPlaceController(CatPlaceRepository repository) {
	      this.repository = repository;
	   }
	@GetMapping
    private ResponseEntity<List<Cat>> findAll(Pageable pageable, Principal principal) {
        Page<Cat> page = repository.findByOwner(principal.getName(),
                PageRequest.of(
                    pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                ));
        return ResponseEntity.ok(page.getContent());
    }
	@GetMapping("/{requestedId}")
	private ResponseEntity<Cat> findById(@PathVariable Long requestedId, Principal principal){
		//return ResponseEntity.ok(new Cat(0L,"",0,""));
		Cat cat = findCat(requestedId, principal);
		if (cat!=null) {
            return ResponseEntity.ok(cat);
        } else {
            return ResponseEntity.notFound().build();
        }
	}
	 private Cat findCat(Long requestedId, Principal principal) {
	        return repository.findByIdAndOwner(requestedId, "legoaggelos");
	    }
}
