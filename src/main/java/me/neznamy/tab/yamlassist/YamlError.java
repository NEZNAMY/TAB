package me.neznamy.tab.yamlassist;

import java.util.ArrayList;
import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.yamlassist.types.BadIndentation;
import me.neznamy.tab.yamlassist.types.DoubleMapping;
import me.neznamy.tab.yamlassist.types.InvalidList;
import me.neznamy.tab.yamlassist.types.MissingQuote;
import me.neznamy.tab.yamlassist.types.MissingSpaceBeforeValue;
import me.neznamy.tab.yamlassist.types.QuoteWrapRequired;
import me.neznamy.tab.yamlassist.types.TABIndent;
import me.neznamy.tab.yamlassist.types.UnknownEscape;

public class YamlError {

	private List<SyntaxError> possibleErrors = new ArrayList<SyntaxError>();

	public YamlError(YAMLException exception, List<String> fileLines) {
		possibleErrors.add(new DoubleMapping(exception, fileLines));
		possibleErrors.add(new InvalidList(exception, fileLines));
		possibleErrors.add(new MissingQuote(exception, fileLines));
		possibleErrors.add(new MissingSpaceBeforeValue(exception, fileLines));
		possibleErrors.add(new QuoteWrapRequired(exception, fileLines));
		possibleErrors.add(new TABIndent(exception, fileLines));
		possibleErrors.add(new UnknownEscape(exception, fileLines));
		possibleErrors.add(new BadIndentation(exception, fileLines));
	}

	public String getSuggestion() {
		for (SyntaxError possibleError : possibleErrors) {
			if (possibleError.isType()) return possibleError.getSuggestion();
		}
		return null;
	}
}