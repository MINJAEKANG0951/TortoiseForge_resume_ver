package forge.model.discussionboard;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class PostContentDTO {

	// POST CONTENT TABLE COLUMN
	private int contentSeq;
	private String contentValue;
	private int typeSeq;
	private int postSeq;
	
	
	// ADDIATIONAL DATA NEEDED FOR PROGRAMMING
	private MultipartFile file;
	private String fileMimeType;
	
}
