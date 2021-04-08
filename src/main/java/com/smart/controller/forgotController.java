package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

@Controller
public class forgotController {

	Random random=new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder; 
	
	@RequestMapping("/forgot")
	public String openEmailForm()
	{
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session)
	{


		int otp=random.nextInt(9999);
		
		System.out.println(otp);
		
		String subject="OTP From SCM";
		String message="<h1> otp ="+otp+" </h1>";
		String to=email;
		boolean flag =this.emailService.SendEmail(subject, message, to);
		
		if(flag)
		{
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);			
			return "/verify_email";
			
		}else {
			
			session.setAttribute("message","check Your Email Id" );
			
			return "/forgot_email_form";
		}
		
	}
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp,HttpSession session)
	{
		int myotp=(int)session.getAttribute("myotp");
		
		String email=(String)session.getAttribute("email");
		System.out.print(otp+" "+ myotp);
		if(otp==myotp)
		{
			System.out.println("if");
			
			User user = this.userRepository.getUserByUserName(email);
			
			if(user==null)
			{
				//no user found and send error msg
				session.setAttribute("message","No User does exits with this email" );
				System.out.println("if");
				return "forgot_email_form";
			}else {
				//send to change password form
				System.out.println("else");
				return "/password_change_form";	
			}
			
			
		}else {
			System.out.println("else");			
			session.setAttribute("message", "You have Entered Wrong Otp");
			
			return "/verify_email";
		}
		
	}
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newPassword,HttpSession session) {
		
		String email=(String)session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		
		user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		this.userRepository.save(user);
		
		session.setAttribute("message", "You have Entered Wrong Otp");
			
		return "redirect:/signin?change=password changed sucessfully..";
	}
	
}
