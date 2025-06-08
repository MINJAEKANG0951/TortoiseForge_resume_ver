package forge.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import forge.model.DAO;
import forge.model.UserDTO;
import forge.services.EmailService;
import forge.services.RecaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class Controller_ForgotUsername {
	
	private final DAO dao;
	private final EmailService emailService;
	private final RecaptchaService recaptchaService;
	private final PasswordEncoder passwordEncoder;
	
	private final int EMAIL_VERIFICATION_STEP 	= 1;
	private final int SEND_USERNAME_STEP		= 2;
	
	public Controller_ForgotUsername
	(	
		DAO dao,
		EmailService emailService,
		RecaptchaService recaptchaService,
		PasswordEncoder	passwordEncoder
	)
	{
		this.dao = dao;
		this.emailService = emailService;
		this.recaptchaService = recaptchaService;
		this.passwordEncoder = passwordEncoder;
	}
	
	
	
	@GetMapping("/forgotUsername")
	public String showForgotUsername(HttpSession session) {
		session.setAttribute("forgotUsernameEmail", null);
		session.setAttribute("forgotUsernameCode", null);
		return "forgot_username";
	}
	
	
	
	// forgot password 할 때 아무데나 메일보내지말고 DB에서 가입된 회원인지 확인하는 로직 ㄱ
	
	@PostMapping("/submitForgotUsernameInfo")
	@ResponseBody
	public ResponseEntity<Map<String,Object>>
	checkForgotUsernameInfo
	(HttpServletRequest req, HttpSession session)
	{
		Map<String,Object> response = new HashMap<String,Object>();		
		int currentStep = 0;
		try
		{
			currentStep					= Integer.parseInt( req.getParameter("currentStep") );
			String userEmail			= req.getParameter("userEmail");
			String userVerificationCode	= req.getParameter("userVerificationCode");
			
			if
			( currentStep != EMAIL_VERIFICATION_STEP && currentStep != SEND_USERNAME_STEP )
			{
				response.put("status", "error");
				response.put("stepToGo", "1");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Please follow the specified signup procedure.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			
			if ( dao.getUserByEmail(userEmail)==null ) {
				response.put("status", "error");
				response.put("stepToGo", "1");
				response.put("error", "NO_USER_WITH_THE_EMAIL");
				response.put("message", "There is no user registered with that email address.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			// STEP1 -> STEP2
			if(currentStep == EMAIL_VERIFICATION_STEP) {
				
				String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0,7);
				
				try
				{
					emailService.sendEmail(userEmail, randomStr, "TORTOISE FORGE EMAIL VERIFICATION CODE");
					session.setAttribute("forgotUsernameEmail", userEmail);
					session.setAttribute("forgotUsernameCode", randomStr);
				}
				catch(Exception e)
				{
					response.put("status", "fail");
					response.put("stepToGo", "1");
					response.put("error", "FAILED_TO_SEND_VERFICATION_CODE");
					response.put("message", "Failed to send a verfication code to your email.");
					return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				
				response.put("status", "success");
				response.put("stepToGo", "2");
				response.put("message", "The verification code has been sent to the email, " + userEmail  + ". Please enter this code to have your username sent to your email.");
				return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
			}
			
			
			// VERIFICATION CODE CHECK
			if
			(
				session.getAttribute("forgotUsernameEmail") == null ||
				session.getAttribute("forgotUsernameCode") 	== null
			)
			{
				response.put("status", "fail");
				response.put("stepToGo", "2");
				response.put("error", "VERIFICATION_CODE_NOT_EXIST");
				response.put("message", "The verification code does not exist or has expired.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			String Email 				 = session.getAttribute("forgotUsernameEmail").toString();
			String EmailVerificationCode = session.getAttribute("forgotUsernameCode").toString();
			
			if(!Email.equals(userEmail)) {
				response.put("status", "fail");
				response.put("stepToGo", "2");
				response.put("error", "NOT_THE_SAME_EMAIL");
				response.put("message", "The email is not the same email we've sent the verification code to.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
				
			if(!EmailVerificationCode.equals(userVerificationCode)) {
				response.put("status", "fail");
				response.put("stepToGo", "2");
				response.put("error", "WRONG_VERIFICATION_CODE");
				response.put("message", "The verification code is wrong.");	
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			
			// SENDING EMAIL WITH USERNAME
			try
			{
				UserDTO user = dao.getUserByEmail(userEmail);
				
				String str = "Your username is [" + user.getUsername() + "]";
				
				emailService.sendEmail(userEmail, str, "YOUR TORTOISE FORGE USERNAME");
				
				session.setAttribute("forgotUsernameEmail", null);
				session.setAttribute("forgotUsernameCode", null);
				
				response.put("status", "success");
				response.put("message", "Your username has been sent to your email!");
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			catch(Exception e)
			{
				response.put("status", "fail");
				response.put("stepToGo", "2");
				response.put("error", "FAILED_TO_UPDATE_PASSWORD");
				response.put("message", "There was an error sending username to your email. Please try again.");	
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.put("status", "error");
			response.put("stepToGo", currentStep);
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "An unknown error occurred. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
