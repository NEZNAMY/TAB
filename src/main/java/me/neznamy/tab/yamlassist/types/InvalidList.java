package me.neznamy.tab.yamlassist.types;

import java.util.List;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.yamlassist.SyntaxError;

public class InvalidList extends SyntaxError {

	public InvalidList(YAMLException exception, List<String> fileLines) {
		super(exception, fileLines);
	}

	@Override
	public boolean isType() {
		return exception.getMessage().contains("expected <block end>, but found '-'");
	}

	@Override
	public String getSuggestion() {
		int line1 = Integer.parseInt(exception.getMessage().split(", line ")[1].split(",")[0]);
		int line2 = Integer.parseInt(exception.getMessage().split(", line ")[2].split(",")[0]);
		if (fileLines.get(line2-2).endsWith(":")) {
			return "List starting at line " + line2 + " seems to be starting at line " + line1 + " already. Make sure indenting is correct.";
		} else {
			return "List starting at line " + line2 + " is missing a name.";
		}
	}
}