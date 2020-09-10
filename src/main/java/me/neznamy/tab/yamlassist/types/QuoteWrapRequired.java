package me.neznamy.tab.yamlassist.types;

import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.yamlassist.SyntaxError;

public class QuoteWrapRequired extends SyntaxError {

	public QuoteWrapRequired(YAMLException exception, List<String> fileLines) {
		super(exception, fileLines);
	}

	@Override
	public boolean isType() {
		return exception.getMessage().contains("expected alphabetic or numeric character") ||
				exception.getMessage().contains("Do not use %");
	}

	@Override
	public String getSuggestion() {
		int line = Integer.parseInt(exception.getMessage().split(", line ")[1].split(",")[0]);
		return "Wrap value in line " + line + " into quotes.";
	}
}