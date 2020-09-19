package me.neznamy.yamlassist;

import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

/**
 * An abstract class representing error finder
 */
public abstract class SyntaxError {
	
	/**
	 * Returns list of syntax errors found based on yaml exception and file content
	 * @param exception - the yaml exception
	 * @param fileLines - lines of file
	 * @return List of fix suggestions of this error type
	 */
	public abstract List<String> getSuggestions(YAMLException exception, List<String> fileLines);
}