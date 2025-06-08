package forge.model.discussionboard.custom;

import java.util.ArrayList;
import java.util.List;

import forge.model.discussionboard.PostCommentDTO;
import forge.model.discussionboard.PostDTO;
import lombok.Data;

@Data
public class BoardSearchResult {				// DATA TYPE FOR LOADING POSTS

	
	private List<PostDTO> announcements;		// ANNOUNCEMENT POSTS TO SHOW ALWAYS ON TOP			
	private List<PostDTO> posts;				// SELECTED POSTS
	private List<PostCommentDTO> comments;		// SELECTED COMMENTS

	
	private int currentPageNum;
	private int currentPageSize;	
	
	private int totalNumberOfPages;				// SAME AS THE LAST PAGE NUMBER
	private int totalNumberOfItems;				// 이름 totalNumberOfItems 로 바꾸자
	
	private String searchType;
	private String searchQuery;
	
	private ArrayList<Integer> pagesAround;

	public BoardSearchResult() {
		
		this.currentPageNum 	= 1;
		this.currentPageSize 	= 20;
		this.totalNumberOfPages	= 0;
		this.totalNumberOfItems = 0;
		this.posts 				= new ArrayList<>();
		this.comments 			= new ArrayList<>();
		
	}
	
}
