package forge.model.discussionboard.custom;

import java.util.List;

import forge.model.discussionboard.PostContentDTO;
import lombok.Data;

@Data
public class PostUpdateResult {
	
	private int result;
	private List<PostContentDTO> formerPostContents;
	
	public PostUpdateResult() {
		this.result = -1;
	}
	
}
