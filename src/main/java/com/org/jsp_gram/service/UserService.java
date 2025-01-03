package com.org.jsp_gram.service;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import com.org.jsp_gram.dto.User;
import com.org.jsp_gram.helper.AES;
import com.org.jsp_gram.helper.EmailSender;
import com.org.jsp_gram.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Service
public class UserService {
	
	
	@Autowired
	UserRepository repository;
	
	@Autowired
	EmailSender emailSender;

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
	
	

}
