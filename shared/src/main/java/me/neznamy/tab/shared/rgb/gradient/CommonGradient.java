package me.neznamy.tab.shared.rgb.gradient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.rgb.TextColor;

public class CommonGradient extends GradientPattern {

	private Pattern pattern;
	private Pattern legacyPattern;
	private String containCheck;
	private int legacyCharPosition;
	private int startColorStart;
	private int messageStart;
	private int endColorStartSub;
	
	public CommonGradient(Pattern pattern, Pattern legacyPattern, String containCheck, int legacyCharPosition,
			int startColorStart, int messageStart, int endColorStartSub) {
		this.pattern = pattern;
		this.legacyPattern = legacyPattern;
		this.containCheck = containCheck;
		this.legacyCharPosition = legacyCharPosition;
		this.startColorStart = startColorStart;
		this.messageStart = messageStart;
		this.endColorStartSub = endColorStartSub;
	}
	
	@Override
	public String applyPattern(String text, boolean ignorePlaceholders) {
		if (!text.contains(containCheck)) return text;
		String replaced = text;
		Matcher m = legacyPattern.matcher(replaced);
		while (m.find()) {
			String format = m.group();
			EnumChatFormat legacyColor = EnumChatFormat.getByChar(format.charAt(legacyCharPosition));
			if ((ignorePlaceholders && format.contains("%")) || legacyColor == null) continue;
			TextColor start = new TextColor(format.substring(startColorStart, startColorStart+7), legacyColor);
			String message = format.substring(messageStart+2, format.length()-10);
			TextColor end = new TextColor(format.substring(format.length()-endColorStartSub, format.length()-endColorStartSub+7));
			String applied = asGradient(start, message, end);
			replaced = replaced.replace(format, applied);
		}
		m = pattern.matcher(replaced);
		while (m.find()) {
			String format = m.group();
			if (ignorePlaceholders && format.contains("%")) continue;
			TextColor start = new TextColor(format.substring(startColorStart, startColorStart+7));
			String message = format.substring(messageStart, format.length()-10);
			TextColor end = new TextColor(format.substring(format.length()-endColorStartSub, format.length()-endColorStartSub+7));
			String applied = asGradient(start, message, end);
			replaced = replaced.replace(format, applied);
		}
		return replaced;
	}
}
