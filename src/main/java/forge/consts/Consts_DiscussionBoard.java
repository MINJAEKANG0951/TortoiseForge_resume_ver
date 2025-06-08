package forge.consts;

import java.util.HashMap;
import java.util.Map;

public class Consts_DiscussionBoard {

	
	// DISCUSSION BOARD
	
	public static final String DISUCSSION_BOARD_IMAGE_MAPPING_PATH	= "/discussion_board";
	
	public static final int POST_TYPE_NORMAL_POST 					= 0;
	public static final int POST_TYPE_ANNOUNCEMENT 					= 1;
	
	public static final int POST_CONTENT_TYPE_TEXT					= 0;
	public static final int POST_CONTENT_TYPE_BOLD_TEXT				= 1;
	public static final int POST_CONTENT_TYPE_IMAGE					= 2;
	
	public static final int MAXIMUM_NUMBER_OF_TEXT_CONTENTS			= 5;
	public static final int MAXIMUM_NUMBER_OF_BOLD_TEXT_CONTENTS	= 5;
	public static final int MAXIMUM_NUMBER_OF_IMAGE_CONTENTS		= 3;
	
	public static final int MAX_TITLE_LENGTH						= 100;
	public static final int MAX_TEXT_CONTENT_LENGTH					= 4000;				// CLOB
	public static final long MAX_IMG_FILE_SIZE 						= 10 * 1024 * 1024;	// 10MB
	public static final int MAX_COMMENT_LENGTH						= 500;				// CLOB

	// COMMENT CRUD RESULT
	public static final int COMMENT_RESULT_SUCCESSFUL					= 0;
	public static final int COMMENT_RESULT_FAILED_POST_NOT_FOUND		= 1;
	public static final int COMMENT_RESULT_FAILED_COMMENT_NOT_FOUND		= 2;
	public static final int COMMENT_RESULT_FAILED_NOT_AUTHORIZED		= 3;
	
	// POST CRUD RESULT
	public static final int POST_RESULT_SUCCESSFUL					= 0;
	public static final int POST_RESULT_FAILED_NOT_FOUND			= 2;
	public static final int POST_RESULT_FAILED_NOT_AUTHORIZED		= 3;
	
	
	public static final String ALLOWED_IMG_MIME_TYPE_JPG			= ".jpg";
	public static final String ALLOWED_IMG_MIME_TYPE_PNG			= ".png";
	public static final String ALLOWED_IMG_MIME_TYPE_GIF			= ".gif";
	
	public static final Map<String, String> ALLOWED_IMG_MIME_TYPES = new HashMap<>();
	
	static {
		ALLOWED_IMG_MIME_TYPES.put("image/jpeg", ALLOWED_IMG_MIME_TYPE_JPG);
		ALLOWED_IMG_MIME_TYPES.put("image/png", ALLOWED_IMG_MIME_TYPE_PNG);
		ALLOWED_IMG_MIME_TYPES.put("image/gif", ALLOWED_IMG_MIME_TYPE_GIF);
    }
	
	
}
