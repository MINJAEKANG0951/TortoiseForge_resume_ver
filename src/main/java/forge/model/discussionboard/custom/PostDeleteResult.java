package forge.model.discussionboard.custom;

import java.util.List;

import forge.model.discussionboard.PostContentDTO;
import lombok.Data;

@Data
public class PostDeleteResult {

	private int result;
	private List<PostContentDTO> postContents;
	
	public PostDeleteResult() {
		this.result = -1;
	}
	
}
