package forge.controller;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import forge.consts.Consts_DiscussionBoard;
import forge.model.discussionboard.PostCommentDTO;
import forge.model.discussionboard.PostContentDTO;
import forge.model.discussionboard.custom.CommentCRUDResult;
import forge.model.discussionboard.custom.CommentLikeResult;
import forge.model.discussionboard.custom.PostDeleteResult;
import forge.model.discussionboard.custom.PostLikeResult;
import forge.model.discussionboard.custom.PostReadResult;
import forge.services.DAOService;
import forge.services.EmailService;
import forge.services.FileStorageService;
import forge.services.MimeTypeCheckService;
import forge.services.RecaptchaService;
import forge.services.TextService;
import jakarta.servlet.http.HttpSession;

@Controller
public class Controller_DiscussionBoard_Post_Read {

	private static final Logger logger = LoggerFactory.getLogger(Controller_DiscussionBoard_Post_Read.class);
	
	private final DAOService daoService;
	private final EmailService emailService;
	private final RecaptchaService recaptchaService;
	private final PasswordEncoder passwordEncoder;
	private final FileStorageService fileStorageService;
	private final MimeTypeCheckService mimeTypeCheckService;
	private final TextService textService;
	
	public Controller_DiscussionBoard_Post_Read
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
	
	
	
	@GetMapping("/discussionBoard/read")
	public String showPostRead
	(
		@RequestParam(name="postSeq") int postSeq,
		@RequestParam(name="contentSearchQuery", required=false) String contentSearchQuery,
		@RequestParam(name="searchedCommentSeq", required=false) Integer searchedCommentSeq,
		HttpSession session,
		Model model
	) 
	{
		try
		{
			
			Integer userSeq = session.getAttribute("userSeq") == null ? null : (Integer) session.getAttribute("userSeq");
			
			daoService.increasePostViews(postSeq);
			PostReadResult postReadResult = daoService.getPostReadResult(postSeq, userSeq);
			
			if(postReadResult == null) {
				model.addAttribute("message", "The post does not exist.");
				return "error";
			}
			
			for(PostContentDTO postContent : postReadResult.getPostContents())
			{
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
			}
			
			model.addAttribute("postSeq", postReadResult.getPost().getPostSeq());	
			model.addAttribute("postType", postReadResult.getPost().getTypeSeq());	
			model.addAttribute("postTitle", postReadResult.getPost().getPostTitle());
			model.addAttribute("postWriter", postReadResult.getPost().getUsername());
			model.addAttribute("postWriterSeq", postReadResult.getPost().getUserSeq());
			model.addAttribute("postCreationTime", postReadResult.getPost().getPostCreationTime());
			model.addAttribute("postUpdatedTime", postReadResult.getPost().getPostUpdatedTime());

			NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
			
			model.addAttribute("howManyPostViews", numberFormat.format(postReadResult.getPost().getPostViews()));
			model.addAttribute("howManyPostLikes", numberFormat.format(postReadResult.getPostLikes().size()));
			model.addAttribute("howManyPostComments", numberFormat.format(postReadResult.getPostComments().size()));
		
			model.addAttribute("postContents", postReadResult.getPostContents());
			model.addAttribute("postLikedOrNot", postReadResult.isLiked());
			model.addAttribute("postComments", sortPostComments( postReadResult.getPostComments()));
			
			model.addAttribute("maxCommentLength", Consts_DiscussionBoard.MAX_COMMENT_LENGTH);
			
			return "discussion_board_post_read";
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "error";
		}
		
	}
	
	
	
	
	@DeleteMapping("/discussionBoard/post")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> discussionBoard_deletePost
	( @RequestParam(name="postSeq") int postSeq, HttpSession session ) 
	{
		Map<String,Object> response = new HashMap<>();
		
		try
		{
			Integer userSeq = session.getAttribute("userSeq") == null ? null : (Integer) session.getAttribute("userSeq");
			
			if(userSeq == null) {
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Please login to delete the post.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			PostDeleteResult postDeleteResult = daoService.getPostDeleteResult(postSeq, userSeq);
			
			if(postDeleteResult.getResult() == Consts_DiscussionBoard.POST_RESULT_FAILED_NOT_AUTHORIZED) {
				response.put("status", "error");
				response.put("error", "NOT_AUTHORIZED");
				response.put("message", "You can only delete posts you created.");
				return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
			} 
			
			if(postDeleteResult.getResult() == Consts_DiscussionBoard.POST_RESULT_FAILED_NOT_FOUND) {
				response.put("status", "error");
				response.put("error", "NOT_FOUND");
				response.put("message", "This post no longer exists. Please return to the board.");
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			
			
			boolean isSuccessful = true;
			
			if(postDeleteResult.getResult() == Consts_DiscussionBoard.POST_RESULT_SUCCESSFUL) {
				
				for(PostContentDTO postContent : postDeleteResult.getPostContents()) {
					if(postContent.getTypeSeq() != Consts_DiscussionBoard.POST_CONTENT_TYPE_IMAGE) {continue;}
					String fileFullPath = postContent.getContentValue().replaceFirst("\\/", "");
					if (!fileStorageService.deleteFile(fileFullPath) ) {
						isSuccessful = false;
					}
				}
				
				if(!isSuccessful) {
					response.put("status", "success");
					response.put("message", "The post has been deleted.");
					return new ResponseEntity<>(response, HttpStatus.OK);
				}
				
				response.put("status", "success");
				response.put("message", "The post has been successfully deleted.");
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
		
			response.put("status", "error");
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "An unknown error occurred. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.put("status", "error");
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "An unknown error occurred. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@PostMapping("/discussionBoard/read/postLike")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> likePost ( @RequestParam(name="postSeq") int postSeq,	HttpSession session ) 
	{
		Map<String,Object> response = new HashMap<>();

		try
		{
			Integer userSeq = session.getAttribute("userSeq") == null ? null : (Integer) session.getAttribute("userSeq");
			if(userSeq == null)
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Please login to like the post.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(daoService.getPost(postSeq) == null) {
				response.put("status", "error");
				response.put("error", "GONE");
				response.put("message", "This post no longer exists. Please return to the board.");
				return new ResponseEntity<>(response, HttpStatus.GONE);
			}
			
			PostLikeResult postLikeReulst = daoService.postLike(postSeq, userSeq);
			response.put("status", "success");
			response.put("postLikedOrNot", postLikeReulst.getPostLike() != null);
			response.put("howmanyPostLikes", postLikeReulst.getHowmanyLiked());
			
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.put("status", "error");
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "An unknown error occurred. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	
	@PostMapping("/discussionBoard/read/commentLike")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> likeComment 
	( 
		@RequestParam(name="postSeq") int postSeq,
		@RequestParam(name="commentSeq") int commentSeq,
		HttpSession session 	
	) 
	{
		Map<String,Object> response = new HashMap<>();

		try
		{
			Integer userSeq = session.getAttribute("userSeq") == null ? null : (Integer) session.getAttribute("userSeq");
			if(userSeq == null)
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Please login to like the comment.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			};
			
			CommentLikeResult commentLikeResult = daoService.commentLike(postSeq, commentSeq, userSeq);
			
			if(commentLikeResult.getCommentLikeResult() == Consts_DiscussionBoard.COMMENT_RESULT_FAILED_POST_NOT_FOUND) {
				response.put("status", "error");
				response.put("error", "GONE");
				response.put("message", "This post no longer exists. Please return to the board.");
				return new ResponseEntity<>(response, HttpStatus.GONE);
			}
			
			if(commentLikeResult.getCommentLikeResult() == Consts_DiscussionBoard.COMMENT_RESULT_FAILED_COMMENT_NOT_FOUND) {
				response.put("status", "error");
				response.put("error", "NOT_FOUND");
				response.put("message", "You couldn't like the comment because the comment no longer exists.");
				response.put("howmanyComments", commentLikeResult.getPostComments().size());
				response.put("postComments", sortPostComments( commentLikeResult.getPostComments()) );
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			
			if(commentLikeResult.getCommentLikeResult() == Consts_DiscussionBoard.COMMENT_RESULT_SUCCESSFUL) {
				
				response.put("status", "success");
				response.put("howmanyLiked", commentLikeResult.getHowmanyLiked());
				response.put("isLiked", commentLikeResult.isLiked());
				
				return new ResponseEntity<>(response, HttpStatus.OK);
				
			} 
				
			response.put("status", "error");
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "An unknown error occurred. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.put("status", "error");
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "An unknown error occurred. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	
	@PostMapping("/discussionBoard/read/comment")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> discussionBoard_addComment
	(
		@RequestParam(name="postSeq") int postSeq,
		@RequestParam(name="commentContent") String commentContent,
		@RequestParam(name="subjectCommentSeq", required=false) Integer subjectCommentSeq,	// ParentComment or targetComment sequence
		HttpSession session	
	) 
	{
		Map<String,Object> response = new HashMap<>();
		
		try
		{
			Integer userSeq = session.getAttribute("userSeq") == null ? null : (Integer) session.getAttribute("userSeq");
			if(userSeq == null)
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Please login to leave a comment.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			commentContent = commentContent.replace("\r\n", "\n");
			
			if(commentContent.length() == 0)
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Please enter content to leave a comment.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(commentContent.length() > Consts_DiscussionBoard.MAX_COMMENT_LENGTH)
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "The comment must be no longer than " + Consts_DiscussionBoard.MAX_COMMENT_LENGTH + " characters.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
	
			CommentCRUDResult commentCRUDResult = daoService.addComment(postSeq, subjectCommentSeq, userSeq, commentContent);
			
			if(commentCRUDResult.getResult() == Consts_DiscussionBoard.COMMENT_RESULT_FAILED_POST_NOT_FOUND)
			{
				response.put("status", "error");
				response.put("error", "GONE");
				response.put("message", "This post no longer exists. Please return to the board.");
				return new ResponseEntity<>(response, HttpStatus.GONE);
			}
			else if(commentCRUDResult.getResult() == Consts_DiscussionBoard.COMMENT_RESULT_FAILED_COMMENT_NOT_FOUND)
			{
				response.put("status", "error");
				response.put("error", "NOT_FOUND");
				response.put("message", "You couldn't reply to the comment because the comment no longer exists.");
				response.put("howmanyComments", commentCRUDResult.getPostComments().size());
				response.put("postComments", sortPostComments( commentCRUDResult.getPostComments()) );
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			else if(commentCRUDResult.getResult() == Consts_DiscussionBoard.COMMENT_RESULT_SUCCESSFUL)
			{
				response.put("status", "success");
				response.put("howmanyComments", commentCRUDResult.getPostComments().size());
				response.put("postComments", sortPostComments( commentCRUDResult.getPostComments()));

				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			else
			{
				response.put("status", "error");
				response.put("error", "INTERNAL_SERVER_ERROR");
				response.put("message", "An unknown error occurred. Please try again.");
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.put("status", "error");
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "An unknown error occurred. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@PatchMapping("/discussionBoard/read/comment")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> discussionBoard_updateComment
	(
		@RequestParam(name="postSeq") int postSeq,
		@RequestParam(name="subjectCommentSeq") int commentSeq,
		@RequestParam(name="commentContent") String commentContent,
		HttpSession session	
	) 
	{
		Map<String,Object> response = new HashMap<>();
		
		try
		{
			Integer userSeq = session.getAttribute("userSeq") == null ? null : (Integer) session.getAttribute("userSeq");
			if(userSeq == null)
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Please login to modify the comment.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			commentContent = commentContent.replace("\r\n", "\n");
			
			if(commentContent.length() == 0)
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Please enter content to modify the comment.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			if(commentContent.length() > Consts_DiscussionBoard.MAX_COMMENT_LENGTH)
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "The comment must be no longer than " + Consts_DiscussionBoard.MAX_COMMENT_LENGTH + " characters.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
	
			CommentCRUDResult commentCRUDResult = daoService.updateComment(postSeq, commentSeq, userSeq, commentContent);
			
			if(commentCRUDResult.getResult() == Consts_DiscussionBoard.COMMENT_RESULT_FAILED_POST_NOT_FOUND)
			{
				response.put("status", "error");
				response.put("error", "GONE");
				response.put("message", "This post no longer exists. Please return to the board.");
				return new ResponseEntity<>(response, HttpStatus.GONE);
			}
			else if(commentCRUDResult.getResult() == Consts_DiscussionBoard.COMMENT_RESULT_FAILED_COMMENT_NOT_FOUND)
			{
				response.put("status", "error");
				response.put("error", "NOT_FOUND");
				response.put("message", "You couldn't modify the comment because the comment no longer exists.");
				response.put("howmanyComments", commentCRUDResult.getPostComments().size());
				response.put("postComments", sortPostComments( commentCRUDResult.getPostComments()) );
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			else if(commentCRUDResult.getResult() == Consts_DiscussionBoard.COMMENT_RESULT_FAILED_NOT_AUTHORIZED) 
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "You can only modify comments that you wrote.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			else if(commentCRUDResult.getResult() == Consts_DiscussionBoard.COMMENT_RESULT_SUCCESSFUL)
			{
				response.put("status", "success");
				response.put("howmanyComments", commentCRUDResult.getPostComments().size());
				response.put("postComments", sortPostComments( commentCRUDResult.getPostComments()));

				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			else
			{
				response.put("status", "error");
				response.put("error", "INTERNAL_SERVER_ERROR");
				response.put("message", "An unknown error occurred. Please try again.");
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.put("status", "error");
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "An unknown error occurred. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@DeleteMapping("/discussionBoard/read/comment")
	@ResponseBody
	public ResponseEntity<Map<String,Object>> discussionBoard_deleteComment
	(
		@RequestParam(name="postSeq") int postSeq,
		@RequestParam(name="commentSeq") int commentSeq,
		HttpSession session
	) 
	{
		Map<String,Object> response = new HashMap<>();
		
		try
		{
			Integer userSeq = session.getAttribute("userSeq") == null ? null : (Integer) session.getAttribute("userSeq");
			if(userSeq == null)
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "Please login to delete the comment.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			
			CommentCRUDResult commentCRUDResult = daoService.deleteComment(postSeq, commentSeq, userSeq);
			
			if(commentCRUDResult.getResult() == Consts_DiscussionBoard.COMMENT_RESULT_FAILED_POST_NOT_FOUND) 
			{
				response.put("status", "error");
				response.put("error", "GONE");
				response.put("message", "This post no longer exists. Please return to the board.");
				return new ResponseEntity<>(response, HttpStatus.GONE);
			}
			else if(commentCRUDResult.getResult() == Consts_DiscussionBoard.COMMENT_RESULT_FAILED_COMMENT_NOT_FOUND) 
			{
				response.put("status", "error");
				response.put("error", "NOT_FOUND");
				response.put("message", "You couldn't delete the comment because the comment no longer exists.");
				response.put("howmanyComments", commentCRUDResult.getPostComments().size());
				response.put("postComments", sortPostComments( commentCRUDResult.getPostComments()) );
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			}
			else if(commentCRUDResult.getResult() == Consts_DiscussionBoard.COMMENT_RESULT_FAILED_NOT_AUTHORIZED) 
			{
				response.put("status", "error");
				response.put("error", "BAD_REQUEST");
				response.put("message", "You can only delete comments that you wrote.");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			else if(commentCRUDResult.getResult() == Consts_DiscussionBoard.COMMENT_RESULT_SUCCESSFUL) 
			{
				response.put("status", "success");
				response.put("howmanyComments", commentCRUDResult.getPostComments().size());
				response.put("postComments", sortPostComments( commentCRUDResult.getPostComments()));

				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			else
			{
				response.put("status", "error");
				response.put("error", "INTERNAL_SERVER_ERROR");
				response.put("message", "An unknown error occurred. Please try again.");
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response.put("status", "error");
			response.put("error", "INTERNAL_SERVER_ERROR");
			response.put("message", "An unknown error occurred. Please try again.");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	
	
	private List<PostCommentDTO> sortPostComments(List<PostCommentDTO> postComments) {
		
		Map<Integer,String> commentSeqAndWriterName = new HashMap<>();
		Map<Integer, ArrayList<PostCommentDTO>> parentAndChildren = new HashMap<>();
		ArrayList<PostCommentDTO> sortedComments = new ArrayList<>();
		
		for(PostCommentDTO commentInfo : postComments) {
			commentSeqAndWriterName.put(commentInfo.getCommentSeq(), commentInfo.getUsername());
			
			if(commentInfo.getParentCommentSeq() == null) 
			{
				sortedComments.add(commentInfo);
				parentAndChildren.put(commentInfo.getCommentSeq(), new ArrayList<PostCommentDTO>());
			} 
			else 
			{
				if(commentInfo.getTargetCommentSeq() != null) {
					commentInfo.setTargetCommentWriter(commentSeqAndWriterName.get(commentInfo.getTargetCommentSeq()));
				}
				parentAndChildren.get(commentInfo.getParentCommentSeq()).add(commentInfo);
			}
		};
		
		
		for(int i=sortedComments.size()-1;i>=0;i--) {
			PostCommentDTO parentComment = sortedComments.get(i);
			ArrayList<PostCommentDTO> children = parentAndChildren.get(parentComment.getCommentSeq());
			sortedComments.addAll(i+1, children);
		}
		
		return sortedComments;
	}
	
	
	
}
