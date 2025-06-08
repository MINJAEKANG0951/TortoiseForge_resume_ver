package forge.model.general.custom;

import lombok.Data;

@Data
public class KeyAndValue {

	private String key;
	private String value;
	
	public KeyAndValue(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
}
