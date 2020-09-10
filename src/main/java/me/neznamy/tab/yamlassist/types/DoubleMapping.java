package me.neznamy.tab.yamlassist.types;

import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.yamlassist.SyntaxError;

public class DoubleMapping extends SyntaxError {

	public DoubleMapping(YAMLException exception, List<String> fileLines) {
		super(exception, fileLines);
	}

	@Override
	public boolean isType() {
		int line = Integer.parseInt(exception.getMessage().split(", line ")[1].split(",")[0]);
		String text = fileLines.get(line-1).split("#")[0];
		return exception.getMessage().contains("mapping values are not allowed here") && text.endsWith(":");
	}

	@Override
	public String getSuggestion() {
		int line = Integer.parseInt(exception.getMessage().split(", line ")[1].split(",")[0]);
		return "Remove the last : from line " + line + ".";
	}
}