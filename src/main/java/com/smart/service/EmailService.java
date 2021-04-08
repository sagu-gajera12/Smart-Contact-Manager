package com.smart.service;

import java.io.File;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	String from="jalsamoj1@gmail.com";
	
	public boolean SendEmail(String subject, String message, String to) {
	
		// TODO Auto-generated method stub
		//variable for email
		//String host=;
		
		//get the properties 
		Properties properties = System.getProperties();
		
		//setting important information to properties object
		
		//host set
	//	properties.put("mail.smtp.user", from);
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");


		
       //setp:1		
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				// TODO Auto-generated method stub
				return new PasswordAuthentication("jalsamoj1@gmail.com","Jalsa@12345");
			}
			
		});
		
		session.setDebug(true);
		//setp:2
		 MimeMessage m = new MimeMessage(session);
		
		 try {
		//from email
			 m.setFrom(from);
			 //send to mail
			 m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			 //add subject
			 m.setSubject(subject);
		//	 m.setText(message);
			m.setContent(message, "text/html"); 
			 
			 //step 3
			 Transport.send(m);
			 
			 System.out.println("successfully sent");
			 return true;
			 
		} catch (Exception e) {
			// TODO: handle exception
		}

		 
		 return false;
	
	}
    

}
