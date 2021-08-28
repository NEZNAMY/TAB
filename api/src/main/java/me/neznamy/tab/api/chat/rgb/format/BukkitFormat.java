package me.neznamy.tab.api.chat.rgb.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.neznamy.tab.api.chat.EnumChatFormat;

/**
 * Formatter for &x&R&R&G&G&B&B
 */
public class BukkitFormat implements RGBFormatter {

	private final Pattern pattern = Pattern.compile("[" + EnumChatFormat.COLOR_CHAR + "&]x[[" + EnumChatFormat.COLOR_CHAR + "&]\\p{XDigit}]{12}");
	
	@Override
	public String reformat(String text) {
		if (!text.contains("&") && !text.contains(EnumChatFormat.COLOR_CHAR + "x")) return text;
		String replaced = text;
		Matcher m = pattern.matcher(replaced);
		while (m.find()) {
			String hexcode = m.group();
			String fixed = new String(new char[] {'#', hexcode.charAt(3), hexcode.charAt(5), hexcode.charAt(7), hexcode.charAt(9), hexcode.charAt(11), hexcode.charAt(13)});
			replaced = replaced.replace(hexcode, fixed);
		}
		return replaced;
	}
}