package forge.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class RecaptchaService 
{
	
    public RecaptchaService() {

    }
	
	private final String secretKey = "";	// secret key
	
	public boolean isTheUserHuman(String token)
	{
		String recaptchaApiUrl = "https://www.google.com/recaptcha/api/siteverify";
		
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("secret", secretKey);
        requestMap.add("response", token);
		
        ResponseEntity<GoogleRecaptchaResponse> response = 
        		restTemplate.postForEntity
        		(recaptchaApiUrl, requestMap, GoogleRecaptchaResponse.class);
        
        if (response.getBody() != null) {
            return response.getBody().isSuccess() && response.getBody().getScore() >= 0.5; 
            // google server 와 잘 통신하고 user의 점수가 0.5 이상이면 true return
        }
        
		return false;
	}
	
	
	public static class GoogleRecaptchaResponse 
	{
	    private boolean success;
	    private float score;
	    
		public boolean isSuccess() {
			return success;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public float getScore() {
			return score;
		}
		public void setScore(float score) {
			this.score = score;
		}
	}
	
}
