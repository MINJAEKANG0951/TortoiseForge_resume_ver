package forge.model.messages.custom;

import java.util.ArrayList;
import java.util.List;

import forge.model.messages.MessageDTO;
import lombok.Data;

@Data
public class MessageSearchResult {

	private List<MessageDTO> messages;
	
	private int currentPageNum;
	private int currentPageSize;	
	
	private int totalNumberOfPages;
	private int totalNumberOfItems;
	
	private String messageType;
	
	private ArrayList<Integer> pagesAround;
	
	public MessageSearchResult() {
		
		this.currentPageNum 	= 1;
		this.currentPageSize 	= 20;
		this.totalNumberOfPages	= 0;
		this.totalNumberOfItems = 0;
		this.messages 			= new ArrayList<>();
		
	}

}
