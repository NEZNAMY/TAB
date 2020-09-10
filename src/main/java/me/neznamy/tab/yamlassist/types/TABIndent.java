package me.neznamy.tab.yamlassist.types;

import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.yamlassist.SyntaxError;

public class TABIndent extends SyntaxError {

	public TABIndent(YAMLException exception, List<String> fileLines) {
		super(exception, fileLines);
	}

	@Override
	public boolean isType() {
		return exception.getMessage().contains("\\t(TAB)");
	}

	@Override
	public String getSuggestion() {
		int line = Integer.parseInt(exception.getMessage().split(", line ")[1].split(",")[0]);
		return "Replace \\t (TAB) with 4 spaces on line " + line + ".";
	}
}