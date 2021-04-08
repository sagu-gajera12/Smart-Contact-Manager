package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.smart.dao.ContactRepository;
import com.smart.dao.MyPaymentOrderRepo;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyPaymenrOrder;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.sun.xml.bind.api.impl.NameConverter.Standard;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder; 
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository; 
	
	@Autowired
	private MyPaymentOrderRepo myPaymentOrderRepo; 
	//method for adding Comman Data
    @ModelAttribute
	public void addCommonData(Model model,Principal principal)
	{
		String userName=principal.getName();
		System.out.println(userName);
		System.out.println("hello this is running");
		User user=userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);		
	}
	//dashBoard Home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
        model.addAttribute("title", "User  DashBoard");
		return "normal/user_dashboard";
	}
	
	//open add from handle
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title", "add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute("contact") Contact contact,
			Principal principal,
			@RequestParam("profileImage") MultipartFile file,
			HttpSession httpSession
			)
	{
		try {
		String  name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		//Processing and uploading file
		
		if(file.isEmpty())
		{
			System.out.println("file is empty");
			contact.setImage("java.png");			
		}else {

			String name1=file.getOriginalFilename();
			contact.setImage(name1);
     		File saveFile=new ClassPathResource("static/image").getFile();
	        Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
	        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		   
		    System.out.println("Image is Uploaded");
		}

		
		contact.setUser(user);
		
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		
		System.out.println("contact "+contact.toString());
		
		System.out.println("Added to Data Base");
		
		//message sucess
		httpSession.setAttribute("message", new Message("Your Contact is Added !! Add More","success"));		
		}catch (Exception e) {
			// TODO: handle exception
		System.out.println("message "+e.getMessage());
		
		//message error
		httpSession.setAttribute("message", new Message("Something Went Wrong Try Again","danger"));		
		}
		
		return "normal/add_contact_form";
	}
	
	//show contact handler
	//per page n contact
	//current page = 0;
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal) {
		
		m.addAttribute("title", "Show User Contacts");
		   String userName = principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		//current page-page
		//contact per page-5
		Pageable pageable=PageRequest.of(page, 3);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	//showing perticular contact details
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model m,Principal principal)
	{
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
	   String userName=principal.getName();
	   
	   User user=this.userRepository.getUserByUserName(userName);
	   
		if(user.getId()==contact.getUser().getId())
		{
		m.addAttribute("contact", contact);
		m.addAttribute("title", contact.getName());
		}
		return "normal/contact_detail";
	}
	//delete contact
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid,Model model,Principal principal,HttpSession  session)
	{
		System.out.print("delete is running");
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
	        Contact contact = contactOptional.get();

	 	   String userName=principal.getName();
		   
		   User user=this.userRepository.getUserByUserName(userName);
		   
		   boolean remove = user.getContacts().remove(contact);
		    this.userRepository.save(user);
		   
			if(remove)
			{

                session.setAttribute("message",new Message("Contact deleted succesfully..","success"));
			}else {
				session.setAttribute("message",new Message(" You can't Delete this Contact..","danger"));
			}

	        
	        return "redirect:/user/show-contacts/0";
				
	}
	
	//open update from handler
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model m)
	{
		
		Contact contact = this.contactRepository.findById(cid).get();
		
		m.addAttribute("title","Upate Contact");
	    m.addAttribute("contact", contact);
	    
		return "normal/update_form";
	}
	//contact update handler
	
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,
			Model m,
			HttpSession session,
			Principal principal
			)
	{
		System.out.println("Name "+contact.getName());
		System.out.println("Id "+contact.getCid());
		
	String oldName = this.contactRepository.findById(contact.getCid()).get().getImage();
	
		
		try {
			
			if(!file.isEmpty())
			{
				//delete old photo

				String name1=file.getOriginalFilename();
				contact.setImage(name1);
	     		File saveFile=new ClassPathResource("static/image").getFile();
		        Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
		        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			   
			    System.out.println("Image is updated");	
			    }else {
			    	contact.setImage(oldName);
			    }
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Data is Upadated","success"));
			
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
	//your profile handler
	
	@GetMapping("/profile")
	public String yourProfile(Model m)
	{
		m.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	//open setting handler
	
	@GetMapping("/settings")
	public String openSettings()
	{
		return "normal/settings";
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword,
			Principal principal,
	HttpSession session)
	{
		User user = this.userRepository.getUserByUserName(principal.getName());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, user.getPassword()))
		{
		//change	
			user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		this.userRepository.save(user);
		session.setAttribute("message", new Message("Your Password  is successfully Changed","success"));
		}else {
			//error
			session.setAttribute("message", new Message("please Enter correct old password","danger"));
		return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}
	
	//createing order for payment
	
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String,Object> data, Principal principal) throws RazorpayException
	{
		
		
		int amt=Integer.parseInt(data.get("amount").toString());
		System.out.println(data);
		
		try {
		
		RazorpayClient razorpayClient = new RazorpayClient("rzp_test_3KvzsvgNcPRBKc", "QhamX4yhqRXQ4gV8i9uWPV9x");
		JSONObject ob=new JSONObject();
		ob.put("amount", amt*100);
		ob.put("currency","INR");
		ob.put("receipt", "txn_2345");
		
		//createing order
		Order order = razorpayClient.Orders.create(ob);
		System.out.println(order);
		MyPaymenrOrder myPaymenrOrder=new MyPaymenrOrder();
		myPaymenrOrder.setAmount(order.get("amount").toString());
		myPaymenrOrder.setOrderId(order.get("id"));
		myPaymenrOrder.setPaymentId(null);
		myPaymenrOrder.setStatus("created");
		myPaymenrOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
		myPaymenrOrder.setRecipe(order.get("receipt"));
		
		try {
			
		this.myPaymentOrderRepo.save(myPaymenrOrder);
		
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
		}
		return order.toString();
		
		} catch (Exception e) {
			
			System.err.println(e); 
			
			return e.getMessage();
		}
		
		

	}
	@PostMapping("/update_order")
	public ResponseEntity<?> updatePayment(@RequestBody Map<String,Object>   data) {
		
		MyPaymenrOrder myPaymentOrder = this.myPaymentOrderRepo.findByOrderId(data.get("order_id").toString());
		
		myPaymentOrder.setPaymentId(data.get("payment_id").toString());
		myPaymentOrder.setStatus(data.get("status").toString());
               
		this.myPaymentOrderRepo.save(myPaymentOrder);
 		   System.out.println(data);
		return ResponseEntity.ok(Map.of("msg","updated"));
 		    
	}
		
}
