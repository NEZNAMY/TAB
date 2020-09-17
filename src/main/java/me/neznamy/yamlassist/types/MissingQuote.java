package me.neznamy.yamlassist.types;

import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.yamlassist.SyntaxError;

public class MissingQuote extends SyntaxError {

	@Override
	public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
		List<String> suggestions = new ArrayList<String>();
		for (int line = 1; line <= fileLines.size(); line++) {
			String text = fileLines.get(line-1).split("#")[0];
			if (text.replace(" ", "").length() == 0) continue;
			text = removeIndent(text);
			String suggestion = null;
			if (text.startsWith("- ")) {
				text = text.substring(text.split("- ")[0].length()+2);
				suggestion = checkElement(text, line);
			} else if (text.contains(": ")) {
				text = text.substring(text.split(": ")[0].length()+2);
				suggestion = checkElement(text, line);
			}
			if (suggestion != null) suggestions.add(suggestion);
		}
		return suggestions;
	}

	private String checkElement(String value, int lineID) {
		String result = null;
		if ((result = checkElement(value, lineID, "'")) != null) {
			return result;
		}
		if ((result = checkElement(value, lineID, "\"")) != null) {
			return result;
		}
		return null;
	}
	
	private String checkElement(String value, int lineID, String c) {
		if (value.equals(c)) {
			return "Add " + c + " at the end of line " + lineID;
		}
		if (value.startsWith(c) && !value.endsWith(c)) {
			return "Add " + c + " at the end of line " + lineID;
		}
		if (value.endsWith(c) && !value.startsWith(c)) {
			return "Add " + c + " at the beginning of value at line " + lineID;
		}
		if (value.endsWith(c + c) && !value.equals(c + c)) {
			return "Remove one " + c + " from the end of line " + lineID;
		}
		return null;
	}

	private String removeIndent(String text) {
		String result = text;
		while (result.startsWith(" ")) result = result.substring(1);
		while (result.endsWith(" ")) result = result.substring(0, result.length()-1);
		return result;
	}
}