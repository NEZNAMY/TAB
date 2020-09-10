package me.neznamy.tab.yamlassist.types;

import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.yamlassist.SyntaxError;

public class BadIndentation extends SyntaxError {

	private String fix;
	public BadIndentation(YAMLException exception, List<String> fileLines) {
		super(exception, fileLines);
		fix = checkForIndent(fileLines);
	}

	@Override
	public boolean isType() {
		return fix != null;
	}

	@Override
	public String getSuggestion() {
		return fix;
	}
	
	private String checkForIndent(List<String> lines) {
		int lineId = -1;
		for (String line : lines) {
			lineId++;
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
					return "Remove " + (currentLineIndent-prevLineIndent-2) + " space(s) from line " + (lineId+1);
				}
				if (currentLineIndent - prevLineIndent == 1) {
					return "Add 1 space to line " + (lineId+1);
				}
				if (prevLineIndent - currentLineIndent == 1) {
					if (line.replace(" ", "").startsWith("-")) {
						return "Add 1 or 3 spaces to line " +  (lineId+1);
					} else {
						return "Remove 1 space from line " + (lineId+1);
					}
				}
			} else {
				//expecting same indent count or 2k less (k = 1,2,..)
				if (currentLineIndent > prevLineIndent) {
					return "Remove " + (currentLineIndent-prevLineIndent) + " space(s) from line " + (lineId+1);
				}
			}
			if (currentLineIndent%2 == 1) {
				return "Add or remove one space at line " + (lineId+1);
			}
		}
		return null;
	}
	private int getIndentCount(String line) {
		if (isComment(line)) return 0;
		int i = -1;
		while (line.charAt(++i) == ' ');
		return i;
	}
	private boolean isComment(String line) {
		return line.replace(" ", "").startsWith("#") || line.replace(" ", "").length() == 0;
	}
}