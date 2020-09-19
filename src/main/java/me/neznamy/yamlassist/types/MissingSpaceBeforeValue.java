package me.neznamy.yamlassist.types;

import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.yamlassist.SyntaxError;

/**
 * Using "xx:yy" instead of "xx: yy" or "-something" instead of "- something"
 */
public class MissingSpaceBeforeValue extends SyntaxError {

	@Override
	public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
		List<String> suggestions = new ArrayList<String>();
		if (exception.getMessage().contains("could not find expected ':'")) {
			int line = Integer.parseInt(exception.getMessage().split(", line ")[1].split(",")[0]);
			String text = fileLines.get(line-1).split("#")[0];
			while (text.startsWith(" ")) text = text.substring(1, text.length());
			if (text.startsWith("-") && !text.startsWith("- ")) {
				suggestions.add("Add a space after the \"-\" at line " + line + ".");
				return suggestions;
			}
			if (text.contains(":") && !text.contains(": ") && !text.endsWith(":")) {
				suggestions.add("Add a space after the \":\" at line " + line + ".");
				return suggestions;
			}
			suggestions.add("Remove line " + line + " or add ':' at the end");
		}
		return suggestions;
	}
}