package forge.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


public class UsernameChecker {

	private static final String[] alphabet = {
			"a","b","c","d","e","f","g",
			"h","i","j","k","l","m","n",
			"o","p","q","r","s","t","u",
			"v","w","x","y","z"
	};
	
	private static final String[] numbers = {
			"0","1","2","3","4",
			"5","6","7","8","9"
	};
	
	public static final String[] etc = {"_"};
	
	
	public static boolean checkUsernameLength(String username)
	{
		if( username==null ||username.length()<5 || username.length()>=16) {
			return false;
		}
		
        return true;
	}
	
	public static boolean checkUsernameValid(String username)
	{
		for(int i=0;i<username.length();i++) {
			
			String letter = username.substring(i,i+1).toLowerCase();
			
			boolean flag1 = Arrays.asList(alphabet).contains(letter);
			boolean flag2 = Arrays.asList(numbers).contains(letter);
			boolean flag3 = Arrays.asList(etc).contains(letter);
			
			if(!flag1 && !flag2 && !flag3) {
				return false;
			}
			
		}
		
		return true;
	}
	
	
	
}