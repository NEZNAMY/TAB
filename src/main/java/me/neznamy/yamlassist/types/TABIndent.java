package me.neznamy.yamlassist.types;

import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.yamlassist.SyntaxError;

/**
 * Using the TAB key instead of 4 spaces to indent
 */
public class TABIndent extends SyntaxError {
	
	@Override
	public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
		List<String> suggestions = new ArrayList<String>();
		for (int i=1; i <= fileLines.size(); i++) {
			if (fileLines.get(i-1).contains("\t")) {
				suggestions.add("Replace \\t (TAB) with 4 spaces on line " + i + ".");
			}
		}
		return suggestions;
	}
}