package me.neznamy.yamlassist;

import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

public abstract class SyntaxError {
	
	public abstract List<String> getSuggestions(YAMLException exception, List<String> fileLines);
}