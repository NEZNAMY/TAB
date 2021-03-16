package me.neznamy.tab.shared.rgb.gradient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.neznamy.tab.shared.rgb.TextColor;

/**
 * Gradient applier for <gradient:#RRGGBB:#RRGGBB>Text</gradient>
 */
public class KyoriGradient extends GradientPattern {

	// <gradient:#RRGGBB:#RRGGBB>Text</gradient>
	private final Pattern pattern = Pattern.compile("<gradient:#[0-9a-fA-F]{6}:#[0-9a-fA-F]{6}>[^<]*</gradient>");
	
	@Override
	public String applyPattern(String text) {
		String replaced = text;
		Matcher m = pattern.matcher(replaced);
		while (m.find()) {
			String format = m.group();
			TextColor start = new TextColor(format.substring(11, 17));
			String message = format.substring(26, format.length()-11);
			TextColor end = new TextColor(format.substring(19,25));
			String applied = asGradient(start, message, end);
			replaced = replaced.replace(format, applied);
		}
		return replaced;
	}
}