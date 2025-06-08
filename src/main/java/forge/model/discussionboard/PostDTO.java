package forge.model.discussionboard;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

@Data
public class PostDTO {
	
	
	// POST TABLE COLUMN
	private int postSeq;
	private int typeSeq;
	private String postTitle;
	private OffsetDateTime postCreationTime;
	private OffsetDateTime postUpdatedTime;
	private int postViews;
	private int userSeq;
	
	
	// ADDITIONAL DATA FROM TALBE JOIN
	private int postNumber;
	private String username;
	private int howmanyImages;
	private int likes;
	private int comments;
	
	
	// ADDITAIONAL DATA TO BE SET PROGRAMATICALLY
	private boolean isLiked;			// whether the post is liked by the current user
	
	
	
	
	public String getUsername() {
		if(this.username == null || this.username.length()==0) {
			return "unknown";
		} else {
			return this.username;
		}
	}
	
}
