package me.neznamy.tab.yamlassist.types;

import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.yamlassist.SyntaxError;

public class UnknownEscape extends SyntaxError {

	public UnknownEscape(YAMLException exception, List<String> fileLines) {
		super(exception, fileLines);
	}
	
	@Override
	public boolean isType() {
		return exception.getMessage().contains("found unknown escape character");
	}

	@Override
	public String getSuggestion() {
		int line = Integer.parseInt(exception.getMessage().split(", line ")[1].split(",")[0]);
		return "Remove the \\ from line " + line + ".";
	}
}