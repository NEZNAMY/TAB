package me.neznamy.tab.shared;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.neznamy.tab.shared.packets.IChatBaseComponent.TextColor;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * A helper class to reformat all RGB formats into the default #RRGGBB and apply gradients
 */
public class RGBUtils {

	//pattern for {#RRGGBB}
	private static final Pattern fix2 = Pattern.compile("\\{#[0-9a-fA-F]{6}\\}");
	
	//pattern for &x&R&R&G&G&B&B
	private static final Pattern fix3 = Pattern.compile("\\&x[\\&0-9a-fA-F]{12}");
	
	//pattern for #<RRGGBB>
	private static final Pattern fix4 = Pattern.compile("#<[0-9a-fA-F]{6}>");
	
	//pattern for <#RRGGBB>Text</#RRGGBB>
	private static final Pattern gradient1 = Pattern.compile("<#[0-9a-fA-F]{6}>[^<]*</#[0-9a-fA-F]{6}>");
	
	//pattern for {#RRGGBB>}text{#RRGGBB<}
	private static final Pattern gradient2 = Pattern.compile("\\{#[0-9a-fA-F]{6}>\\}[^\\{]*\\{#[0-9a-fA-F]{6}<\\}");

	//pattern for <$#RRGGBB>Text<$#RRGGBB>
	private static final Pattern gradient3 = Pattern.compile("<\\$#[0-9a-fA-F]{6}>[^<]*<\\$#[0-9a-fA-F]{6}>");

	/**
	 * Returns a 6-digit HEX output from given colors
	 * @param red - red
	 * @param green - green
	 * @param blue - blue
	 * @return the hex string
	 */
	public static String toHexString(int red, int green, int blue) {
		String s = Integer.toHexString((red << 16) + (green << 8) + blue);
		while (s.length() < 6) s = "0" + s;
		return s;
	}

	/**
	 * Applies all RGB formats and gradients to text and returns it
	 * @param text - original text
	 * @return text where everything is converted to #RRGGBB
	 */
	public static String applyFormats(String text) {
		String replaced = fixFormat1(text);
		replaced = fixFormat2(replaced);
		replaced = fixFormat3(replaced);
		replaced = fixFormat4(replaced);
		replaced = setGradient1(replaced);
		replaced = setGradient2(replaced);
		replaced = setGradient3(replaced);
		return replaced;
	}

	/**
	 * Reformats &#RRGGBB into #RRGGBB
	 * @param text - text to be reformatted
	 * @return reformatted text
	 */
	private static String fixFormat1(String text) {
		return text.replace("&#", "#");
	}

	/**
	 * Reformats {#RRGGBB} into #RRGGBB
	 * @param text - text to be reformatted
	 * @return reformatted text
	 */
	private static String fixFormat2(String text) {
		Matcher m = fix2.matcher(text);
		String replaced = text;
		while (m.find()) {
			String hexcode = m.group();
			String fixed = hexcode.substring(2, 8);
			replaced = replaced.replace(hexcode, "#" + fixed);
		}
		return replaced;
	}

	/**
	 * Reformats &x&R&R&G&G&B&B into #RRGGBB
	 * @param text - text to be reformatted
	 * @return reformatted text
	 */
	private static String fixFormat3(String text) {
		String replaced = text.replace('\u00a7', '&');
		Matcher m = fix3.matcher(replaced);
		while (m.find()) {
			String hexcode = m.group();
			String fixed = new String(new char[] {hexcode.charAt(3), hexcode.charAt(5), hexcode.charAt(7), hexcode.charAt(9), hexcode.charAt(11), hexcode.charAt(13)});
			replaced = replaced.replace(hexcode, "#" + fixed);
		}
		return replaced;
	}
	
	/**
	 * Reformats #<RRGGBB> into #RRGGBB
	 * @param text - text to be reformatted
	 * @return reformatted text
	 */
	private static String fixFormat4(String text) {
		Matcher m = fix4.matcher(text);
		String replaced = text;
		while (m.find()) {
			String hexcode = m.group();
			String fixed = hexcode.substring(2, 8);
			replaced = replaced.replace(hexcode, "#" + fixed);
		}
		return replaced;
	}

	/**
	 * Applies gradients formatted with <#RRGGBB>Text</#RRGGBB> and returns text using only #RRGGBB
	 * @param text - text to be reformatted
	 * @return reformatted text
	 */
	private static String setGradient1(String text) {
		Matcher m = gradient1.matcher(text);
		String replaced = text;
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

	/**
	 * Applies gradients formatted with {#RRGGBB>}text{#RRGGBB<} and returns text using only #RRGGBB
	 * @param text - text to be reformatted
	 * @return reformatted text
	 */
	private static String setGradient2(String text) {
		Matcher m = gradient2.matcher(text);
		String replaced = text;
		while (m.find()) {
			String format = m.group();
			TextColor start = new TextColor(format.substring(2, 8));
			String message = format.substring(10, format.length()-10);
			TextColor end = new TextColor(format.substring(format.length()-8, format.length()-2));
			String applied = asGradient(start, message, end);
			replaced = replaced.replace(format, applied);
		}
		return replaced;
	}

	/**
	 * Applies gradients formatted with <$#RRGGBB>text<$#RRGGBB> and returns text using only #RRGGBB
	 * @param text - text to be reformatted
	 * @return reformatted text
	 */
	private static String setGradient3(String text) {
		Matcher m = gradient3.matcher(text);
		String replaced = text;
		while (m.find()) {
			String format = m.group();
			TextColor start = new TextColor(format.substring(3, 9));
			String message = format.substring(10, format.length()-10);
			TextColor end = new TextColor(format.substring(format.length()-7, format.length()-1));
			String applied = asGradient(start, message, end);
			replaced = replaced.replace(format, applied);
		}
		return replaced;
	}


	/**
	 * Returns gradient text based on start color, end color and text
	 * @param start - start color
	 * @param text - text to be reformatted
	 * @param end - end color
	 * @return reformatted text
	 */
	private static String asGradient(TextColor start, String text, TextColor end) {
		String colors = Placeholders.getLastColors(Placeholders.color(text));
		String decolorized = text.substring(colors.length());
		StringBuilder sb = new StringBuilder();
		int length = decolorized.length();
		for (int i=0; i<length; i++) {
			int red = (int) (start.getRed() + (float)(end.getRed() - start.getRed())/(length-1)*i);
			int green = (int) (start.getGreen() + (float)(end.getGreen() - start.getGreen())/(length-1)*i);
			int blue = (int) (start.getBlue() + (float)(end.getBlue() - start.getBlue())/(length-1)*i);
			sb.append("#" + toHexString(red, green, blue) + colors + decolorized.charAt(i));
		}
		return sb.toString();
	}
}
