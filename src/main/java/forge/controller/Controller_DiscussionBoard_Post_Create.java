package forge.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import forge.consts.Consts_DiscussionBoard;
import forge.consts.Consts_General;
import forge.model.UserDTO;
import forge.model.discussionboard.PostContentDTO;
import forge.model.discussionboard.PostDTO;
import forge.model.discussionboard.custom.PostUpdateResult;
import forge.services.DAOService;
import forge.services.EmailService;
import forge.services.FileStorageService;
import forge.services.MimeTypeCheckService;
import forge.services.RecaptchaService;
import forge.services.TextService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class Controller_DiscussionBoard_Post_Create {

	private static final Logger logger = LoggerFactory.getLogger(Controller_DiscussionBoard_Post_Create.class);
	
	private final DAOService daoService;
	private final EmailService emailService;
	private final RecaptchaService recaptchaService;
	private final PasswordEncoder passwordEncoder;
	private final FileStorageService fileStorageService;
	private final MimeTypeCheckService mimeTypeCheckService;
	private final TextService textService;
	
	public Controller_DiscussionBoard_Post_Create
	(	
		DAOService daoService,
		EmailService emailService,
		RecaptchaService recaptchaService,
		PasswordEncoder	passwordEncoder,
		FileStorageService fileStorageService,
		MimeTypeCheckService mimeTypeCheckService,
		TextService textService
	) 
	{
		this.daoService = daoService;
		this.emailService = emailService;
		this.recaptchaService = recaptchaService;
		this.passwordEncoder = passwordEncoder;
		this.fileStorageService = fileStorageService;
		this.mimeTypeCheckService = mimeTypeCheckService;
		this.textService = textService;
	}
	
	private final String COMMA_REPLACEMENT = "%CoMmA%";
	// needed because ,(comma) is a delimeter for @RequestParam List<String> by default.
	
	
	@GetMapping("/discussionBoard/create")
	public String showPostWrite(Model model, HttpSession session) {	
		try
		{
			if(session.getAttribute("userSeq") == null) {
				return "redirect:/login";
			}
			
			int userSeq 	= Integer.parseInt(session.getAttribute("userSeq").toString());
			UserDTO user 	= daoService.getUserBySeq(userSeq);
			
			model.addAttribute(
				"isManagerMode",
				(user.getRoleSeq() == Consts_General.USER_TYPE_MANAGER ||
				user.getRoleSeq() == Consts_General.USER_TYPE_THE_TORTOISE)
				? true : false
			);
			
			model.addAttribute("normalPostTypeSeq",Consts_DiscussionBoard.POST_TYPE_NORMAL_POST);
			model.addAttribute("announcementPostTypeSeq",Consts_DiscussionBoard.POST_TYPE_ANNOUNCEMENT);
			
			model.addAttribute("textContentTypeSeq", Consts_DiscussionBoard.POST_CONTENT_TYPE_TEXT);
			model.addAttribute("boldTextContentTypeSeq", Consts_DiscussionBoard.POST_CONTENT_TYPE_BOLD_TEXT);
			model.addAttribute("imageContentTypeSeq", Consts_DiscussionBoard.POST_CONTENT_TYPE_IMAGE);
			
			model.addAttribute("maxTextContentNumber", Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_TEXT_CONTENTS);
			model.addAttribute("maxBoldTextContentNumber", Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_BOLD_TEXT_CONTENTS);
			model.addAttribute("maxImageContentNumber", Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_IMAGE_CONTENTS);
			
			model.addAttribute("maxTitleLength", Consts_DiscussionBoard.MAX_TITLE_LENGTH);
			model.addAttribute("maxTextContentLength", Consts_DiscussionBoard.MAX_TEXT_CONTENT_LENGTH);
			model.addAttribute("maxImgFileSize", Consts_DiscussionBoard.MAX_IMG_FILE_SIZE);
			
			model.addAttribute("commaReplacement", COMMA_REPLACEMENT);
			
			return "discussion_board_post_create";
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "error";
		}
	}
	
	
	@GetMapping("/discussionBoard/update")
	public String showPostUpdate
	(
		@RequestParam(name="postSeq") int postSeq,
		Model model, 
		HttpSession session
	) 
	{
		try
		{
			if(session.getAttribute("userSeq") == null) {
				return "redirect:/login";
			}
			
			int userSeq 	= Integer.parseInt(session.getAttribute("userSeq").toString());
			UserDTO user 	= daoService.getUserBySeq(userSeq);
			PostDTO post 	= daoService.getPost(postSeq);
			
			if(post == null) {
				model.addAttribute("message", "The post does not exist.");
				return "error";
			}
			
			if(post.getUserSeq() != userSeq) {
				model.addAttribute("message","You can only modify posts you created.");
				return "error";
			}
			
			List<PostContentDTO> postContents = daoService.getPostContents(postSeq);
			
			int howmanyImagesToConvert = 0;
			for(PostContentDTO postContent : postContents) {
				
				if(postContent.getTypeSeq() == Consts_DiscussionBoard.POST_CONTENT_TYPE_IMAGE) {
					howmanyImagesToConvert++;
				}
				
				if
				(
					postContent.getTypeSeq() == Consts_DiscussionBoard.POST_CONTENT_TYPE_TEXT || 
					postContent.getTypeSeq() == Consts_DiscussionBoard.POST_CONTENT_TYPE_BOLD_TEXT
				) 
				{
					postContent.setContentValue(
						textService.toThymeleafForm( postContent.getContentValue() )
					);
				}
				
			};
			
			
			
			
			model.addAttribute(
					"isManagerMode",
					(user.getRoleSeq() == Consts_General.USER_TYPE_MANAGER ||
					user.getRoleSeq() == Consts_General.USER_TYPE_THE_TORTOISE)
					? true : false
			);
	
			model.addAttribute("normalPostTypeSeq",Consts_DiscussionBoard.POST_TYPE_NORMAL_POST);
			model.addAttribute("announcementPostTypeSeq",Consts_DiscussionBoard.POST_TYPE_ANNOUNCEMENT);
			
			model.addAttribute("textContentTypeSeq", Consts_DiscussionBoard.POST_CONTENT_TYPE_TEXT);
			model.addAttribute("boldTextContentTypeSeq", Consts_DiscussionBoard.POST_CONTENT_TYPE_BOLD_TEXT);
			model.addAttribute("imageContentTypeSeq", Consts_DiscussionBoard.POST_CONTENT_TYPE_IMAGE);
			
			model.addAttribute("maxTextContentNumber", Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_TEXT_CONTENTS);
			model.addAttribute("maxBoldTextContentNumber", Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_BOLD_TEXT_CONTENTS);
			model.addAttribute("maxImageContentNumber", Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_IMAGE_CONTENTS);
			
			model.addAttribute("maxTitleLength", Consts_DiscussionBoard.MAX_TITLE_LENGTH);
			model.addAttribute("maxTextContentLength", Consts_DiscussionBoard.MAX_TEXT_CONTENT_LENGTH);
			model.addAttribute("maxImgFileSize", Consts_DiscussionBoard.MAX_IMG_FILE_SIZE);
			
			
			// MODIFY MODE
			model.addAttribute("postSeq", postSeq);
			model.addAttribute("postType", post.getTypeSeq());
			model.addAttribute("postTitle", post.getPostTitle());
			model.addAttribute("postContents",postContents);	
			
			model.addAttribute("textContentType", Consts_DiscussionBoard.POST_CONTENT_TYPE_TEXT);
			model.addAttribute("boldTextContentType", Consts_DiscussionBoard.POST_CONTENT_TYPE_BOLD_TEXT);
			model.addAttribute("ImageContentType", Consts_DiscussionBoard.POST_CONTENT_TYPE_IMAGE);
			
			model.addAttribute("howmanyImagesToConvert", howmanyImagesToConvert);
			
			
			// MODIFY MODE - FOR PREVIEW DIALOG
			model.addAttribute("post_createdTime_preview", post.getPostCreationTime());
			model.addAttribute("post_views_preview", post.getPostViews());
			model.addAttribute("post_likes_preview", daoService.getPostLikes(postSeq).size());
			model.addAttribute("post_comments_preview", daoService.getPostComments(postSeq).size());
			
			model.addAttribute("commaReplacement", COMMA_REPLACEMENT);
			
			return "discussion_board_post_create";
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "error";
		}
	}


	
	@PostMapping("/discussionBoard/post")
	@ResponseBody 
	public ResponseEntity<Map<String,Object>> DiscussionBoard_createPost
	(	
		HttpSession session,
		HttpServletRequest req,
		@RequestParam(name="postTitle",			required = false) 	String postTitle,
		@RequestParam(value="postType", 		defaultValue = "0") int postType,
		@RequestParam(name="postContentTypes",	required = false)	List<Integer> postContentTypes,
		
		@RequestParam(name="textContents", 		required = false) 	List<String> textContents,
		@RequestParam(name="boldTextContents", 	required = false) 	List<String> boldTextContents,
		@RequestParam(name="imageContents", 	required = false) 	List<MultipartFile> imageContents
	) 
	{
		
		Map<String,Object> response = new HashMap<String,Object>();
		
		if(postContentTypes == null) {	postContentTypes 	= new ArrayList<>();	}
		if(textContents 	== null) {	textContents 		= new ArrayList<>();	}
		if(boldTextContents	== null) {	boldTextContents 	= new ArrayList<>();	}
		if(imageContents	== null) {	imageContents 		= new ArrayList<>();	}
		
		
		try
		{
			
			// ONLY USER CAN CREATE A POST
			if(session.getAttribute("userSeq") == null) {
				
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "You must be logged in to create a post.");
				
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			int userSeq		= Integer.parseInt(session.getAttribute("userSeq").toString());
			UserDTO user 	= daoService.getUserBySeq(userSeq);
			
			
			// ONLY MANAGERS OR ADMINS CAN CREATE A SPECIAL POST
			if (postType == Consts_DiscussionBoard.POST_TYPE_ANNOUNCEMENT) {
				
			    int userType = user.getRoleSeq();
			    
			    if (
		    		userType != Consts_General.USER_TYPE_MANAGER && 
		    		userType != Consts_General.USER_TYPE_THE_TORTOISE
			    ) {
			        response.put("status", "error");
			        response.put("error", "BAD_REQUEST");
			        response.put("message", "Only managers or administrators are authorized to create the type of posts.");
			        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			    }
			}
			
			
			// POST TITLE MUST NOT BE NULL
			if(postTitle == null || postTitle.length() == 0 || postTitle.trim().length() == 0) {
				
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "The post title cannot be empty. Please provide a title.");
				
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			postTitle = postTitle.trim();
			
			
			// POST TITLE LENGTH CHECK
			if(postTitle.length() > Consts_DiscussionBoard.MAX_TITLE_LENGTH) {
				
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "The post title must be no longer than " + Consts_DiscussionBoard.MAX_TITLE_LENGTH + " characters.");
				
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			
			// A POST MUST HAVE AT LEAST ONE CONTENT ITEM
			if(postContentTypes.size() == 0) {
				
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "The post must contain at least one piece of content.");
				
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			
			
			// MUST BE CONTENTTYPES'LENGTH == CONTENTS LENGTH
			if
			(
				postContentTypes.size() 
				!= 
				(
					textContents.size() +	
					boldTextContents.size() +
					imageContents.size()
				)
			)
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "The number of content types does not match the number of content items. Please ensure they match.\n\n(If they match, please refresh the page and try again)");
				System.out.println(
						"postContentSize : " + postContentTypes.size() + "," +
						"textContentsSize : " + textContents.size() + "," +
						"boldTextContentsSize : " + boldTextContents.size() + "," +
						"imageContentsSize : " + imageContents.size() 
				);
				
				
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
				
			

			
			// MAXIMUM NUMBER OF EACH CONTENT CHECK
			if(textContents.size() > Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_TEXT_CONTENTS) {
			    response.put("status", "error");
			    response.put("error", "BAD_REQUEST");
			    response.put("message", "The maximum number of [text content] items is " + Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_TEXT_CONTENTS + ".");
			    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			if(boldTextContents.size() > Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_BOLD_TEXT_CONTENTS) {
			    response.put("status", "error");
			    response.put("error", "BAD_REQUEST");
			    response.put("message", "The maximum number of [bold text content] items is " + Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_BOLD_TEXT_CONTENTS + ".");
			    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			if(imageContents.size() > Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_IMAGE_CONTENTS) {
			    response.put("status", "error");
			    response.put("error", "BAD_REQUEST");
			    response.put("message", "The maximum number of [image content] items is " + Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_IMAGE_CONTENTS + ".");
			    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			
			
			// CREATE LIST OF POST CONTENT MODELS WITH THE DATA TAKEN
	        LocalDate now 	= LocalDate.now();
	        String year 	= String.valueOf(now.getYear());
	        String month 	= String.format("%02d", now.getMonthValue());
	        String day 		= String.format("%02d", now.getDayOfMonth());
			
			ArrayList<PostContentDTO> postContents = new ArrayList<>();
			
			for(int i=0;i<postContentTypes.size();i++) {
				
				int postContentType = postContentTypes.get(i).intValue();
				PostContentDTO content = new PostContentDTO();
				
				content.setTypeSeq(postContentType);
				
				if(postContentType == Consts_DiscussionBoard.POST_CONTENT_TYPE_TEXT) 
				{
					content.setContentValue(
							textContents.remove(0)
							.replaceFirst(":","")
							.replaceAll(COMMA_REPLACEMENT, ",")
							.replace("\r\n", "\n")
					);
					if(content.getContentValue().length() == 0) {
						response.put("status", "error");
						response.put("error", "BAD_REQUEST");
						response.put("message", "Please enter content inside the text content item.");
						return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
					}
					if(content.getContentValue().length() > Consts_DiscussionBoard.MAX_TEXT_CONTENT_LENGTH) {
						response.put("status", "error");
						response.put("error", "BAD_REQUEST");
						response.put("message", "The text content must not exceed " + Consts_DiscussionBoard.MAX_TEXT_CONTENT_LENGTH + " characters.");
						return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
					}
				}
				
				else if(postContentType == Consts_DiscussionBoard.POST_CONTENT_TYPE_BOLD_TEXT) 
				{
					content.setContentValue(
							boldTextContents.remove(0)
							.replaceFirst(":","")
							.replaceAll(COMMA_REPLACEMENT, ",")
							.replace("\r\n", "\n")
					);
					if(content.getContentValue().length() == 0) {
						response.put("status", "error");
						response.put("error", "BAD_REQUEST");
						response.put("message", "Please enter content inside the bold text content item.");
						return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
					}
					if(content.getContentValue().length() > Consts_DiscussionBoard.MAX_TEXT_CONTENT_LENGTH) {
						response.put("status", "error");
						response.put("error", "BAD_REQUEST");
						response.put("message", "The text content must not exceed " + Consts_DiscussionBoard.MAX_TEXT_CONTENT_LENGTH + " characters.");
						return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
					}
				}
				
				else if(postContentType == Consts_DiscussionBoard.POST_CONTENT_TYPE_IMAGE) 
				{
					// FILE VALIDATION CHECK
					String uniqueName 		= UUID.randomUUID().toString().replaceAll("-", "_");
					MultipartFile imageFile = imageContents.remove(0);
					String fileExtension 	= Consts_DiscussionBoard.ALLOWED_IMG_MIME_TYPES.get(mimeTypeCheckService.getMimeType(imageFile));
					
					if(imageFile.getSize() > Consts_DiscussionBoard.MAX_IMG_FILE_SIZE) {
					    response.put("status", "error");
					    response.put("message", "The size of one or more image files exceeds the "+ Consts_DiscussionBoard.MAX_IMG_FILE_SIZE/(1024*1024) +"MB limit.");
					    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
					} 
					
					if(imageFile.getSize() == 0) {
					    response.put("status", "error");
					    response.put("message", "Please upload an image file to the image upload section.");
					    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
					}
					
					if(fileExtension == null) {
						response.put("status", "error");
						response.put("error", "BAD_REQUEST");
						response.put("message", "Only JPG, PNG, and GIF formats are allowed for post images.");
						return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
					};
					
					content.setContentValue(
							Consts_DiscussionBoard.DISUCSSION_BOARD_IMAGE_MAPPING_PATH 
							+ "/" 
							+ year 
							+ "/" 
							+ month 
							+ "/" 
							+ day 
							+ "/"
							+ uniqueName + fileExtension
					);
					
					content.setFileMimeType(fileExtension);
					
					content.setFile(imageFile);
				}
				else {
					response.put("status", "error");
					response.put("error", "BAD_REQUEST");
					response.put("message", "The request includes an invalid content type.");
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}
				
				postContents.add(content);
			};
			
			
			// SAVE DATA TO DB
			int createdPostSeq = daoService.createPost(postType, postTitle, postContents, userSeq);

			
			
			// SAVE DATA(IMAGE) TO FILE SYSTEM
			
			boolean filesSaveSuccessfully = true;
			for(PostContentDTO content : postContents) {
				if(content.getTypeSeq() == Consts_DiscussionBoard.POST_CONTENT_TYPE_IMAGE) {
					
					String fileFullPath = content.getContentValue().replaceFirst("\\/", "");
					MultipartFile file	= content.getFile();
					
					if(content.getFileMimeType().equals(Consts_DiscussionBoard.ALLOWED_IMG_MIME_TYPE_JPG)) {
						filesSaveSuccessfully = fileStorageService.saveImageFile(fileFullPath, file) ? filesSaveSuccessfully : false;
					} else {
						filesSaveSuccessfully = fileStorageService.saveImageFile(fileFullPath, file) ? filesSaveSuccessfully : false;
					}
					
				}
			}
			

			if(!filesSaveSuccessfully) {
				logger.info("Post created, but some images failed to upload.");
				response.put("status", "multi_status");
				response.put("createdPostSeq", createdPostSeq);
				response.put("message", "Post created, but some images failed to upload.");
				return new ResponseEntity<>(response,HttpStatus.MULTI_STATUS); 
			}
			
			
			logger.info("Post created successfully.");
			response.put("status", "success");
			response.put("createdPostSeq", createdPostSeq);
			response.put("message", "Post created successfully.");
			return new ResponseEntity<>(response,HttpStatus.OK);
		}
		catch(Exception e) 
		{
			e.printStackTrace();
			logger.info("Post creation failed.");
			response.put("status", "error");
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "An unknown error occurred. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
	}
	

	
	@PutMapping("/discussionBoard/post")
	@ResponseBody 
	public ResponseEntity<Map<String,Object>> DiscussionBoard_updatePost
	(	
		HttpSession session,
		@RequestParam(name="postSeq") 								int postSeq,
		@RequestParam(name="postTitle",			required = false) 	String postTitle,
		@RequestParam(value="postType", 		defaultValue = "0") int postType,
		@RequestParam(name="postContentTypes",	required = false)	List<Integer> postContentTypes,
		
		@RequestParam(name="textContents", 		required = false) 	List<String> textContents,
		@RequestParam(name="boldTextContents", 	required = false) 	List<String> boldTextContents,
		@RequestParam(name="imageContents", 	required = false) 	List<MultipartFile> imageContents
	) 
	{
		
		Map<String,Object> response = new HashMap<String,Object>();
		
		if(postContentTypes == null) {	postContentTypes 	= new ArrayList<>();	}
		if(textContents 	== null) {	textContents 		= new ArrayList<>();	}
		if(boldTextContents	== null) {	boldTextContents 	= new ArrayList<>();	}
		if(imageContents	== null) {	imageContents 		= new ArrayList<>();	}
		
		
		try
		{
			
			// ONLY USER CAN CREATE A POST
			if(session.getAttribute("userSeq") == null) {
				
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "You must be logged in to update a post.");
				
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			int userSeq		= Integer.parseInt(session.getAttribute("userSeq").toString());
			UserDTO user 	= daoService.getUserBySeq(userSeq);
			
			// ONLY MANAGERS OR ADMINS CAN CREATE A SPECIAL POST
			if (postType == Consts_DiscussionBoard.POST_TYPE_ANNOUNCEMENT) {
				
			    int userType = user.getRoleSeq();
			    
			    if (
		    		userType != Consts_General.USER_TYPE_MANAGER && 
		    		userType != Consts_General.USER_TYPE_THE_TORTOISE
			    ) {
			        response.put("status", "error");
			        response.put("error", "BAD_REQUEST");
			        response.put("message", "Only managers or administrators are authorized to create the type of posts.");
			        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			    }
			}
			
			
			// POST TITLE MUST NOT BE NULL
			if(postTitle == null || postTitle.length() == 0 || postTitle.trim().length() == 0) {
				
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "The post title cannot be empty. Please provide a title.");
				
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			postTitle = postTitle.trim();
			
			// POST TITLE LENGTH CHECK
			if(postTitle.length() > Consts_DiscussionBoard.MAX_TITLE_LENGTH) {
				
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "The post title must be no longer than " + Consts_DiscussionBoard.MAX_TITLE_LENGTH + " characters.");
				
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			
			// A POST MUST HAVE AT LEAST ONE CONTENT ITEM
			if(postContentTypes.size() == 0) {
				
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "The post must contain at least one piece of content.");
				
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			
			
			// MUST BE CONTENTTYPES'LENGTH == CONTENTS LENGTH
			if
			(
				postContentTypes.size() 
				!= 
				(
					textContents.size() +	
					boldTextContents.size() +
					imageContents.size()
				)
			)
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "The number of content types does not match the number of content items. Please ensure they match.\n\n(If they match, please refresh the page and try again)");
				
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
				
			

			
			// MAXIMUM NUMBER OF EACH CONTENT CHECK
			if(textContents.size() > Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_TEXT_CONTENTS) {
			    response.put("status", "error");
			    response.put("error", "BAD_REQUEST");
			    response.put("message", "The maximum number of [text content] items is " + Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_TEXT_CONTENTS + ".");
			    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			if(boldTextContents.size() > Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_BOLD_TEXT_CONTENTS) {
			    response.put("status", "error");
			    response.put("error", "BAD_REQUEST");
			    response.put("message", "The maximum number of [bold text content] items is " + Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_BOLD_TEXT_CONTENTS + ".");
			    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			if(imageContents.size() > Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_IMAGE_CONTENTS) {
			    response.put("status", "error");
			    response.put("error", "BAD_REQUEST");
			    response.put("message", "The maximum number of [image content] items is " + Consts_DiscussionBoard.MAXIMUM_NUMBER_OF_IMAGE_CONTENTS + ".");
			    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			
			
			// CREATE LIST OF POST CONTENT MODELS WITH THE DATA TAKEN
	        LocalDate now 	= LocalDate.now();
	        String year 	= String.valueOf(now.getYear());
	        String month 	= String.format("%02d", now.getMonthValue());
	        String day 		= String.format("%02d", now.getDayOfMonth());
			
			ArrayList<PostContentDTO> postContents = new ArrayList<>();
			
			for(int i=0;i<postContentTypes.size();i++) {
				
				int postContentType = postContentTypes.get(i).intValue();
				PostContentDTO content = new PostContentDTO();
				
				content.setTypeSeq(postContentType);
				
				if(postContentType == Consts_DiscussionBoard.POST_CONTENT_TYPE_TEXT) 
				{
					content.setContentValue(
							textContents.remove(0)
							.replaceFirst(":","")
							.replaceAll(COMMA_REPLACEMENT, ",")
							.replace("\r\n", "\n")
					);
					if(content.getContentValue().length() == 0) {
						response.put("status", "error");
						response.put("error", "BAD_REQUEST");
						response.put("message", "Please enter content inside the text content item.");
						return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
					}
					if(content.getContentValue().length() > Consts_DiscussionBoard.MAX_TEXT_CONTENT_LENGTH) {
						response.put("status", "error");
						response.put("error", "BAD_REQUEST");
						response.put("message", "The text content must not exceed " + Consts_DiscussionBoard.MAX_TEXT_CONTENT_LENGTH + " characters.");
						return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
					}
				}
				
				else if(postContentType == Consts_DiscussionBoard.POST_CONTENT_TYPE_BOLD_TEXT) 
				{
					content.setContentValue(
							boldTextContents.remove(0)
							.replaceFirst(":","")
							.replaceAll(COMMA_REPLACEMENT, ",")
							.replace("\r\n", "\n")
					);
					if(content.getContentValue().length() == 0) {
						response.put("status", "error");
						response.put("error", "BAD_REQUEST");
						response.put("message", "Please enter content inside the bold text content item.");
						return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
					}
					if(content.getContentValue().length() > Consts_DiscussionBoard.MAX_TEXT_CONTENT_LENGTH) {
						response.put("status", "error");
						response.put("error", "BAD_REQUEST");
						response.put("message", "The text content must not exceed " + Consts_DiscussionBoard.MAX_TEXT_CONTENT_LENGTH + " characters.");
						return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
					}
				}
				
				else if(postContentType == Consts_DiscussionBoard.POST_CONTENT_TYPE_IMAGE) 
				{
					// FILE VALIDATION CHECK
					String uniqueName 		= UUID.randomUUID().toString().replaceAll("-", "_");
					MultipartFile imageFile = imageContents.remove(0);
					String fileExtension 	= Consts_DiscussionBoard.ALLOWED_IMG_MIME_TYPES.get(mimeTypeCheckService.getMimeType(imageFile));
					
					if(imageFile.getSize() > Consts_DiscussionBoard.MAX_IMG_FILE_SIZE) {
					    response.put("status", "error");
					    response.put("message", "The size of one or more image files exceeds the "+ Consts_DiscussionBoard.MAX_IMG_FILE_SIZE/(1024*1024) +"MB limit.");
					    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
					} 
					
					if(imageFile.getSize() == 0) {
					    response.put("status", "error");
					    response.put("message", "Please upload an image file to the image upload section.");
					    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
					}
					
					if(fileExtension == null) {
						response.put("status", "error");
						response.put("error", "BAD_REQUEST");
						response.put("message", "Only JPG, PNG, and GIF formats are allowed for post images.");
						return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
					};
					
					content.setContentValue(
							Consts_DiscussionBoard.DISUCSSION_BOARD_IMAGE_MAPPING_PATH 
							+ "/" 
							+ year 
							+ "/" 
							+ month 
							+ "/" 
							+ day 
							+ "/"
							+ uniqueName + fileExtension
					);
					
					content.setFileMimeType(fileExtension);
					
					content.setFile(imageFile);
				}
				else {
					response.put("status", "error");
					response.put("error", "BAD_REQUEST");
					response.put("message", "The request includes an invalid content type.");
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}
				
				postContents.add(content);
			};
			
			
			PostUpdateResult postUpdateResult 
				= daoService.updatePost(postSeq ,postType, postTitle, postContents, userSeq);

			if(postUpdateResult.getResult() == Consts_DiscussionBoard.POST_RESULT_FAILED_NOT_FOUND){
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "The post no longer exists. Please return to the board.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(postUpdateResult.getResult() == Consts_DiscussionBoard.POST_RESULT_FAILED_NOT_AUTHORIZED)
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "You can only modify posts you created.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(postUpdateResult.getResult() != Consts_DiscussionBoard.POST_RESULT_SUCCESSFUL)
			{
				response.put("status", "error");
				response.put("error", "INTERNAL_SERVER_ERROR");
				response.put("message", "An unknown error occurred. Please try again.");
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			
			boolean filesDeletedSavedSuccessfully = true;
			
			// DELETE FORMER IMAGES
			for(PostContentDTO postContent : postUpdateResult.getFormerPostContents()) {
				if(postContent.getTypeSeq() != Consts_DiscussionBoard.POST_CONTENT_TYPE_IMAGE) {continue;}
				String filePath = postContent.getContentValue().replaceFirst("\\/", "");
				if (!fileStorageService.deleteFile(filePath) ) {
					filesDeletedSavedSuccessfully = false;
				}
			}
			
			// SAVE DATA(IMAGES) TO FILE SYSTEM
			for(PostContentDTO content : postContents) {
				if(content.getTypeSeq() == Consts_DiscussionBoard.POST_CONTENT_TYPE_IMAGE) {
					
					String filePath = content.getContentValue().replaceFirst("\\/", "");
					MultipartFile file	= content.getFile();

					if(content.getFileMimeType().equals(Consts_DiscussionBoard.ALLOWED_IMG_MIME_TYPE_JPG)) {
						filesDeletedSavedSuccessfully = fileStorageService.saveImageFile(filePath, file) ? filesDeletedSavedSuccessfully : false;
					} else {
						filesDeletedSavedSuccessfully = fileStorageService.saveImageFile(filePath, file) ? filesDeletedSavedSuccessfully : false;
					}
					
				}
			}
			

			if(!filesDeletedSavedSuccessfully) {
				logger.info("Post updated, but some images failed to upload.");
				response.put("status", "multi_status");
				response.put("message", "Post updated, but some images failed to delete or upload.");
				return new ResponseEntity<>(response,HttpStatus.MULTI_STATUS); 
			}
			
			logger.info("Post updated successfully.");
			response.put("status", "success");
			response.put("message", "Post updated successfully.");
			return new ResponseEntity<>(response,HttpStatus.OK);
		}
		catch(Exception e) 
		{
			e.printStackTrace();
			logger.info("Post update failed.");
			response.put("status", "error");
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "An unknown error occurred. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
	}
	
	
	

	
	
}
