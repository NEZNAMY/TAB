package me.neznamy.tab.shared.rgb.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formatter for &x&R&R&G&G&B&B
 */
public class BukkitFormat implements RGBFormatter {

	private final Pattern pattern = Pattern.compile("[\\\u00a7&]{1}x[[\\\u00a7&]{1}0-9a-fA-F]{12}");
	
	@Override
	public String reformat(String text) {
		if (!text.contains("&") && !text.contains("\u00a7x")) return text;
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