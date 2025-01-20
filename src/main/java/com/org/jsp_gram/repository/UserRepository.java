package com.org.jsp_gram.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.jsp_gram.dto.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	
	  boolean existsByEmail(String email);
	  
	  boolean existsByMobile(long mobile);
	  
	  boolean existsByUsername(String username);

	  User findByUsername(String username);
	  
	  List<User> findByVerifiedTrue();
}
