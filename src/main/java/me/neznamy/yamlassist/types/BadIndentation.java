package me.neznamy.yamlassist.types;

import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.yamlassist.SyntaxError;

public class BadIndentation extends SyntaxError {
	
	@Override
	public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
		return checkForIndent(fileLines);
	}

	private List<String> checkForIndent(List<String> lines) {
		List<String> suggestions = new ArrayList<String>();
		for (int lineId = 0; lineId < lines.size(); lineId++) {
			String line = lines.get(lineId);
			if (line.isEmpty()) continue;
			if (line.startsWith("#")) continue;
			line = line.split("#")[0];
			if (line.length() == 0 || line.replace(" ", "").length() == 0 || isComment(line)) continue;
			int currentLineIndent = getIndentCount(line);
			String prevLine = lineId == 0 ? "" : lines.get(lineId-1);
			int remove = 1;
			while (isComment(prevLine)) {
				int id = lineId-(remove++);
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
					suggestions.add("Remove " + (currentLineIndent-prevLineIndent-2) + " space(s) from line " + (lineId+1));
					lineId++;
					continue;
				}
				if (currentLineIndent - prevLineIndent == 1) {
					suggestions.add("Add 1 space to line " + (lineId+1));
					lineId++;
					continue;
				}
				if (prevLineIndent - currentLineIndent == 1) {
					if (line.replace(" ", "").startsWith("-")) {
						suggestions.add("Add 1 or 3 spaces to line " +  (lineId+1));
						lineId++;
						continue;
					} else {
						suggestions.add("Remove 1 space from line " + (lineId+1));
						lineId++;
						continue;
					}
				}
			} else {
				//expecting same indent count or 2k less (k = 1,2,..)
				if (currentLineIndent > prevLineIndent) {
					suggestions.add("Remove " + (currentLineIndent-prevLineIndent) + " space(s) from line " + (lineId+1));
					lineId++;
					continue;
				}
			}
			if (currentLineIndent%2 == 1) {
				suggestions.add("Add or remove one space at line " + (lineId+1));
				lineId++;
				continue;
			}
		}
		return suggestions;
	}
	
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
	
	private boolean isComment(String line) {
		return line.replace(" ", "").startsWith("#") || line.replace(" ", "").length() == 0;
	}
}