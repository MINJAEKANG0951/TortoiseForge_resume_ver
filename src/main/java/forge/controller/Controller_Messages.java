package forge.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import forge.consts.Consts_Messages;
import forge.model.UserDTO;
import forge.model.messages.MessageDTO;
import forge.model.messages.custom.MessageSearchResult;
import forge.services.AESService;
import forge.services.DAOService;
import forge.services.RecaptchaService;
import forge.services.TextService;
import forge.services.UserMessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class Controller_Messages {

	private static final Logger logger = LoggerFactory.getLogger(Controller_Messages.class);
	
	private final DAOService daoService;
	private final UserMessageService userMessageService;
	private final RecaptchaService recaptchaService;
	private final AESService aesService;
	private final TextService textService;
	
	public Controller_Messages
	(
		DAOService daoService,
		UserMessageService userMessageService,
		RecaptchaService recaptchaService,
		AESService aesService,
		TextService textService
	) 
	{
		this.daoService = daoService;
		this.userMessageService = userMessageService;
		this.recaptchaService = recaptchaService;
		this.aesService = aesService;
		this.textService = textService;
	}
	
	
	public static final String MESSAGE_TYPE_INBOX	=	"0";
	public static final String MESSAGE_TYPE_SENT	=	"1";
	
	public static final int PAGE_SIZE_TYPE1	=	10;	
	
	public static final int HOW_MANY_PAGES_AROUND = 5;
	
	
	@GetMapping("/messages")
	public String showMessages
	(
		@RequestParam(value = "messageType", 	required = false) 	String messageType,
	    @RequestParam(value = "pageNumber", 	defaultValue = "1") int pageNumber,
		Model model,
		HttpSession session, 
		HttpServletRequest req
	)
	{
		try
		{
			if(session.getAttribute("userSeq") == null) {
				return "redirect:/login";
			}
			
			int pageSize = PAGE_SIZE_TYPE1;
			
			model.addAttribute("headerImgPath",	"/img/original/title_messages.png");
			model.addAttribute("headerTitle",	"MESSAGES");
			model.addAttribute("headerIcon",	"/img/original/message.png");
			model.addAttribute("maxMessageLength", Consts_Messages.MAX_MESSAGE_LENGTH);
			
			Map<Integer,String> pageSizeTypeKeyAndVal 	= new LinkedHashMap<>();

			model.addAttribute("inboxMessageType", 	MESSAGE_TYPE_INBOX);
			model.addAttribute("sentMessageType", 	MESSAGE_TYPE_SENT);
			
			pageSizeTypeKeyAndVal.put(PAGE_SIZE_TYPE1,	PAGE_SIZE_TYPE1 + " items");
			
			model.addAttribute("pageSizeTypes", pageSizeTypeKeyAndVal);
			
			int userSeq = Integer.parseInt(session.getAttribute("userSeq").toString());
			UserDTO user = daoService.getUserBySeq(userSeq);
			
			MessageSearchResult messageSearchResult = daoService.getUserMessages(userSeq, messageType, pageNumber, pageSize);

			decipherMesasges(messageSearchResult.getMessages());
			
			// SSR 때만 필요
			for(MessageDTO message : messageSearchResult.getMessages()) {
				message.setMessageContent(
					textService.toThymeleafForm(message.getMessageContent())
				);
			}
			
			model.addAttribute("currentPageNum", messageSearchResult.getCurrentPageNum());
			model.addAttribute("currentPageSize", messageSearchResult.getCurrentPageSize());
			
			model.addAttribute("totalNumberOfPages", messageSearchResult.getTotalNumberOfPages());
			model.addAttribute("totalNumberOfItems", messageSearchResult.getTotalNumberOfItems());

			model.addAttribute("messageType", messageSearchResult.getMessageType());

			model.addAttribute("messages",	messageSearchResult.getMessages());	
			
			for(int i=0;i<messageSearchResult.getMessages().size();i++) {
				
			}
			

			model.addAttribute("pagesAround", messageSearchResult.getPagesAround());

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return "messages";
	}
	
	
	
	
	@GetMapping("/messages/load")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> messages_getNewMessages
	(
		@RequestParam(value = "messageType", 	required = false) 	String messageType,
	    @RequestParam(value = "pageNumber", 	defaultValue = "1") int pageNumber,
	    HttpSession session
	) 
	{
		Map<String,Object> response = new HashMap<>();
		
		try
		{
			if(session.getAttribute("userSeq") == null) {
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Please login to load messages(Your session has expired. Please log in again).");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			int userSeq 	= Integer.parseInt(session.getAttribute("userSeq").toString());
			
			MessageSearchResult messageSearchResult = daoService.getUserMessages(userSeq, messageType, pageNumber, PAGE_SIZE_TYPE1);
			decipherMesasges(messageSearchResult.getMessages());
			
			response.put("status", "success");
			response.put("messageSearchResult", messageSearchResult);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		catch(Exception e)
		{
			response.put("status", "error");
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "Failed to load messages. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	
	@PostMapping("/messages/message")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> messages_sendMessage
	(
		HttpSession session, 
		@RequestParam(name="recipientName") String recipientName,
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
			
			UserDTO recipient = daoService.getUser(recipientName);
			
			if(recipient == null) {
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "The recipient does not exist.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			int senderSeq = Integer.parseInt(session.getAttribute("userSeq").toString());
	
			return userMessageService.sendMessage(
					senderSeq, 
					recipient.getUserSeq(), 
					message
			);
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
	
	
	@DeleteMapping("/messages/message")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> messages_deleteMessages 
	(
		@RequestParam(name="messageSeqs",	required = false)	List<Integer> messageSeqs,
		HttpSession session
	) 
	{
		Map<String,Object> response = new HashMap<>();
		
		try
		{
			if(session.getAttribute("userSeq") == null) {
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "You must be logged in to update the messages.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(messageSeqs == null) {messageSeqs = new ArrayList<>();}
			
			int userSeq = Integer.parseInt(session.getAttribute("userSeq").toString());
			
			daoService.deleteUserMessages(messageSeqs, userSeq);
			
			response.put("status", "success");
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
	
	
	@PatchMapping("/messages/message")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> messages_markAsReadOrUnread
	(
		HttpSession session,
		@RequestParam(name="messageSeqs", required = false) List<Integer> messageSeqs,
		@RequestParam(name="markAsRead", defaultValue = "true") boolean markAsRead				
	) 
	{
		Map<String,Object> response = new HashMap<>();
		
		try
		{
			if(session.getAttribute("userSeq") == null) {
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "You must be logged in to update the messages.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(messageSeqs == null) {messageSeqs = new ArrayList<>();}
			
			int userSeq = Integer.parseInt(session.getAttribute("userSeq").toString());
			
			daoService.updateUserMessages(messageSeqs, userSeq, markAsRead);	
			
			response.put("status", "success");
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
	
	
	@GetMapping("/messages/check")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> message_checkNewMessages(HttpSession session)
	{
		Map<String,Object> response = new HashMap<>();
		
		try
		{
			if(session.getAttribute("userSeq") == null) {
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "You must be logged in to check new messages.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			int userSeq = Integer.parseInt(session.getAttribute("userSeq").toString());
			boolean isThereNewMessages = daoService.checkNewMessages(userSeq);
			
			response.put("status", "success");
			response.put("isThereNewMessage", isThereNewMessages);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		catch(Exception e)
		{
			logger.error("An error occurred while checking for new messages");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}
	
	
	
	
	private List<MessageDTO> decipherMesasges(List<MessageDTO> messages) {
		for(MessageDTO message : messages) 
		{
			try 
			{
				message.setMessageContent(aesService.decrypt(message.getMessageContent()));
				message.setMessagePreview(getMessagePreview(message.getMessageContent()));
			} 
			catch(Exception e) 
			{
				message.setMessageContent(null);
				message.setMessagePreview(null);
			}
		}
		return messages;
	}
	
	private String getMessagePreview(String messageContent) {
		if(messageContent == null || messageContent.isBlank()){
			return "";
		} 
		
		String[] parts = messageContent.split("\\R", 2);
		String firstLine = parts[0];
		
		return (parts.length > 1) ? firstLine + "..." : firstLine;
	}
	
}
