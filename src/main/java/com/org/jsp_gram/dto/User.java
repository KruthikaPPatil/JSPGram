package com.org.jsp_gram.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Transient;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
	
	@Size(min=3,max=10,message = "it should be between 3 and 10 characters")
	private String firstname;
	
	@Size(min=1,max=15,message = "it should be between 1 and 15 characters")
	private String lastname;
	
	@Size(min=5,max=15,message = "it should be between 5 and 15 characters")
	private String username;
	
	@Email(message="it should be proper email format")
	@NotEmpty(message="it is required field")
	private String email;
	
	@DecimalMin(value="6000000000",message="it should be proper mobile number")
	@DecimalMax(value="9999999999",message="it should be proper mobile number")
	private long mobile;
	
	@Pattern(regexp = "^.*(?=.{8,})(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",message="it should contain atleast 8 character,one uppercase,one lowercase,one number and one special character")
	private String password;
	
	@Pattern(regexp = "^.*(?=.{8,})(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",message="it should contain atleast 8 character,one uppercase,one lowercase,one number and one special character")
	@Transient
	private String confirmpassword;
	
	@NotNull(message="it is requird field")
	private String gender;
	
	private int otp;
	
	private boolean verified;
	
	private String bio;
	private String imageUrl;
	
	@ManyToMany(fetch = FetchType.EAGER)
	List<User> followers = new ArrayList<User>();
	@ManyToMany(fetch = FetchType.EAGER)
	List<User> following = new ArrayList<User>();
	
	public boolean isFollowing() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpSession session = attributes.getRequest().getSession();
		User user=(User) session.getAttribute("user");
		if(user!=null) {
			for(User user2:user.following) {
				if(this.id==user2.id) {
					return true;
				}
			}
			return false;
		}else {
			return false;
		}
	}
	 
}
