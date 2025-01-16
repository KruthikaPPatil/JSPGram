package com.org.jsp_gram.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.jsp_gram.dto.Post;
import com.org.jsp_gram.dto.User;

public interface PostRepository extends JpaRepository<Post, Integer> {

	List<Post> findByUser(User user);

}
