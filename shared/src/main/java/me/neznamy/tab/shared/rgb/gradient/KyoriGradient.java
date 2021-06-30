package me.neznamy.tab.shared.rgb.gradient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.rgb.TextColor;

/**
 * Gradient applier for <gradient:#RRGGBB:#RRGGBB>Text</gradient> and <gradient:#RRGGBB|L:#RRGGBB>Text</gradient>
 */
public class KyoriGradient extends GradientPattern {

	private final Pattern pattern = Pattern.compile("<gradient:#[0-9a-fA-F]{6}:#[0-9a-fA-F]{6}>[^<]*</gradient>");

	private final Pattern patternLegacy = Pattern.compile("<gradient:#[0-9a-fA-F]{6}\\|.:#[0-9a-fA-F]{6}>[^<]*</gradient>");
	
	@Override
	public String applyPattern(String text, boolean ignorePlaceholders) {
		if (!text.contains("<grad")) return text;
		String replaced = text;
		Matcher m = patternLegacy.matcher(replaced);
		while (m.find()) {
			String format = m.group();
			EnumChatFormat legacyColor = EnumChatFormat.getByChar(format.charAt(18));
			if ((ignorePlaceholders && format.contains("%")) || legacyColor == null) continue;
			TextColor start = new TextColor(format.substring(10, 17), legacyColor);
			String message = format.substring(28, format.length()-11);
			TextColor end = new TextColor(format.substring(20, 27));
			String applied = asGradient(start, message, end);
			replaced = replaced.replace(format, applied);
		}
		m = pattern.matcher(replaced);
		while (m.find()) {
			String format = m.group();
			if (ignorePlaceholders && format.contains("%")) continue;
			TextColor start = new TextColor(format.substring(10, 17));
			String message = format.substring(26, format.length()-11);
			TextColor end = new TextColor(format.substring(18, 25));
			String applied = asGradient(start, message, end);
			replaced = replaced.replace(format, applied);
		}
		return replaced;
	}
}