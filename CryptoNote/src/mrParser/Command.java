package mrParser;

import java.util.List;

public class Command {
	private String com;
	private List<String> prop;
	private boolean isValid;

	public Command(String commandValue, List<String> proporties, boolean isValid) {
		com = commandValue;
		prop = proporties;
		this.isValid = isValid;
	}

	public String getCommand() {
		return com;
	}

	public List<String> getProp() {
		return prop;
	}

	public boolean isValid() {
		return isValid;
	}

	public boolean is(String compString) {
		
		if(!isValid)
			return false;
		
		return com.contains(compString);
	}
}
