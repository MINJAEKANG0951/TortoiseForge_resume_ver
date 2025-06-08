package forge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class Controller_WorkDone {

	@GetMapping("/workDone")
	public String showWorkDoneBase(Model model) { // Base
		model.addAttribute("largeText","COMPLETE");
		model.addAttribute("smallText","The work is successfully done!");
		return "work_done";
	}
	
	@GetMapping("/workDone/{work}")
	public String showWorkDone(Model model, @PathVariable("work") String work) { // Base
		
		model.addAttribute("largeText","COMPLETE");
		model.addAttribute("smallText","The work is successfully done!");
		
		if(work.equals("password"))
		{
			model.addAttribute("mode","password");
			model.addAttribute("largeText","COMPLETE");
			model.addAttribute("smallText","Your password has been successfully changed!");
			model.addAttribute("imgPath","/img/original/lock_shiny.png");
		}
		else if(work.equals("username"))
		{
			model.addAttribute("mode","username");
			model.addAttribute("largeText","COMPLETE");
			model.addAttribute("smallText","Your username has been sent to your email!");
			model.addAttribute("imgPath","/img/original/tortoise_sending_message.png");
		}
		else if(work.equals("signup"))
		{
			model.addAttribute("mode","signup");
			model.addAttribute("largeText","COMPLETE");
			model.addAttribute("smallText","You have successfully signed up!");
			model.addAttribute("imgPath","/img/original/tortoise_chilling_on_water.png");
		}
		else if(work.equals("postCreatedButFileUploadFailed"))
		{
			model.addAttribute("mode","postCreatedButFileUploadFailed");
			model.addAttribute("largeText","FAILED TO UPLOAD IMAGES");
			model.addAttribute("smallText","Post created, but some images failed to upload.");
			model.addAttribute("imgPath","/img/original/tortoise_questioning.png");
		}
		
		return "work_done";
	}
	
	
	
	// 나중에..
	
	@PostMapping("/workDone/signup")
	public String showSignupDone(Model model) {
		
		model.addAttribute("largeText","COMPLETE");
		model.addAttribute("smallText","You have successfully signed up!");
		model.addAttribute("imgPath","/img/original/tortoise_chilling_on_water.png");
		
		return "work_done";
	}
	
	
	@PostMapping("/workDone/passwordChange")
	public String showPasswordChangeDone(Model model) {
		
		model.addAttribute("largeText","COMPLETE");
		model.addAttribute("smallText","Your password has been successfully changed!");
		model.addAttribute("imgPath","/img/original/lock_shiny.png");
		
		return "work_done";
	}
	
	
	@PostMapping("/workDone/usernameChange")
	public String showUsernameChangeDone(Model model) {
		
		model.addAttribute("largeText","COMPLETE");
		model.addAttribute("smallText","You have successfully signed up!");
		model.addAttribute("imgPath","/img/original/tortoise_chilling_on_water.png");
		
		return "work_done";
	}
	
	
	
	
	
}
