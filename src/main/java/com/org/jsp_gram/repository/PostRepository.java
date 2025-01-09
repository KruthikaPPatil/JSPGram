package com.org.jsp_gram.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.jsp_gram.dto.Post;

public interface PostRepository extends JpaRepository<Post, Integer> {

}
