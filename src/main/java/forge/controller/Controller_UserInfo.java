package forge.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import forge.consts.Consts_General;
import forge.consts.Consts_Messages;
import forge.consts.Consts_Report;
import forge.model.DAO;
import forge.model.UserDTO;
import forge.services.AESService;
import forge.services.RecaptchaService;
import forge.services.UserMessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


@Controller
public class Controller_UserInfo {
	
	
	private final DAO dao;
	private final AESService aesService;
	private final RecaptchaService recaptchaService;
	private final UserMessageService userMessageService;
	
	public Controller_UserInfo
	(	
		DAO dao,
		AESService aesService,
		RecaptchaService recaptchaService,
		UserMessageService userMessageService
	) 
	{
		this.dao = dao;
		this.aesService = aesService;
		this.recaptchaService = recaptchaService;
		this.userMessageService = userMessageService;
	}
	
	
	
	/*
		userInfoPage
		유저의 간단한 정보(email 말고 아이디랑 등급)를 보여주고,
		다른 유저에게 쪽지를 보낼 수 있거나 해당 유저를 신고할 수 있는 기능을 담을 예정임
		일단 메시지 보내는것만 구현해놓을것.
	
		메시지를 보내는 코드는 userInfoPage / messagePage 둘 다 에서 가능해야 하므로, 
		service class 새로 만들어서 코드를 공유하도록 하거나, 
		procedure 를 만들어서 구현하던가 하자.
	
	*/
	
	@GetMapping("/userInfo/{userSeq}")
	public String showUserInfo
	(
		HttpSession session, 
		@PathVariable("userSeq") int userSeq,
		Model model
	) 
	{
		UserDTO user = dao.getUserBySeq(userSeq);
		if(user == null) {
			model.addAttribute("message", "The user does not exist.");
			return "error";
		}
		
		if
		(
			session.getAttribute("userSeq") != null &&
			user.getUserSeq() == Integer.parseInt(session.getAttribute("userSeq").toString())
		)
		{
			return "redirect:/accountSetting";
		}
		
		model.addAttribute("userSeq", user.getUserSeq());
		model.addAttribute("username",	user.getUsername());
		model.addAttribute("userType", Consts_General.USER_TYPE_STRING.get(user.getRoleSeq()));
		
		model.addAttribute("headerImgPath", "/img/original/title_userinfo.png");
		model.addAttribute("headerIcon", 	"/img/original/profile.png");
		model.addAttribute("headerTitle", 	"USER INFO");
		
		model.addAttribute("maxMessageLength", Consts_Messages.MAX_MESSAGE_LENGTH);
		model.addAttribute("maxReportDetailsLength", Consts_Report.REPORT_DETAILS_MAX_LENGTH);
		
		return "userinfo";
	}
	
	
	
	
	
	
	@PostMapping("/userInfo/message")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> UserInfo_SendMessage
	(
		HttpSession session, 
		@RequestParam(name="recipientSeq") int recipientSeq,
		@RequestParam(name="message") String message
	)
	{

		Map<String,Object> response = new HashMap<>();
		
		try
		{
			if(session.getAttribute("userSeq") == null) {
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "You must be logged in to send a message.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			int senderSeq = Integer.parseInt(session.getAttribute("userSeq").toString());
			return userMessageService.sendMessage(senderSeq, recipientSeq, message);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.put("status", "error");
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "An unknown error occurred. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

	}
	
	
	
	
	
	@PostMapping("/userInfo/report")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> UserInfo_Report
	(
		HttpSession session, 
		HttpServletRequest req,
		@RequestParam(name="targetSeq") int targetSeq,
		@RequestParam(name="reportDetails") String reportDetails
	)
	{

		Map<String,Object> response = new HashMap<>();
		
		try
		{
			if(session.getAttribute("userSeq") == null) {
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "You must be logged in to send a message.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			UserDTO reporter = dao.getUserBySeq(
				Integer.parseInt(session.getAttribute("userSeq").toString())
			);
			
			UserDTO target = dao.getUserBySeq(targetSeq);
			
			if(target == null) {
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
			    response.put("message", "The target account does not exist.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(target.getUserStatus() == Consts_General.USER_STATUS_DEACTIVATED) 
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
		        response.put("message", "The target account has been deleted or deactivated.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			
			if (reportDetails == null || reportDetails.isEmpty()) {
			    response.put("status", "error");
			    response.put("error", "BAD_REQUEST");
			    response.put("message", "Please provide report details.");
			    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			reportDetails = reportDetails.replace("\r\n", "\n");

			if (reportDetails.length() > Consts_Report.REPORT_DETAILS_MAX_LENGTH) {
			    response.put("status", "error");
			    response.put("error", "BAD_REQUEST");
			    response.put("message",
			        "The report details must not exceed " +
			        Consts_Report.REPORT_DETAILS_MAX_LENGTH + " characters."
			    );
			    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			dao.report(
				Consts_Report.REPORT_STATUS_PENDING,
				reporter.getUserSeq(), 
				target.getUserSeq(), 
				reportDetails
			);
			
			response.put("status", "success");
			response.put("message", "You have successfully reported the user.");
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.put("status", "error");
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "An unknown error occurred. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

	}
	
	
	
	
	
	
}
