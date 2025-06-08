package forge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Controller_Creations {

	@GetMapping("/creations")
	public String showCreations(Model model) {
		
		model.addAttribute("headerTitle", 	"FORGE CREATIONS");
		model.addAttribute("headerImgPath", "/img/original/title_creations.png");
		
		return "creations";
	}
	
}
