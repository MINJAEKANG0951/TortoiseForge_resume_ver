package forge.scheduler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import forge.consts.Consts_General;
import forge.controller.Controller_DiscussionBoard_Post_Create;
import forge.model.UserDTO;
import forge.model.discussionboard.PostContentDTO;
import forge.services.DAOService;
import forge.services.FileStorageService;

@Service
public class AccountDeletionScheduler {

	private static final Logger logger = LoggerFactory.getLogger(AccountDeletionScheduler.class);
	
	private final DAOService daoService;
	private final FileStorageService fileStorageService;
	
	public AccountDeletionScheduler(DAOService daoService, FileStorageService fileStorageService) {
		this.daoService = daoService;
		this.fileStorageService = fileStorageService;
	};
	
	
	
	@Async
	@Scheduled(cron = "0 0 03 * * ?")
	//@Scheduled(fixedRate = 10000)
    public void executeAccountDeletion() {
		try
		{
			// ANONYMIZE ACCOUNTS
			daoService.anonymizeAccounts(
					Consts_General.USER_STATUS_DEACTIVATED,
					Consts_General.USER_STATUS_SCHEDULED_FOR_DEACTIVATION, 
					Consts_General.ACCOUNT_DELETE_OR_ANONYMIZE_IN_WHAT_DAYS
			);
			
			
			// DELETE ACCOUNTS
			List<UserDTO> usersToDelete 
			= daoService.getAccountsToDelete(
				Consts_General.USER_STATUS_SCHEDULED_FOR_DELETION,
				Consts_General.ACCOUNT_DELETE_OR_ANONYMIZE_IN_WHAT_DAYS
			);
		
		
			for(UserDTO userToDelete : usersToDelete) {
				
				// MESSAGE DELETION
				daoService.unlinkUserFromMessages(userToDelete.getUserSeq());
				
				// POST IMAGE CONTENT DELETION
				List<PostContentDTO> imagePostContentsToDelete = 
						daoService.getImagePostContentsToDelete(userToDelete.getUserSeq());
				
				for(PostContentDTO imageContent : imagePostContentsToDelete) {
					boolean imgDeleteResult = fileStorageService.deleteFile(imageContent.getContentValue().replaceFirst("\\/", ""));
					if(imgDeleteResult) {
						logger.info("image : " + imageContent.getContentValue().replaceFirst("\\/", "") + " has been deleted successfully.");
					}
				}
				
				
				// PURGE ACCOUNT EVENTUALLY
				daoService.deleteAccount(userToDelete.getUserSeq());
				logger.info("user : " + userToDelete.getUsername() + " has been successfully deleted.");
			}
			
			// 다 하고 익명화,삭제 완료 된 유저한테 메일까지 보내주는 기능 추가하면 좋을듯??
		}
		catch(Exception e)
		{
			logger.info("account deletion failed.");
			e.printStackTrace();
		}
    }
	

}
