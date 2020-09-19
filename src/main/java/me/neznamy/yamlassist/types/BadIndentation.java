package me.neznamy.yamlassist.types;

import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.yamlassist.SyntaxError;

/**
 * Incorrect amount of leading spaces
 */
public class BadIndentation extends SyntaxError {
	
	@Override
	public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
		return checkForIndent(fileLines);
	}

	/**
	 * The core method, really messy right now, will be reworked
	 * @param lines - lines of file
	 * @return - list of fix suggestions
	 */
	private List<String> checkForIndent(List<String> lines) {
		List<String> suggestions = new ArrayList<String>();
		for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
			String line = lines.get(lineNumber);
			line = line.split("#")[0];
			if (line.isEmpty()) continue;
			if (isComment(line)) continue;
			int currentLineIndent = getIndentCount(line);
			String prevLine = lineNumber == 0 ? "" : lines.get(lineNumber-1);
			int remove = 1;
			while (isComment(prevLine)) {
				int id = lineNumber-(remove++);
				if (id == -1) {
					prevLine = "";
					break;
				}
				prevLine = lines.get(id);
			}
			prevLine = prevLine.split("#")[0];
			int prevLineIndent = getIndentCount(prevLine);
			if (prevLine.replace(" ", "").endsWith(":")) {
				//expecting 2 more spaces or same or 2k less (k = 1,2,..)
				if (currentLineIndent - prevLineIndent > 2) {
					suggestions.add("Remove " + (currentLineIndent-prevLineIndent-2) + " space(s) from line " + (lineNumber+1));
					lineNumber++;
					continue;
				}
				if (currentLineIndent - prevLineIndent == 1) {
					suggestions.add("Add 1 space to line " + (lineNumber+1));
					lineNumber++;
					continue;
				}
				if (prevLineIndent - currentLineIndent == 1) {
					if (line.replace(" ", "").startsWith("-")) {
						suggestions.add("Add 1 or 3 spaces to line " +  (lineNumber+1));
						lineNumber++;
						continue;
					} else {
						suggestions.add("Remove 1 space from line " + (lineNumber+1));
						lineNumber++;
						continue;
					}
				}
			} else {
				//expecting same indent count or 2k less (k = 1,2,..)
				if (currentLineIndent > prevLineIndent) {
					suggestions.add("Remove " + (currentLineIndent-prevLineIndent) + " space(s) from line " + (lineNumber+1));
					lineNumber++;
					continue;
				}
			}
			if (currentLineIndent%2 == 1) {
				suggestions.add("Add or remove one space at line " + (lineNumber+1));
				lineNumber++;
				continue;
			}
		}
		return suggestions;
	}
	
	/**
	 * Returns amount of leading spaces in line of text
	 * @param line - line to check
	 * @return amount of leading spaces
	 */
	private int getIndentCount(String line) {
		if (isComment(line)) return 0;
		int i = 0;
		while (line.charAt(i) == ' ') {
			i++;
		}
		//not letting tab indent give invalid suggestions
		while (line.charAt(i) == '\t') {
			i += 4;
		}
		return i;
	}
	
	/**
	 * Return true if this line appears to be a comment only, false if not
	 * @param line - line of file
	 * @return true if comment, false otherwise
	 */
	private boolean isComment(String line) {
		return line.split("#")[0].replace(" ", "").length() == 0;
	}
}