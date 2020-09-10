package me.neznamy.tab.yamlassist.types;

import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.yamlassist.SyntaxError;

public class MissingSpaceBeforeValue extends SyntaxError {

	public MissingSpaceBeforeValue(YAMLException exception, List<String> fileLines) {
		super(exception, fileLines);
	}

	@Override
	public boolean isType() {
		return exception.getMessage().contains("could not find expected ':'");
	}

	@Override
	public String getSuggestion() {
		int line = Integer.parseInt(exception.getMessage().split(", line ")[1].split(",")[0]);
		String text = fileLines.get(line-1).split("#")[0];
		while (text.startsWith(" ")) text = text.substring(1, text.length());
		if (text.startsWith("-") && !text.startsWith("- ")) {
			return "Add a space after the \"-\" at line " + line + ".";
		}
		if (text.contains(":") && !text.contains(": ") && !text.endsWith(":")) {
			return "Add a space after the \":\" at line " + line + ".";
		}
		return "Remove line " + line + " or add ':' at the end";
	}
}