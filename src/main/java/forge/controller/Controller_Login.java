package forge.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import forge.consts.Consts_General;
import forge.model.DAO;
import forge.model.UserDTO;
import forge.services.EmailService;
import forge.services.RecaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class Controller_Login {

	private final DAO dao;
	private final EmailService emailService;
	private final RecaptchaService recaptchaService;
	private final PasswordEncoder passwordEncoder;

	public Controller_Login(DAO dao, EmailService emailService, RecaptchaService recaptchaService,
			PasswordEncoder passwordEncoder) {
		this.dao = dao;
		this.emailService = emailService;
		this.recaptchaService = recaptchaService;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping("/login")
	public String showLogin(HttpServletRequest req, Model model) {

		if (req.getParameter("username") != null) {
			model.addAttribute("username", req.getParameter("username"));
		}

		return "login";
	}

	@GetMapping("/logout")
	public String doLogout(HttpSession session) { // request logout 은 만들 필요 X
		session.setAttribute("userSeq", null);
		session.setAttribute("username", null);
		return "redirect:/";
	}

	@GetMapping("/loginAlready")
	public String showLoginAlready(HttpSession session) {

		if (session.getAttribute("userSeq") == null) {
			return "redirect:/";
		}

		return "login_already";
	}

	@PostMapping("/requestLogin")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> doLogin(HttpSession session, HttpServletRequest req) {

		Map<String, Object> response = new HashMap<>();

		try {

			// RECAPTCHA - 아직 활성화 x
			/*
			 * 
			 * String recaptchaToken = req.getParameter("recaptchaToken"); boolean
			 * isTheUserHuman = recaptchaService.isTheUserHuman(recaptchaToken);
			 * if(!isTheUserHuman) { response.put("status", "fail"); response.put("error",
			 * "SUSPICIOUS_REQUEST"); response.put("message",
			 * "The request has been denied because suspicious activity has been detected. Please try again."
			 * ); return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); }
			 * 
			 */

			String username = req.getParameter("username");
			String password = req.getParameter("password");

			if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
				response.put("status", "fail");
				response.put("error", "LOGIN_INFO_INCOMPLETE");
				response.put("message", "Please provide all necessary information to log in.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			UserDTO userInfo = dao.getUser(username);
			if (userInfo == null) {
				response.put("status", "fail");
				response.put("error", "USERNAME_NOT_FOUND");
				response.put("message", "The username does not exist.");
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}

			if (!passwordEncoder.matches(password, userInfo.getUserPassword())) {
				response.put("status", "fail");
				response.put("error", "WRONG_PASSWORD");
				response.put("message", "The password is incorrect.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			// 2025-03-28 삭제요청 계정에 대한 처리
			if (userInfo.getUserStatus() == Consts_General.USER_STATUS_SCHEDULED_FOR_DELETION) {
				response.put("status", "fail");
				response.put("error", "ACCOUNT_PENDING_DELETION");
				response.put("message",
						"Your account is scheduled for deletion. Would you like to cancel it and log in?");
				return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
			}

			if (userInfo.getUserStatus() == Consts_General.USER_STATUS_SCHEDULED_FOR_DEACTIVATION) {
				response.put("status", "fail");
				response.put("error", "ACCOUNT_PENDING_DELETION");
				response.put("message",
						"Your account is scheduled for deactivation. Would you like to cancel it and log in?");
				return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
			}

			// 필터링 끝나면 session 생성
			session.setAttribute("userSeq", userInfo.getUserSeq());
			session.setAttribute("username", userInfo.getUsername());

			response.put("status", "success");
			return new ResponseEntity<>(HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			response.put("status", "fail");
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "An unknown error occurred. Please try again.");
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PostMapping("/requestAccountActivation")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> activateAccount(HttpSession session, HttpServletRequest req) {

		Map<String, Object> response = new HashMap<>();

		try 
		{
			
			String username = req.getParameter("username");
			String password = req.getParameter("password");

			if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
				response.put("status", "fail");
				response.put("error", "ACCOUNT_ACTIVATION_INFO_INCOMPLETE");
				response.put("message", "Please provide all necessary information to active your account.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			UserDTO userInfo = dao.getUser(username);
			if (userInfo == null) {
				response.put("status", "fail");
				response.put("error", "USERNAME_NOT_FOUND");
				response.put("message", "The username does not exist.");
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}

			if (!passwordEncoder.matches(password, userInfo.getUserPassword())) {
				response.put("status", "fail");
				response.put("error", "WRONG_PASSWORD");
				response.put("message", "The password is incorrect.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			if 
			(
				userInfo.getUserStatus() != Consts_General.USER_STATUS_SCHEDULED_FOR_DEACTIVATION	&&
				userInfo.getUserStatus() != Consts_General.USER_STATUS_SCHEDULED_FOR_DELETION 
			) 
			{
				response.put("status", "fail");
				response.put("error", "BAD_REQUEST");
				response.put("message", "This request is only allowed when your account is scheduled for deactivation or deletion.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
				
	
			dao.updateUserStatus(userInfo.getUserSeq(), Consts_General.USER_STATUS_ACTIVE);
			response.put("status", "success");
			return new ResponseEntity<>(HttpStatus.OK);
			

		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			response.put("status", "fail");
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "An unknown error occurred. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}


	}

}
