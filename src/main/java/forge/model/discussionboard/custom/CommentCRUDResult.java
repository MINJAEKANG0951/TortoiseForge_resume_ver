package forge.model.discussionboard.custom;

import java.util.List;

import forge.model.discussionboard.PostCommentDTO;
import lombok.Data;

@Data
public class CommentCRUDResult {

	private int result;
	private List<PostCommentDTO> postComments;
	
}
