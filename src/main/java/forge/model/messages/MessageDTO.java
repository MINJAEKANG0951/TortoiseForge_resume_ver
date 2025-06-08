package forge.model.messages;

import java.time.OffsetDateTime;

import lombok.Data;

@Data
public class MessageDTO {

	// COMMENT TABLE COLUMN
	private int messageSeq;
	private String messageContent;
	private OffsetDateTime messageCreationTime;
	
	private int senderSeq;
	private int recipientSeq;
	private OffsetDateTime openedAt;
	private OffsetDateTime readAt;
	private OffsetDateTime deletedAtSender;
	private OffsetDateTime deletedAtRecipient;
	
	
	// ADDITAIONAL DATA FROM TABLE JOIN
	private String senderName;
	private String recipientName;
	private String messagePreview;
	
	public String getSenderName() {
		if(this.senderName == null || this.senderName.length()==0) {
			return "unknown";
		} else {
			return this.senderName;
		}
	}
	
	public String getRecipientName() {
		if(this.recipientName == null || this.recipientName.length()==0) {
			return "unknown";
		} else {
			return this.recipientName;
		}
	}

}
