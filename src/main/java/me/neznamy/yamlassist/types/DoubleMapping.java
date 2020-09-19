package me.neznamy.yamlassist.types;

import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.yamlassist.SyntaxError;

/**
 * Additional : used after value
 */
public class DoubleMapping extends SyntaxError {

	@Override
	public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
		List<String> suggestions = new ArrayList<String>();
		int line = Integer.parseInt(exception.getMessage().split(", line ")[1].split(",")[0]);
		String text = fileLines.get(line-1).split("#")[0];
		if (exception.getMessage().contains("mapping values are not allowed here") && text.endsWith(":")) {
			suggestions.add("Remove the last : from line " + line + ".");
		}
		return suggestions;
	}
}