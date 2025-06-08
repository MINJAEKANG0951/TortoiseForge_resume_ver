package forge.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import forge.consts.LegalStuffTemporary;
import forge.model.DAO;
import forge.services.EmailService;
import forge.services.RecaptchaService;
import forge.util.UsernameChecker;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class Controller_Signup {

	private final DAO dao;
	private final EmailService emailService;
	private final RecaptchaService recaptchaService;
	private final PasswordEncoder passwordEncoder;
	
	private final int TERMS_AND_PRIVACY_AGREE_STEP 	= 1;
	private final int ACCOUNT_INFO_SUBMIT_STEP		= 2;
	private final int SIGNUP_STEP					= 3;
	
	public Controller_Signup
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
	
	
	@GetMapping("/signup")
	public String showSignUp(HttpSession session, Model model) {
		
		session.setAttribute("signupEmail", null);
		session.setAttribute("signupCode", null);
		
		model.addAttribute("privacyPolicy", LegalStuffTemporary.PRIVACY_POLICY);
		model.addAttribute("termsOfService", LegalStuffTemporary.TERMS_OF_SERVICE);
		
		return "signup";
	}
	

	@GetMapping("/signupComplete")
	public String showSignUpComplete() {
		return "signup_complete";
	}
	
	
	
	
	@PostMapping("/submitSignupInfo")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> 
	checkSignUpInfo
	(HttpServletRequest req, HttpSession session) 
	{
		
		Map<String,Object> response = new HashMap<>();
		int currentStep = 0;
		try
		{
			currentStep					= Integer.parseInt( req.getParameter("currentStep") );
			boolean termsOfservice 		= req.getParameter("termsOfService").equals("true");
			boolean privacyPolicy 		= req.getParameter("privacyPolicy").equals("true");
			String username				= req.getParameter("username");
			String userEmail			= req.getParameter("userEmail");
			String userPassword			= req.getParameter("userPassword");
			String userPasswordConfirm	= req.getParameter("userPasswordConfirm");
			String userVerificationCode	= req.getParameter("userVerificationCode");
			
			
			// RECAPTCHA     - 아직 활성화 x
			/*
			
			String recaptchaToken = req.getParameter("recaptchaToken");
			boolean isTheUserHuman = recaptchaService.isTheUserHuman(recaptchaToken);
			if(!isTheUserHuman) {
				response.put("status", "fail");
				response.put("error", "SUSPICIOUS_REQUEST");
				response.put("message", "The request has been denied because suspicious activity has been detected. lease try again.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			*/
			
			if
			(
				currentStep != TERMS_AND_PRIVACY_AGREE_STEP &&
				currentStep != ACCOUNT_INFO_SUBMIT_STEP		&&
				currentStep != SIGNUP_STEP
			) 
			{
				response.put("status", "error");
				response.put("stepToGo", "1");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Please follow the specified signup procedure.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
				
			
			if(!termsOfservice || !privacyPolicy) {
				response.put("status", "error");
				response.put("stepToGo", "1");
				response.put("error", "TERMS_AND_PRIVACY_NOT_ACCEPTED");
				response.put("message", "You must accept the terms of service and privacy policy.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			// STEP 1 -> STEP 2
			if(currentStep == TERMS_AND_PRIVACY_AGREE_STEP) {
				response.put("status", "success");
				response.put("stepToGo", "2");
				response.put("message", "Please enter a username, password and email address to sign up. A verification code will be sent to your email.");
				return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
			}
			
			// USERNAME CHECK
			if(!UsernameChecker.checkUsernameLength(username)) {
				response.put("status", "error");
				response.put("stepToGo", "2");
				response.put("error", "USERNAME_TOO_SHORT_OR_LONG");
				response.put("message", "Username must be between 5 and 15 characters long.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(!UsernameChecker.checkUsernameValid(username)) {
				response.put("status", "error");
				response.put("stepToGo", "2");
				response.put("error", "INVALID_CHARACTER");
				response.put("message", "Username can only include alphabets, numbers and underscore.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(dao.getUser(username)!=null) {
				response.put("status", "fail");
				response.put("stepToGo", "2");
				response.put("error", "USERNAME_ALREADY_EXISTS");
				response.put("message", "The username has been already taken.");
				return new ResponseEntity<>(response, HttpStatus.CONFLICT);
			}
			
			
			
			// PASSWORD CHECK
			if(!userPassword.equals(userPasswordConfirm) ) {
				response.put("status", "error");
				response.put("stepToGo", "2");
				response.put("error", "PASSWORD_MISMATCH");
				response.put("message", "Password and confirm password do not match.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if( userPassword.length() < 4 ) {
				response.put("status", "error");
				response.put("stepToGo", "2");
				response.put("error", "PASSWORD_TOO_SHORT");
				response.put("message", "Password must be at least 4 characters long.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			
			
			// EMAIL CHECK
			if (dao.getUserByEmail(userEmail)!=null) {
				response.put("status", "fail");
				response.put("stepToGo", "2");
				response.put("error", "USER_EMAIL_ALREADY_EXISTS");
				response.put("message", "The email has been already used to create an account.");
				return new ResponseEntity<>(response, HttpStatus.CONFLICT);
			}
			
			
			// STEP2 -> STEP3
			if(currentStep == ACCOUNT_INFO_SUBMIT_STEP) {
				
				String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0,7);
				
				try
				{
					emailService.sendEmail(userEmail, randomStr, "TORTOISE FORGE EMAIL VERIFICATION CODE");
					session.setAttribute("signupEmail", userEmail);
					session.setAttribute("signupCode", randomStr);
				}
				catch(Exception e)
				{
					response.put("status", "fail");
					response.put("stepToGo", "2");
					response.put("error", "FAILED_TO_SEND_VERFICATION_CODE");
					response.put("message", "Failed to send a verfication code to your email.");
					return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				
				response.put("status", "success");
				response.put("stepToGo", "3");
				response.put("message", "The verification code has been sent to the email, " + userEmail  + ". Please enter the code.");
				return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
			}
			
			
			// EMAIL SESSION CHECK
			if(session.getAttribute("signupEmail") == null || session.getAttribute("signupCode") == null) {
				response.put("status", "fail");
				response.put("stepToGo", "3");
				response.put("error", "VERIFICATION_CODE_NOT_EXIST");
				response.put("message", "The verification code does not exist or has expired.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			String Email 					= session.getAttribute("signupEmail").toString();
			String EmailVerificationCode 	= session.getAttribute("signupCode").toString();
			
			if(!Email.equals(userEmail)) {
				response.put("status", "fail");
				response.put("stepToGo", "3");
				response.put("error", "NOT_THE_SAME_EMAIL");
				response.put("message", "The email is not the same email we've sent the verification code to.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(!EmailVerificationCode.equals(userVerificationCode)) {
				response.put("status", "fail");
				response.put("stepToGo", "3");
				response.put("error", "WRONG_VERIFICATION_CODE");
				response.put("message", "The verification code is wrong.");	
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			
			// PASSWORD ENCODING
			String encodedPassword = passwordEncoder.encode(userPassword);

			// TRY NEW USER ACCOUNT CREATION
			int result = dao.createUser(username, encodedPassword, userEmail);
			if(result == 2)
			{
				response.put("status", "fail");
				response.put("error", "USER_EMAIL_ALREADY_EXISTS");
				response.put("stepToGo", "2");
				response.put("message", "The email has been already used to create an account.");
				return new ResponseEntity<>(response, HttpStatus.CONFLICT);
			} 
			else if(result == 1) 
			{
				response.put("status", "fail");
				response.put("error", "USERNAME_ALREADY_EXISTS");
				response.put("stepToGo", "2");
				response.put("message", "The username has been already taken.");
				return new ResponseEntity<>(response, HttpStatus.CONFLICT);
			}
			else if(result == 0)
			{
				session.setAttribute("signupEmail", null);
				session.setAttribute("signupCode", null);
				response.put("status", "success");
				response.put("message", "The user account has been successfully created!");
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			else 
			{
				response.put("status", "error");
				response.put("stepToGo", "3");
				response.put("error", "ERROR_CREATING_USER_ACCOUNT");
				response.put("message", "Failed to create a new account. Please try again.");
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
