package forge.model.general.custom;

import lombok.Data;

@Data
public class OutParam {

	private Integer result;
	
	public OutParam() {
		this.result = -1;
	}
	
}
