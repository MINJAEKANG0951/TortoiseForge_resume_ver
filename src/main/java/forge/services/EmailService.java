package forge.services;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
	
	public void sendEmail(	String to,	
							String content,
							String subject ) throws MessagingException 
	{
		MimeMessage mimeMessage 	= mailSender.createMimeMessage();
		MimeMessageHelper helper	= new MimeMessageHelper(mimeMessage, true);
		
		helper.setFrom("the_relaxed_tortoise@naver.com");
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(content, true);
			
		// image 보내는건 나중에하자
		
		mailSender.send(mimeMessage);
	}
	
}
