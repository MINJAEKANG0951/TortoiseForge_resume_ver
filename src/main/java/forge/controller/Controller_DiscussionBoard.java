package forge.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import forge.model.discussionboard.custom.BoardSearchResult;
import forge.services.DAOService;
import forge.services.EmailService;
import forge.services.RecaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class Controller_DiscussionBoard {
	
	private final DAOService daoService;
	private final EmailService emailService;
	private final RecaptchaService recaptchaService;
	private final PasswordEncoder passwordEncoder;
	
	public Controller_DiscussionBoard
	(	
		DAOService daoService,
		EmailService emailService,
		RecaptchaService recaptchaService,
		PasswordEncoder	passwordEncoder
	) 
	{
		this.daoService = daoService;
		this.emailService = emailService;
		this.recaptchaService = recaptchaService;
		this.passwordEncoder = passwordEncoder;
	}
	
	public static final String SEARCH_TYPE_TITLE_AND_CONTENT	=	"1";
	public static final String SEARCH_TYPE_TITLE				=	"2";
	public static final String SEARCH_TYPE_CONTENT				=	"3";
	public static final String SEARCH_TYPE_USER					=	"4";
	public static final String SEARCH_TYPE_COMMENT				=	"5";

	public static final int PAGE_SIZE_TYPE1	=	10;
	public static final int PAGE_SIZE_TYPE2	=	25;
	public static final int PAGE_SIZE_TYPE3	=	50;
	public static final int PAGE_SIZE_TYPE4	=	75;
	public static final int PAGE_SIZE_TYPE5	=	100;
	
	public static final int HOW_MANY_PAGES_AROUND = 5;		// how many pages to show that are around the current page
	
	@GetMapping("/discussionBoard")
	public String showDiscussionBoard
	(
	    @RequestParam(value = "pageNumber", 	defaultValue = "1") 	int pageNumber,
	    @RequestParam(value = "pageSize", 		defaultValue = "10") 	int pageSize,
	    @RequestParam(value = "searchQuery", 	required = false) 	String searchQuery,	
	    @RequestParam(value = "searchType", 	required = false) 	String searchType,
		Model model
	) 
	{
		try
		{
			model.addAttribute("headerTitle", 	"DISCUSSION BOARD");
			model.addAttribute("headerLogoImgPath", "/img/original/discussionBoard.png");
			model.addAttribute("headerImgPath", "/img/original/title_discussionBoard.png");	
			
			// + SEARCH TYPE, PAGE SIZE TYPE
			Map<String, String> searchTypeKeyAndVal 	= new LinkedHashMap<>();
			Map<Integer,String> pageSizeTypeKeyAndVal 	= new LinkedHashMap<>();

			searchTypeKeyAndVal.put(SEARCH_TYPE_TITLE_AND_CONTENT, "Title & Content");
			searchTypeKeyAndVal.put(SEARCH_TYPE_TITLE, "Title Only");
			searchTypeKeyAndVal.put(SEARCH_TYPE_CONTENT, "Content Only");
			searchTypeKeyAndVal.put(SEARCH_TYPE_USER, "Username");
			searchTypeKeyAndVal.put(SEARCH_TYPE_COMMENT, "Comment");

			pageSizeTypeKeyAndVal.put(PAGE_SIZE_TYPE1,	PAGE_SIZE_TYPE1 + " posts");
			pageSizeTypeKeyAndVal.put(PAGE_SIZE_TYPE2,	PAGE_SIZE_TYPE2 + " posts");
			pageSizeTypeKeyAndVal.put(PAGE_SIZE_TYPE3,	PAGE_SIZE_TYPE3 + " posts");
			pageSizeTypeKeyAndVal.put(PAGE_SIZE_TYPE4,	PAGE_SIZE_TYPE4 + " posts");
			pageSizeTypeKeyAndVal.put(PAGE_SIZE_TYPE5,	PAGE_SIZE_TYPE5 + " posts");
			
			model.addAttribute("searchTypes", searchTypeKeyAndVal);
			model.addAttribute("pageSizeTypes", pageSizeTypeKeyAndVal);
			

			
			// GET DATA FOR FILTERED POSTS
			BoardSearchResult boardSearchResult = 
					daoService.searchPosts(pageNumber, pageSize, searchType, searchQuery);
			
			model.addAttribute("currentPageNum", boardSearchResult.getCurrentPageNum());
			model.addAttribute("currentPageSize", boardSearchResult.getCurrentPageSize());
			
			model.addAttribute("totalNumberOfPages", boardSearchResult.getTotalNumberOfPages());

			model.addAttribute("searchQuery", boardSearchResult.getSearchQuery());
			model.addAttribute("searchType", boardSearchResult.getSearchType());
			
			model.addAttribute("announcements", boardSearchResult.getAnnouncements());
			model.addAttribute("posts",	boardSearchResult.getPosts());	
			model.addAttribute("comments",boardSearchResult.getComments());

			model.addAttribute("pagesAround", boardSearchResult.getPagesAround());
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return "discussion_board";
	}
	
	@GetMapping("/discussionBoard/more")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> showDiscussionBoardMore
	(
	    @RequestParam(value = "pageNumber", 	defaultValue = "1") 	int pageNumber,
	    @RequestParam(value = "pageSize", 		defaultValue = "10") 	int pageSize,
	    @RequestParam(value = "searchQuery", 	required = false) 	String searchQuery,	
	    @RequestParam(value = "searchType", 	required = false) 	String searchType
	) 
	{
		Map<String,Object> response = new HashMap<>();
		try
		{
			BoardSearchResult boardSearchResult = 
					daoService.searchPosts(pageNumber, pageSize, searchType, searchQuery);
			
			response.put("boardSearchResult", boardSearchResult);
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.put("message", "Failed to show more posts. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	
	// for better code readability, postWrite, postRead page will be handled in a different controller
}
