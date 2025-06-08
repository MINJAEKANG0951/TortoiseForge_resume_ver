package forge.model.discussionboard;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

@Data
public class PostLikeDTO {

	private int likeSeq;
	private int postSeq;
	private int userSeq;
	private OffsetDateTime likeTime;
	
}
