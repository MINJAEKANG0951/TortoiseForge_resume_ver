package forge.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import forge.consts.Consts_General;
import forge.model.UserDTO;
import forge.services.DAOService;
import forge.services.EmailService;
import forge.services.FileStorageService;
import forge.services.MimeTypeCheckService;
import forge.services.RecaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class Controller_AccountSetting {

	private final DAOService daoService;
	private final EmailService emailService;
	private final RecaptchaService recaptchaService;
	private final PasswordEncoder passwordEncoder;
	private final FileStorageService fileStorageService;
	private final MimeTypeCheckService mimeTypeCheckService;
	
	
	public Controller_AccountSetting
	(	
		DAOService daoService,
		EmailService emailService,
		RecaptchaService recaptchaService,
		PasswordEncoder	passwordEncoder,
		FileStorageService fileStorageService,
		MimeTypeCheckService mimeTypeCheckService
	) 
	{
		this.daoService = daoService;
		this.emailService = emailService;
		this.recaptchaService = recaptchaService;
		this.passwordEncoder = passwordEncoder;
		this.fileStorageService = fileStorageService;
		this.mimeTypeCheckService = mimeTypeCheckService;
	}
	
	
	
	
	@GetMapping("/accountSetting")
	public String showAccountSetting(Model model, HttpSession session) {
		
		if(session.getAttribute("userSeq") == null) {
			return "redirect:/login";
		}
		
		int userSeq = Integer.parseInt(session.getAttribute("userSeq").toString());
		UserDTO user = daoService.getUserBySeq(userSeq);
		
		model.addAttribute("username",	user.getUsername());
		model.addAttribute("userEmail", user.getUserEmail());
		model.addAttribute("userType", Consts_General.USER_TYPE_STRING.get(user.getRoleSeq()));
		
		
		model.addAttribute("headerImgPath", "/img/original/title_accountSetting.png");
		model.addAttribute("headerIcon", "/img/original/profile.png");
		model.addAttribute("headerTitle", "ACCOUNT SETTING");
		
		return "account_setting";
	};
	

	
	
	@PutMapping("/accountSetting/password")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> 
	AccountSetting_ChangePassword
	(HttpServletRequest req, HttpSession session)
	{
		
		Map<String,Object> response = new HashMap<>();
		
		try
		{
			String currentPassword 		= req.getParameter("currentPassword");
			String newPassword 			= req.getParameter("newPassword");
			String newPasswordConfirm 	= req.getParameter("newPasswordConfirm");
			
			
			// LOGIN CHECK
			if(session.getAttribute("userSeq") == null) {
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Please login to change your password.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			int userSeq 	= Integer.parseInt(session.getAttribute("userSeq").toString());
			UserDTO user 	= daoService.getUserBySeq(userSeq);
			
			
			// PASSWORD CHECK
			if
			(
				currentPassword == null 	||	currentPassword.length() == 0 		||
				newPassword == null			||	newPassword.length() == 0			||
				newPasswordConfirm == null	||	newPasswordConfirm.length() == 0
			) 
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Enter your current password and new password.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			
			if(!passwordEncoder.matches(currentPassword, user.getUserPassword())) {
			    response.put("status", "error");
			    response.put("error", "INVALID_CURRENT_PASSWORD");
			    response.put("message", "The current password is incorrect.");
			    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			
			if(newPassword.length() < 4) {
				response.put("status", "error");
				response.put("error", "PASSWORD_TOO_SHORT");
				response.put("message", "Password must be at least 4 characters long.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(!newPassword.equals(newPasswordConfirm) ) {
				response.put("status", "error");
				response.put("error", "PASSWORD_MISMATCH");
				response.put("message", "Password and confirm password do not match.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			
			/*
			 	0.로그인상태인지 확인
				1.현재패스워드 맞는지 확인
				2.패스워드 길이 4이상인지확인
				3.패스워드 == 패스워드확인 인지 확인
				4.패스워드 변경승인
			*/
			
			daoService.updatePassword(userSeq, passwordEncoder.encode(newPassword));
			
			response.put("status", "success");
			response.put("message", "Your password has been changed successfully!");
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.put("status", "error");
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "An unknown error occurred. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	
	
	
	
	@DeleteMapping("/accountSetting/account")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> 
	AccountSetting_DeleteUser
	(HttpServletRequest req, HttpSession session) 
	{
		
		/*
			유저 status 만 변경,
			유저가 업로드한 파일, 데이터베이스 내 유저 데이터는 유저의 삭제 방식에 따라 
			나중에 삭제 및 유지											> scheduler 로 작업
		*/
		
		Map<String,Object> response = new HashMap<>();
		
		try
		{
			// LOGIN CHECK
			if(session.getAttribute("userSeq") == null) {
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Please login to delete your account.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}	
			
			int userSeq = Integer.parseInt(session.getAttribute("userSeq").toString());
			UserDTO user = daoService.getUserBySeq(userSeq);
			
			if
			(
				req.getParameter("userPassword") == null 					|| 
				req.getParameter("userPassword").toString().length() == 0
			) 
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Enter your current password.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(req.getParameter("wayToDeleteAccount") == null) {
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Select a way to delete your account.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			String 	userPassword		= req.getParameter("userPassword").toString();
			Integer wayToDeleteAccount 	= Integer.parseInt(req.getParameter("wayToDeleteAccount"));
			
			
			if(!passwordEncoder.matches(userPassword, user.getUserPassword())) {
			    response.put("status", "error");
			    response.put("error", "INVALID_CURRENT_PASSWORD");
			    response.put("message", "The current password is incorrect.");
			    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(user.getUserStatus() != Consts_General.USER_STATUS_ACTIVE) {
			    response.put("status", "error");
			    response.put("error", "BAD_REQUEST");
			    response.put("message", "The account state must be active to make this request.");
			    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(wayToDeleteAccount == Consts_General.ACCOUNT_DELETION_TYPE_ANONYMIZE)
			{
				daoService.updateUserStatus(userSeq, Consts_General.USER_STATUS_SCHEDULED_FOR_DEACTIVATION);
				response.put("status", "success");
				response.put("message", "You have successfully requested account anonymization.");
				return new ResponseEntity<>(response, HttpStatus.OK);
			}	
			else if(wayToDeleteAccount == Consts_General.ACCOUNT_DELETION_TYPE_DELETE)
			{
				daoService.updateUserStatus(userSeq, Consts_General.USER_STATUS_SCHEDULED_FOR_DELETION);
				response.put("status", "success");
				response.put("message", "You have successfully requested account deletion.");
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			else
			{
			    response.put("status", "error");
			    response.put("error", "INVALID_ACCOUNT_DELETION_TYPE");
			    response.put("message", "The submitted account deletion type is invalid.");
			    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}


		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.put("status", "error");
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "An unknown error occurred. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	
	
	
}
