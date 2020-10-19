package me.neznamy.yamlassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.yamlassist.types.BadIndentation;
import me.neznamy.yamlassist.types.DoubleMapping;
import me.neznamy.yamlassist.types.InvalidList;
import me.neznamy.yamlassist.types.MissingQuote;
import me.neznamy.yamlassist.types.MissingSpaceBeforeValue;
import me.neznamy.yamlassist.types.QuoteWrapRequired;
import me.neznamy.yamlassist.types.TABIndent;
import me.neznamy.yamlassist.types.UnknownEscape;

/**
 * The core class of yamlassist
 */
public class YamlAssist {

	//registered error checkers
	private static Map<Class<? extends SyntaxError>, SyntaxError> registeredSyntaxErrors = new HashMap<Class<? extends SyntaxError>, SyntaxError>();

	/**
	 * Registers predefined checkers
	 */
	static {
		registerSyntaxError(new DoubleMapping());
		registerSyntaxError(new InvalidList());
		registerSyntaxError(new MissingQuote());
		registerSyntaxError(new MissingSpaceBeforeValue());
		registerSyntaxError(new QuoteWrapRequired());
		registerSyntaxError(new TABIndent());
		registerSyntaxError(new UnknownEscape());
		registerSyntaxError(new BadIndentation());
	}

	/**
	 * Returns list of all syntax errors found based on yaml exception and file content
	 * @param exception - the yaml exception
	 * @param fileLines - lines of file
	 * @return List of fix suggestions
	 */
	public static List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
		List<String> suggestions = new ArrayList<String>();
		for (SyntaxError possibleError : registeredSyntaxErrors.values()) {
			suggestions.addAll(possibleError.getSuggestions(exception, fileLines));
		}
		return suggestions;
	}
	
	/**
	 * Allows anyone to register their own syntax error finder
	 * @param error - the error finder
	 */
	public static void registerSyntaxError(SyntaxError error) {
		registeredSyntaxErrors.put(error.getClass(), error);
	}
}