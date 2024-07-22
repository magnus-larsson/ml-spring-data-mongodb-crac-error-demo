package com.example.data.mongodb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class AuthorController {
    
	@Autowired
	private AuthorRepository authorRepository;

	@GetMapping(
		value = "/getAuthor/{id}",
		produces = "application/json")
	Author getAuthor(@PathVariable String id) {
		return authorRepository.findById(id).orElse(null);
	}
}
