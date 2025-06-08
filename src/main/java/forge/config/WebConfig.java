package forge.config;

import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import forge.consts.Consts_DiscussionBoard;
import forge.consts.Consts_General;
import forge.services.DAOService;

@Configuration
@EnableWebSecurity
public class WebConfig implements WebMvcConfigurer {

	
	private final DAOService daoService;
	public WebConfig(DAOService daoService) {
		this.daoService = daoService;
	}
	
	
	// FILE PATH CONFIGURATION
    private String currentDir = Paths.get("").toAbsolutePath().toString();
	
	private String discussionBoard_actualPath 	= getFileUri(currentDir + Consts_DiscussionBoard.DISUCSSION_BOARD_IMAGE_MAPPING_PATH + "/");
	private String discussionBoard_mappingPath 	= Consts_DiscussionBoard.DISUCSSION_BOARD_IMAGE_MAPPING_PATH + "/**";
	
	private String profile_actualPath 	= getFileUri(currentDir + Consts_General.USER_PROFILE_IMAGE_MAPPING_PATH + "/");
	private String profile_mappingPath 	= Consts_General.USER_PROFILE_IMAGE_MAPPING_PATH + "/**";
	
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler(discussionBoard_mappingPath)
				.addResourceLocations(discussionBoard_actualPath);
		
		registry.addResourceHandler(profile_mappingPath)
				.addResourceLocations(profile_actualPath);
	}
	

    // Method to handle different OS file URI formats
    private String getFileUri(String path) {
        if (isWindows()) {
            return "file:///" + path.replace("\\", "/");  // Windows uses file:///
        } else {
            return "file://" + path;  // Linux/Unix uses file:// without the extra slash
        }
    }

    // Check if the OS is Windows
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
	

    
    
    
    
    
    
    
    // INTERCEPTER CONFIGURATION
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	
        registry.addInterceptor(new UserTypeIntercepter(daoService))
                .addPathPatterns("/**")
                .excludePathPatterns("/img/**", "/css/**", "/js/**");
        
    }
    
    
    
}
