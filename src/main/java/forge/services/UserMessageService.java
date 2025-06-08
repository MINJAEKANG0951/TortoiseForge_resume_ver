package forge.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import forge.consts.Consts_General;
import forge.consts.Consts_Messages;
import forge.model.DAO;
import forge.model.UserDTO;

@Service
public class UserMessageService {
	
	private final DAO dao;
	private final AESService aesService;
	
	public UserMessageService(DAO dao, AESService aesService) 
	{
		this.dao = dao;
		this.aesService = aesService;
	}
	
	public ResponseEntity<Map<String,Object>> sendMessage(int senderSeq, int recipientSeq, String message)
	{
		Map<String,Object> response = new HashMap<>();
		try
		{
			
			UserDTO sender = dao.getUserBySeq(senderSeq);
			UserDTO recipient = dao.getUserBySeq(recipientSeq);
			
			if(recipient == null) {
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Recipient account does not exist.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(senderSeq == recipientSeq) {
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Sending messages to yourself is not allowed.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(recipient.getUserStatus() == Consts_General.USER_STATUS_DEACTIVATED) 
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Recipient account is deleted.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			
			if (message == null) {
			    response.put("status", "error");
			    response.put("error", "BAD_REQUEST");
			    response.put("message", "Please enter a message.");
			    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			message = message.trim();
			message = message.replace("\r\n", "\n");

			if(message.isEmpty()) {
			    response.put("status", "error");
			    response.put("error", "BAD_REQUEST");
			    response.put("message", "Please enter a message.");
			    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if (message.length() > Consts_Messages.MAX_MESSAGE_LENGTH) {
			    response.put("status", "error");
			    response.put("error", "BAD_REQUEST");
			    response.put("message",
			        "The message must not exceed " +
			        Consts_Messages.MAX_MESSAGE_LENGTH + " characters."
			    );
			    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			String encryptedMessage = aesService.encrypt(message);
			dao.sendMessage(sender.getUserSeq(), recipient.getUserSeq(), encryptedMessage);
			
			response.put("status", "success");
			response.put("message", "Your message has been sent successfully.");
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
