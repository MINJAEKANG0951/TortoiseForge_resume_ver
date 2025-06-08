package forge.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import forge.services.DAOService;

@Service
public class MessageDeletionScheduler {

	
	private static final Logger logger = LoggerFactory.getLogger(MessageDeletionScheduler.class);
	
	private final DAOService daoService;
	
	public MessageDeletionScheduler(DAOService daoService) {
		this.daoService = daoService;
	}
	
	@Async
	@Scheduled(fixedRate = 3600000)	// every an hour
    public void executeMesasgeDeletion() {
		try
		{
			daoService.purgeMessages();
		}
		catch(Exception e)
		{
			logger.info("purging messages failed.");
			e.printStackTrace();
		}
    }
	
}
