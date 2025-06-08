package forge.model.discussionboard;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

@Data
public class PostCommentDTO {

	// COMMENT TABLE COLUMN
	private int commentSeq;
	private String commentContent;
	private OffsetDateTime commentCreationTime;
	private OffsetDateTime commentUpdatedTime;
	private int postSeq;
	private int userSeq;
	private Integer parentCommentSeq;
	private Integer targetCommentSeq;
	
	
	// ADDITAIONAL DATA FROM TABLE JOIN
	private int commentNumber;
	private int howmanyImages;				// how many images the commented post includes
	
	private String username;
	private String userImg;
	
	private String postTitle;
	private int postType;
	
	private int likes;
	
	
	
	// ADDITAIONAL DATA FOR PROGRAMMING
	private String targetCommentWriter;
	private boolean isLiked;				// whether the comment is liked by the current user
	private boolean isUserComment;
	
	public String getUsername() {
		if(this.username == null || this.username.length()==0) {
			return "unknown";
		} else {
			return this.username;
		}
	}

	
}
