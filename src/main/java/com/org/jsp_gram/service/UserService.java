package com.org.jsp_gram.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.org.jsp_gram.dto.Post;
import com.org.jsp_gram.dto.User;
import com.org.jsp_gram.helper.AES;
import com.org.jsp_gram.helper.CloudinaryHelper;
import com.org.jsp_gram.helper.EmailSender;
import com.org.jsp_gram.repository.PostRepository;
import com.org.jsp_gram.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Service
public class UserService {
	
	
	@Autowired
	UserRepository repository;
	
	@Autowired
	PostRepository postRepository;
	
	@Autowired
	EmailSender emailSender;
	
	@Autowired
	CloudinaryHelper cloudinaryHelper;


	public String loadRegister(ModelMap map, User user) {
		map.put("user", user);
		return "register.html";
	}
	
	
	 public String register(User user,BindingResult result,HttpSession session) {
		  if(!user.getPassword().equals(user.getConfirmpassword()))
			  result.rejectValue("confirmpassword", "error.confirmpassword", "passwords not matching");
		  
		    if(repository.existsByEmail(user.getEmail()))
		    	result.rejectValue("email", "error.email","email already exists");
		    
		    if(repository.existsByMobile(user.getMobile()))
		    	result.rejectValue("mobile", "error.mobile","mobile number already exists");
		    
		    if(repository.existsByUsername(user.getUsername()))
		    	result.rejectValue("username", "error.username","username already taken");
		  
		  if(result.hasErrors()) {
			  return "register.html";
		  }
		  else {
			  user.setPassword(AES.encrypt(user.getPassword()));
			  int otp = new Random().nextInt(100000,1000000);
			  user.setOtp(otp);
			  //emailSender.sendOtp(user.getEmail(), otp, user.getFirstname());
			  System.err.println(otp);
			  repository.save(user);
			  session.setAttribute("pass", "otp sent success");
			  return "redirect:/otp/"+user.getId();
		  }
	  }
	 
	 
	 public String verifyOtp(int otp,int id,HttpSession session) {
		 User user = repository.findById(id).get();
		 if(user.getOtp()==otp) {
			 user.setVerified(true);
			 user.setOtp(0);
			 repository.save(user);
			 session.setAttribute("pass", "account created success");
			 return "redirect:/login";
		 }
		 else {
			 session.setAttribute("fail", "invalid otp, Try again");
			 return "redirect:/otp/"+id;
		 }
	 }


	public String resendOtp(int id, HttpSession session) {
		User user = repository.findById(id).get();
		int otp = new Random().nextInt(100000,1000000);
		user.setOtp(otp);
		System.err.println(otp);
		//emailSender.sendOtp(user.getEmail(), otp, user.getFirstname());
		repository.save(user);
		session.setAttribute("pass", "Otp re-sent success");
		
		return "redirect:/otp/"+user.getId();
	}


	
	public String login(String username, String password, HttpSession session) {
		User user = repository.findByUsername(username);
		if (user == null) {
			session.setAttribute("fail", "Invalid Username");
			return "redirect:/login";
		} else {
			if (AES.decrypt(user.getPassword()).equals(password)) {
				if (user.isVerified()) {
					session.setAttribute("user", user);
					session.setAttribute("pass", "Login Success");
					return "redirect:/home";
				} else {
					int otp = new Random().nextInt(100000, 1000000);
					user.setOtp(otp);
					System.err.println(otp);
					// emailSender.sendOtp(user.getEmail(), otp, user.getFirstname());
					repository.save(user);
					session.setAttribute("pass", "Otp Sent Success, First Verify Email to Login");
					return "redirect:/otp/" + user.getId();
				}
			} else {
				session.setAttribute("fail", "Incorrect Password");
				return "redirect:/login";
			}
		}
	}


	public String loadHome(HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user != null)
			return "home.html";
		else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}


	public String logout(HttpSession session) {
		session.removeAttribute("user");
		session.setAttribute("pass", "Logout Success");
		return "redirect:/login";
	}

	public String profile(HttpSession session,ModelMap map) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			List<Post> posts=postRepository.findByUser(user);
			if(!posts.isEmpty())
				map.put("posts",posts);
			return "profile.html";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}
	public String editProfile(HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			return "edit-profile.html";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}
	public String updateProfile(HttpSession session, MultipartFile image, String bio) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			user.setBio(bio);
			user.setImageUrl(cloudinaryHelper.saveImage(image));
			repository.save(user);
			return "redirect:/profile";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}

	public String loadAddPost(ModelMap map, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			map.put("add", "add");
			return "profile.html";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}
	public String addPost(Post post, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			post.setImageUrl(cloudinaryHelper.saveImage(post.getImage()));
			post.setUser(user);
			postRepository.save(post);
			
			session.setAttribute("pass", "Posted Success");
			return "redirect:/profile";
			
		}else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}


	public String delete(int id, HttpSession session) {
		if(session.getAttribute("user")!=null) {
			postRepository.deleteById(id);
			session.setAttribute("success", "post deleted successfully");
			return "redirect:/profile";
		}
		else {
			session.setAttribute("failure", "invalid session,first login");
			return "redirect:/login";
		}
	}


	public String editPost(int id, ModelMap map, HttpSession session) {
		
			User user = (User) session.getAttribute("user");
			if (user != null) {
				Post post = postRepository.findById(id).get();
				map.put("post", post);
				return "edit-post.html";
			} else
			{
				session.setAttribute("fail", "Invalid Session");
				return "redirect:/login";
			}
	}


	public String updatePost(Post post, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if(user!=null) {
			if(!post.getImage().isEmpty())
			post.setImageUrl(cloudinaryHelper.saveImage(post.getImage()));
			else
				post.setImageUrl(postRepository.findById(post.getId()).get().getImageUrl());
			post.setUser(user);
			postRepository.save(post);
			session.setAttribute("success", "post updated successfully");
			return "redirect:/profile";
		}
		else {
			session.setAttribute("failure", "invalid session");
			return "redirect:/login";
		}
		
	}
	
	public String viewSuggestions(HttpSession session, ModelMap map) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			List<User> suggestions = repository.findByVerifiedTrue();
			List<User> usersToRemove = new ArrayList<User>();
			for (User suggestion : suggestions) {
				if (suggestion.getId() == user.getId()) {
					usersToRemove.add(suggestion);
				}
				for (User followingUser : user.getFollowing()) {
					if (followingUser.getId() == suggestion.getId()) {
						usersToRemove.add(suggestion);
					}
				}
			}
			if (suggestions.isEmpty()) {
				session.setAttribute("fail", "No Suggestions");
				return "redirect:/profile";
			} else {
				suggestions.removeAll(usersToRemove);
				map.put("suggestions", suggestions);
				return "suggestions.html";
			}
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}
	public String followUser(int id, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			User folllowedUser = repository.findById(id).get();
			user.getFollowing().add(folllowedUser);
			folllowedUser.getFollowers().add(user);
			repository.save(user);
			repository.save(folllowedUser);
			return "redirect:/profile";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}

	public String getFollowers(HttpSession session, ModelMap map) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			List<User> followers = user.getFollowers();
			if (followers.isEmpty()) {
				session.setAttribute("fail", "No Followers");
				return "redirect:/profile";
			} else {
				map.put("followers", followers);
				return "followers.html";
			}
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}
	public String getFollowing(HttpSession session, ModelMap map) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			List<User> following = user.getFollowing();
			if (following.isEmpty()) {
				session.setAttribute("fail", "Not Following Anyone");
				return "redirect:/profile";
			} else {
				map.put("following", following);
				return "following.html";
			}
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}
	public String unfollow(HttpSession session, int id) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			User user2 = null;
			for (User user3 : user.getFollowing()) {
				if (id == user3.getId()) {
					user2 = user3;
					break;
				}
			}
			user.getFollowing().remove(user2);
			repository.save(user);
			User user3 = null;
			for (User user4 : user2.getFollowers()) {
				if (user.getId() == user4.getId()) {
					user3 = user4;
					break;
				}
			}
			user2.getFollowers().remove(user3);
			repository.save(user2);
			session.setAttribute("user", repository.findById(user.getId()).get());
			return "redirect:/profile";
		} else {
			session.setAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}
	}
}
