package forge.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import forge.consts.Consts_General;
import forge.model.UserDTO;
import forge.services.DAOService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class UserTypeIntercepter implements HandlerInterceptor {

	private final DAOService daoService;
	
	public UserTypeIntercepter(DAOService daoService) {
		this.daoService = daoService;
	}
	
	
	@Override
	public boolean preHandle
	(HttpServletRequest req, HttpServletResponse res, Object handler) 
	{
		try
		{
			if( req.getSession().getAttribute("userSeq") != null ) {
				
				int userSeq = Integer.parseInt(req.getSession().getAttribute("userSeq").toString());
				UserDTO userInfo = daoService.getUserBySeq(userSeq);
				
				if(userInfo == null) {												// ONLY VALID USER CAN HAVE A SESSION(LOGGED IN)
					req.getSession().setAttribute("userSeq", null);
					req.getSession().setAttribute("username", null);
				}
				else if(userInfo.getUserStatus() != Consts_General.USER_STATUS_ACTIVE) 	// ONLY ACTIVE USER CAN HAVE A SESSION(LOGGED IN)
				{
					req.getSession().setAttribute("userSeq", null);
					req.getSession().setAttribute("username", null);
				}
				
			} 
			
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}

	}




}
