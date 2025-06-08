package forge.model.discussionboard;

import java.time.OffsetDateTime;

import lombok.Data;

@Data
public class PostCommentLikeDTO {

	private int likeSeq;
	private int commentSeq;
	private int userSeq;
	private OffsetDateTime likeTime;
	
	private int howmanyLiked;
	
}
