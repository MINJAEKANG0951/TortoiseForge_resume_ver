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
import forge.services.EmailService;
import forge.services.RecaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class Controller_ForgotPassword {
	
	private final DAO dao;
	private final EmailService emailService;
	private final RecaptchaService recaptchaService;
	private final PasswordEncoder passwordEncoder;
	
	private final int EMAIL_VERIFICATION_STEP 	= 1;
	private final int NEW_PASSWORD_SUBMIT		= 2;
	
	public Controller_ForgotPassword
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
	
	
	
	@GetMapping("/forgotPassword")
	public String showForgotPassword(HttpSession session) {
		session.setAttribute("forgotPasswordEmail", null);
		session.setAttribute("forgotPasswordCode", null);
		return "forgot_password";
	}
	
	
	
	// forgot password 할 때 아무데나 메일보내지말고 DB에서 가입된 회원인지 확인하는 로직 ㄱ
	
	@PostMapping("/submitForgotPasswordInfo")
	@ResponseBody
	public ResponseEntity<Map<String,Object>>
	checkForgotPasswordInfo
	(HttpServletRequest req, HttpSession session)
	{
		Map<String,Object> response = new HashMap<String,Object>();		
		int currentStep = 0;
		try
		{
			currentStep					= Integer.parseInt( req.getParameter("currentStep") );
			String userEmail			= req.getParameter("userEmail");
			String userPassword			= req.getParameter("userPassword");
			String userPasswordConfirm	= req.getParameter("userPasswordConfirm");
			String userVerificationCode	= req.getParameter("userVerificationCode");
			
			if
			( currentStep != EMAIL_VERIFICATION_STEP && currentStep != NEW_PASSWORD_SUBMIT )
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
					session.setAttribute("forgotPasswordEmail", userEmail);
					session.setAttribute("forgotPasswordCode", randomStr);
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
				response.put("message", "The verification code has been sent to the email, " + userEmail  + ". Please enter the code and new password.");
				return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
			}
			
			
			// PASSWORD CHECK
			if( userPassword.length() < 4 ) {
				response.put("status", "error");
				response.put("stepToGo", "2");
				response.put("error", "PASSWORD_TOO_SHORT");
				response.put("message", "Password must be at least 4 characters long.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(!userPassword.equals(userPasswordConfirm) ) {
				response.put("status", "error");
				response.put("stepToGo", "2");
				response.put("error", "PASSWORD_MISMATCH");
				response.put("message", "Password and confirm password do not match.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			
			// VERIFICATION CODE CHECK
			if
			(
				session.getAttribute("forgotPasswordEmail") == null ||
				session.getAttribute("forgotPasswordCode") 	== null
			)
			{
				response.put("status", "fail");
				response.put("stepToGo", "2");
				response.put("error", "VERIFICATION_CODE_NOT_EXIST");
				response.put("message", "The verification code does not exist or has expired.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			String Email 				 = session.getAttribute("forgotPasswordEmail").toString();
			String EmailVerificationCode = session.getAttribute("forgotPasswordCode").toString();
			
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
			
			
			// PASSWORD ENCODING
			String encodedPassword = passwordEncoder.encode(userPassword);
			try
			{
				dao.updateUserPassword(userEmail, encodedPassword);
				session.setAttribute("forgotPasswordEmail", null);
				session.setAttribute("forgotPasswordCode", null);
				response.put("status", "success");
				response.put("message", "Your password has been successfully updated!");
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			catch(Exception e)
			{
				response.put("status", "fail");
				response.put("stepToGo", "2");
				response.put("error", "FAILED_TO_UPDATE_PASSWORD");
				response.put("message", "There was an error updating your password. Please try again.");	
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
