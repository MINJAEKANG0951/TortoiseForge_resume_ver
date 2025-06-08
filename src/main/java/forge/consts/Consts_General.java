package forge.consts;

import java.util.HashMap;
import java.util.Map;

public class Consts_General {

	// USER TYPE
	public static final int USER_TYPE_USER 			= 0;
	public static final int USER_TYPE_MANAGER 		= 1;
	public static final int USER_TYPE_THE_TORTOISE 	= 2;
	
	
	// USER STATUS
	public static final int USER_STATUS_ACTIVE 						= 0;
	public static final int USER_STATUS_SUSPENDED 					= 1;
	public static final int USER_STATUS_SCHEDULED_FOR_DELETION 		= 2;
	public static final int USER_STATUS_SCHEDULED_FOR_DEACTIVATION 	= 3;
	public static final int USER_STATUS_DEACTIVATED 				= 4;
	

	// WAY TO DELETE ACCOUNT
	public static final int ACCOUNT_DELETION_TYPE_ANONYMIZE	= 0;
	public static final int ACCOUNT_DELETION_TYPE_DELETE	= 1;

	// IN WHAT DAYS TO DELETE ACCOUNT
	public static final int ACCOUNT_DELETE_OR_ANONYMIZE_IN_WHAT_DAYS = 30;
	
	
	
	
	
	// USER PROFILE RELATED
	public static final int USER_ABOUT_ME_MAXLENGTH 			= 3000;
	public static final String USER_PROFILE_IMAGE_MAPPING_PATH	= "/profile";
	public static final long MAX_USER_PROFILE_IMAGE_FILE_SIZE 	= 5 * 1024 * 1024;		// 5MB
	
	
	// CRUD RESULT
	public static final int CRUD_SUCCESSFUL	= 0;
	public static final int CRUD_NOT_FOUND	= 1;
	public static final int CRUD_FAILURE	= 2;

	
	public static final Map<Integer, String> USER_TYPE_STRING = new HashMap<>();

	static {
		USER_TYPE_STRING.put(USER_TYPE_USER, 			"regular user");
		USER_TYPE_STRING.put(USER_TYPE_MANAGER, 		"manager");
		USER_TYPE_STRING.put(USER_TYPE_THE_TORTOISE, 	"admin");
    }
	
	
	public static final Map<String, String> ALLOWED_USER_PROFILE_IMAGE_MIME_TYPES = new HashMap<>();

	static {
		ALLOWED_USER_PROFILE_IMAGE_MIME_TYPES.put("image/jpeg", ".jpg");
		ALLOWED_USER_PROFILE_IMAGE_MIME_TYPES.put("image/png", ".png");
    }
	

	
}
