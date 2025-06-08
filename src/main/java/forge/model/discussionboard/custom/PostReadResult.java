package forge.model.discussionboard.custom;

import java.util.ArrayList;
import java.util.List;

import forge.model.discussionboard.PostCommentDTO;
import forge.model.discussionboard.PostContentDTO;
import forge.model.discussionboard.PostDTO;
import forge.model.discussionboard.PostLikeDTO;
import lombok.Data;

@Data
public class PostReadResult {

	private PostDTO post;
	private List<PostContentDTO> postContents;
	private List<PostCommentDTO> postComments;
	private List<PostLikeDTO> postLikes;
	private boolean isLiked;
	
	
	
	public PostReadResult() {
		
		this.post 			= null;
		this.postContents 	= new ArrayList<PostContentDTO>();
		this.postComments 	= new ArrayList<PostCommentDTO>();
		this.postLikes 		= new ArrayList<PostLikeDTO>();
		this.isLiked 		= false;
		
	}
	
}
