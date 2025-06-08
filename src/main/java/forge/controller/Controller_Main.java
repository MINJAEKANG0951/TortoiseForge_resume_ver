package forge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Controller_Main {

	@GetMapping("/")
	public String showMain() {
		return "main";
	}
	
	@GetMapping("/privacy")
	public String showPrivacyPolicy() {
		return "privacy_policy";
	}
	
	@GetMapping("/terms")
	public String showTermsOfService() {
		return "terms_of_service";
	}
	
	
}
