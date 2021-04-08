package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder PasswordEncoder;
	
	@Autowired
	private UserRepository  userRepository;
	
	@GetMapping("/test")
	@ResponseBody
	public String test()
	{
		User user=new User();
		user.setName("sagar gajera");
		user.setEmail("sagar1@gmail.com");
		userRepository.save(user);
		return "working";
	}
	
	@RequestMapping("/")
	public String  home(Model model)
	{
		model.addAttribute("title", "Home - smart contact manager");
		
		return "home";
	}
	
	@RequestMapping("/about")
	public String  about(Model model)
	{
		model.addAttribute("title", "about - smart contact manager");
		
		return "about";
	}
	
	@RequestMapping("/signup")
	public String  signup(Model model)
	{
		model.addAttribute("title", "Register - smart contact manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	@RequestMapping(value = "/do_register",method = RequestMethod.POST)
	public String  registerUser(@Valid @ModelAttribute("user") User user,BindingResult  bindingResult,@RequestParam(value = "agreement",defaultValue = "false") boolean agreement,Model model,HttpSession session )
	{
		System.out.print(agreement);
		
		try {
			
			System.out.println(agreement);
			
			if(!agreement)
			{
				
				
				
			throw new Exception("You have not agreed the term and condition");			
			}
			
			if(bindingResult.hasErrors())
			{
			//	model.addAttribute("user", user);
				System.out.println(bindingResult.toString());
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnable(true);
			user.setImageUrl("default.jpg");
			user.setPassword(PasswordEncoder.encode(user.getPassword()));
	        System.out.println("agreement "+agreement);
	        System.out.println("User "+user.toString());
	        User result=this.userRepository.save(user);
	        model.addAttribute("user", new User());
	        session.setAttribute("message",new Message("Regestion sucessful","alert-success"));
	        System.out.print(result);
			
			
		} catch (Exception e) {
			model.addAttribute("user", user);
			session.setAttribute("message",new Message("something went wrong  !! "+e.getMessage(),"alert-danger"));
			
		}
		
		
		return "signup";
	}

	
	//handler for custom login
	
	@GetMapping("/signin")
	public String customLogin(Model model)
	{
		
		model.addAttribute("title", "login page");
		
		return "login";
	}
}
