package me.neznamy.tab.shared;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.neznamy.tab.shared.packets.IChatBaseComponent.TextColor;

/**
 * A helper class to reformat all RGB formats into the default #RRGGBB and apply gradients
 */
public class RGBUtils {

	private static final Pattern fix2 = Pattern.compile("\\{#[0-9a-fA-F]{6}\\}");
	private static final Pattern fix3 = Pattern.compile("\\&x[\\&0-9a-fA-F]{12}");
	private static final Pattern fix4 = Pattern.compile("#<[0-9a-fA-F]{6}>");
	private static final Pattern gradient1 = Pattern.compile("<#[0-9a-fA-F]{6}>[^<]*</#[0-9a-fA-F]{6}>");
	private static final Pattern gradient2 = Pattern.compile("\\{#[0-9a-fA-F]{6}>\\}[^\\{]*\\{#[0-9a-fA-F]{6}<\\}");

	public static String toHexString(int red, int green, int blue) {
		String s = Integer.toHexString((red << 16) + (green << 8) + blue);
		while (s.length() < 6) s = "0" + s;
		return s;
	}

	public static String applyFormats(String text) {
		text = fixFormat1(text);
		text = fixFormat2(text);
		text = fixFormat3(text);
		text = fixFormat4(text);
		text = setGradient1(text);
		text = setGradient2(text);
		return text;
	}

	//&#RRGGBB
	private static String fixFormat1(String text) {
		return text.replace("&#", "#");
	}

	//{#RRGGBB}
	private static String fixFormat2(String text) {
		Matcher m = fix2.matcher(text);
		while (m.find()) {
			String hexcode = m.group();
			String fixed = hexcode.substring(2, 8);
			text = text.replace(hexcode, "#" + fixed);
		}
		return text;
	}

	//&x&R&R&G&G&B&B
	private static String fixFormat3(String text) {
		text = text.replace('\u00a7', '&');
		Matcher m = fix3.matcher(text);
		while (m.find()) {
			String hexcode = m.group();
			String fixed = new String(new char[] {hexcode.charAt(3), hexcode.charAt(5), hexcode.charAt(7), hexcode.charAt(9), hexcode.charAt(11), hexcode.charAt(13)});
			text = text.replace(hexcode, "#" + fixed);
		}
		return text;
	}
	
	//#<RRGGBB>
	private static String fixFormat4(String text) {
		Matcher m = fix4.matcher(text);
		while (m.find()) {
			String hexcode = m.group();
			String fixed = hexcode.substring(2, 8);
			text = text.replace(hexcode, "#" + fixed);
		}
		return text;
	}

	//<#RRGGBB>Text</#RRGGBB>
	private static String setGradient1(String text) {
		Matcher m = gradient1.matcher(text);
		while (m.find()) {
			String format = m.group();
			TextColor start = new TextColor(format.substring(2, 8));
			String message = format.substring(9, format.length()-10);
			TextColor end = new TextColor(format.substring(format.length()-7, format.length()-1));
			String applied = asGradient(start, message, end);
			text = text.replace(format, applied);
		}
		return text;
	}

	//{#RRGGBB>}text{#RRGGBB<}
	private static String setGradient2(String text) {
		Matcher m = gradient2.matcher(text);
		while (m.find()) {
			String format = m.group();
			TextColor start = new TextColor(format.substring(2, 8));
			String message = format.substring(10, format.length()-10);
			TextColor end = new TextColor(format.substring(format.length()-8, format.length()-2));
			String applied = asGradient(start, message, end);
			text = text.replace(format, applied);
		}
		return text;
	}

	private static String asGradient(TextColor start, String text, TextColor end) {
		StringBuilder sb = new StringBuilder();
		int length = text.length();
		for (int i=0; i<length; i++) {
			int red = (int) (start.getRed() + (float)(end.getRed() - start.getRed())/(length-1)*i);
			int green = (int) (start.getGreen() + (float)(end.getGreen() - start.getGreen())/(length-1)*i);
			int blue = (int) (start.getBlue() + (float)(end.getBlue() - start.getBlue())/(length-1)*i);
			sb.append("#" + toHexString(red, green, blue) + text.charAt(i));
		}
		return sb.toString();
	}
}