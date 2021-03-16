package me.neznamy.tab.shared.rgb.gradient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.rgb.TextColor;

/**
 * Gradient applier for <#RRGGBB>Text</#RRGGBB>
 */
public class HtmlGradient extends GradientPattern {

	// <#RRGGBB>Text</#RRGGBB>
	private Pattern pattern = Pattern.compile("<#[0-9a-fA-F]{6}>[^<]*</#[0-9a-fA-F]{6}>");
	
	// <#RRGGBB|L>Text</#RRGGBB>
	private Pattern patternLegacy = Pattern.compile("<#[0-9a-fA-F]{6}\\|.>[^<]*</#[0-9a-fA-F]{6}>");
	
	@Override
	public String applyPattern(String text) {
		String replaced = text;
		Matcher m = patternLegacy.matcher(replaced);
		while (m.find()) {
			String format = m.group();
			EnumChatFormat legacyColor = EnumChatFormat.getByChar(format.charAt(9));
			if (legacyColor == null) continue;
			TextColor start = new TextColor(format.substring(2, 8), legacyColor);
			String message = format.substring(11, format.length()-10);
			TextColor end = new TextColor(format.substring(format.length()-7, format.length()-1));
			String applied = asGradient(start, message, end);
			replaced = replaced.replace(format, applied);
		}
		m = pattern.matcher(replaced);
		while (m.find()) {
			String format = m.group();
			TextColor start = new TextColor(format.substring(2, 8));
			String message = format.substring(9, format.length()-10);
			TextColor end = new TextColor(format.substring(format.length()-7, format.length()-1));
			String applied = asGradient(start, message, end);
			replaced = replaced.replace(format, applied);
		}
		return replaced;
	}
}