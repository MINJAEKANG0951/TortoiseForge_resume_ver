package forge.model;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

@Data
public class UserDTO {

	private int userSeq;
	private String username;
	private String userPassword;
	private String userEmail;
	private String userImg;
	private String userAboutMe;
	private OffsetDateTime userCreationTime;
	private OffsetDateTime userUpdateTime;
	private int userStatus;
	private int roleSeq;
	
	
	public String getUserCreationTime() {
		if(this.userCreationTime == null) {
			return null;
		}
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return userCreationTime.format(formatter);
	}
	
	
	public String getUsername() {
		if(this.username == null || this.username.length()==0) {
			return "unknown";
		} else {
			return this.username;
		}
	}

	
}
