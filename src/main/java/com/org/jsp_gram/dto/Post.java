package com.org.jsp_gram.dto;



import java.sql.Date;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Post {
	
	private int id;
	private String imageUrl;
	private String caption;
	@UpdateTimestamp
	private Date postedtime;
	@ManyToOne
	private User user;

}
