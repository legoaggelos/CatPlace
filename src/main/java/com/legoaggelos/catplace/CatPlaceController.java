package com.legoaggelos.catplace;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/cats")
public class CatPlaceController {
	private final CatPlaceRepository repository;
	private CatPlaceController(CatPlaceRepository repository) {
	      this.repository = repository;
	   }
	@GetMapping
    ResponseEntity<List<Cat>> findAll(Pageable pageable, Principal principal) {
        Page<Cat> page = repository.findByOwner(principal.getName(),
                PageRequest.of(
                    pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "name"))
                ));
        return ResponseEntity.ok(page.getContent());
    }
	@GetMapping("/{requestedId}")
	ResponseEntity<Cat> findById(@PathVariable Long requestedId){
		//everyone should be able to see each other's cats.
		Optional<Cat> cat = findCat(requestedId);
		if (cat.isPresent()) {
            return ResponseEntity.ok(cat.get());
        } else {
            return ResponseEntity.notFound().build();
        }
	}
	 private Optional<Cat> findCat(Long requestedId) {
	        return repository.findById(requestedId);
	    }
	 private Cat findCat(Long requestedId, Principal principal) {
	        return repository.findByIdAndOwner(requestedId, principal.getName());
	    }
	    @PostMapping
	    private ResponseEntity<Void> createCat(@RequestBody Cat newCatRequest, UriComponentsBuilder ucb, Principal principal) {
	        Cat catWithOwner = new Cat(null, newCatRequest.name(), newCatRequest.ageInMonths(), principal.getName());
	        Cat savedCat = repository.save(catWithOwner);
	        URI locationOfNewCat = ucb
	                .path("/cats/{id}")
	                .buildAndExpand(savedCat.id())
	                .toUri();
	        return ResponseEntity.created(locationOfNewCat).build();
	    }
	    @PutMapping("/{requestedId}")
	    private ResponseEntity<Void> putCat(@PathVariable Long requestedId, @RequestBody Cat catUpdate, Principal principal) {
	    	Cat cat = findCat(requestedId,principal);
	    	if(cat!=null) {
	    		Cat update = new Cat(requestedId, catUpdate.name(),cat.ageInMonths(), principal.getName());
	    		repository.save(update);
	    		return ResponseEntity.noContent().build();
	    	}
	        return ResponseEntity.notFound().build();
	    }
	    @DeleteMapping("/{id}")
	    private ResponseEntity<Void> deleteCat(@PathVariable Long id, Principal principal ) {
	      
	        if (repository.existsByIdAndOwner(id, principal.getName())) {
	        		repository.deleteById(id);
	        		return ResponseEntity.noContent().build();
	    	}
	    	return ResponseEntity.notFound().build();
	    }
}
