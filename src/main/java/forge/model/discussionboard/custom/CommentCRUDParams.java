package forge.model.discussionboard.custom;

import lombok.Data;

@Data
public class CommentCRUDParams {

	private int userSeq;
	private int postSeq;
	private Integer subjectCommentSeq;
	private String commentContent;
	private int result;
	
	public CommentCRUDParams() {
		this.result = -1;
	}
	
}
