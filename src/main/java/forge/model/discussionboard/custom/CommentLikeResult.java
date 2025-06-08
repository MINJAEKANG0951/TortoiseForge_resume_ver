package forge.model.discussionboard.custom;

import java.util.List;

import forge.model.discussionboard.PostCommentDTO;
import lombok.Data;

@Data
public class CommentLikeResult {

	
	private int commentLikeResult;
	
	private int howmanyLiked;
	private boolean isLiked;
	private List<PostCommentDTO> postComments;
	
}
