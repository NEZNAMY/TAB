package me.neznamy.yamlassist.types;

import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.yamlassist.SyntaxError;

public class UnknownEscape extends SyntaxError {

	private char[] validEscapedCharacters = new char[] {'\\', 'b', 'f', 'n', 'r', 't'};
	
	@Override
	public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
		List<String> suggestions = new ArrayList<String>();
		for (int i=1; i <= fileLines.size(); i++) {
			String line = fileLines.get(i-1);
			for (int j=0; j<line.length(); j++) {
				if (line.charAt(j) == '\\' && !isValidEscapedCharacter(line.charAt(j+1))) {
					suggestions.add("Remove the \\ from line " + i + " or add another one after it to make the character display properly.");
					j++;
				}
			}
		}
		return suggestions;
	}
	
	private boolean isValidEscapedCharacter(char c) {
		for (char valid : validEscapedCharacters) {
			if (c == valid) return true;
		}
		return false;
	}
}