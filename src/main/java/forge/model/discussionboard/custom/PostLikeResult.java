package forge.model.discussionboard.custom;

import forge.model.discussionboard.PostLikeDTO;
import lombok.Data;

@Data
public class PostLikeResult {

	private int howmanyLiked;
	private PostLikeDTO postLike;
	
}
