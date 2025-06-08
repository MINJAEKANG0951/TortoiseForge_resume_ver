package forge.services;

import org.springframework.stereotype.Service;

@Service
public class TextService {

	// To Render with Thymeleaf 
	public String toThymeleafForm(String text) {
		
		
		// when rendering, browser remove first character if the text starts with \n or \r\n
		if(text == null) {	return null; }
		if(text.startsWith("\n")) {	return "\n" + text; }
		if(text.startsWith("\r\n")) { return "\r\n" + text; }
		
		return text;
		
	}
	
}
