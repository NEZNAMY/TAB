package me.neznamy.tab.yamlassist;

import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

public abstract class SyntaxError {

	protected YAMLException exception;
	protected List<String> fileLines;
	
	public SyntaxError(YAMLException exception, List<String> fileLines) {
		this.exception = exception;
		this.fileLines = fileLines;
	}
	
	public abstract boolean isType();
	
	public abstract String getSuggestion();
}
