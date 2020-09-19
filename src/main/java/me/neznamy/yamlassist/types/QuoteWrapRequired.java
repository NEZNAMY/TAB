package me.neznamy.yamlassist.types;

import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.yamlassist.SyntaxError;

/**
 * String starting with "%", "&" or other character that is not allowed without quotes
 */
public class QuoteWrapRequired extends SyntaxError {

	@Override
	public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
		List<String> suggestions = new ArrayList<String>();
		if (exception.getMessage().contains("expected alphabetic or numeric character") ||
			exception.getMessage().contains("Do not use %")) {
			int line = Integer.parseInt(exception.getMessage().split(", line ")[1].split(",")[0]);
			suggestions.add("Wrap value in line " + line + " into quotes.");
		}
		return suggestions;
	}
}