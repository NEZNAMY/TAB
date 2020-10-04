package me.neznamy.yamlassist.types;

import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.yamlassist.SyntaxError;

/**
 * A missing ' or " at the beginning or ending of a value
 */
public class MissingQuote extends SyntaxError {

	@Override
	public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
		List<String> suggestions = new ArrayList<String>();
		for (int line = 1; line <= fileLines.size(); line++) {
			String text = fileLines.get(line-1);
			if (text.startsWith("#") || text.replace(" ", "").length() == 0) continue;
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

	/**
	 * Calls checkElement(String, int, String) with both ' and " and returns error message if any, null otherwise
	 * @param value - map value to be checked
	 * @param lineNumber - line number to be used in error message
	 * @return error message or null if nothing was found
	 */
	private String checkElement(String value, int lineNumber) {
		String result = null;
		if ((result = checkElement(value, lineNumber, "'")) != null) {
			return result;
		}
		if ((result = checkElement(value, lineNumber, "\"")) != null) {
			return result;
		}
		return null;
	}
	
	/**
	 * Checks value for all possible problems
	 * @param value - mapping value
	 * @param lineNumber - line number to be used in error message
	 * @param c - quote character, ' or " to prevent duplicated code
	 * @return error message or null if nothing was found
	 */
	private String checkElement(String value, int lineNumber, String c) {
		if (value.equals(c)) {
			return "Add " + c + " at the end of line " + lineNumber;
		}
		if (value.startsWith(c) && !value.endsWith(c)) {
			return "Add " + c + " at the end of line " + lineNumber;
		}
		if (value.endsWith(c) && !value.startsWith(c)) {
			return "Add " + c + " at the beginning of value at line " + lineNumber;
		}
		if (value.endsWith(c + c) && !value.equals(c + c)) {
			return "Remove one " + c + " from the end of line " + lineNumber;
		}
		return null;
	}

	/**
	 * Removes spaces at the beginning and end and returns it
	 * @param text - text to alter
	 * @return the altered text
	 */
	private String removeIndent(String text) {
		String result = text;
		while (result.startsWith(" ")) result = result.substring(1);
		while (result.endsWith(" ")) result = result.substring(0, result.length()-1);
		return result;
	}
}