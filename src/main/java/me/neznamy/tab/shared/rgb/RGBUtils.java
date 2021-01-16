package me.neznamy.tab.shared.rgb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.neznamy.tab.shared.TAB;

/**
 * A helper class to reformat all RGB formats into the default #RRGGBB and apply gradients
 */
public class RGBUtils {

	//pattern for {#RRGGBB}
	private final Pattern fix2 = Pattern.compile("\\{#[0-9a-fA-F]{6}\\}");
	
	//pattern for &x&R&R&G&G&B&B
	private final Pattern fix3 = Pattern.compile("\\\u00a7x[\\\u00a70-9a-fA-F]{12}");
	
	//pattern for #<RRGGBB>
	private final Pattern fix4 = Pattern.compile("#<[0-9a-fA-F]{6}>");
	
	//pattern for <#RRGGBB>Text</#RRGGBB>
	private final Pattern gradient1 = Pattern.compile("<#[0-9a-fA-F]{6}>[^<]*</#[0-9a-fA-F]{6}>");
	
	//pattern for {#RRGGBB>}text{#RRGGBB<}
	private final Pattern gradient2 = Pattern.compile("\\{#[0-9a-fA-F]{6}>\\}[^\\{]*\\{#[0-9a-fA-F]{6}<\\}");

	//pattern for <$#RRGGBB>Text<$#RRGGBB>
	private final Pattern gradient3 = Pattern.compile("<\\$#[0-9a-fA-F]{6}>[^<]*<\\$#[0-9a-fA-F]{6}>");

	/**
	 * Applies all RGB formats and gradients to text and returns it
	 * @param text - original text
	 * @return text where everything is converted to #RRGGBB
	 */
	public String applyFormats(String text) {
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
	private String fixFormat1(String text) {
		return text.replace("&#", "#");
	}

	/**
	 * Reformats {#RRGGBB} into #RRGGBB
	 * @param text - text to be reformatted
	 * @return reformatted text
	 */
	private String fixFormat2(String text) {
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
	private String fixFormat3(String text) {
		String replaced = text;
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
	private String fixFormat4(String text) {
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
	private String setGradient1(String text) {
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
	private String setGradient2(String text) {
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
	private String setGradient3(String text) {
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
	private String asGradient(TextColor start, String text, TextColor end) {
		//lazy support for magic codes in gradients
		String magicCodes = TAB.getInstance().getPlaceholderManager().getLastColors(text);
		String decolorized = text.substring(magicCodes.length());
		StringBuilder sb = new StringBuilder();
		int length = decolorized.length();
		for (int i=0; i<length; i++) {
			int red = (int) (start.getRed() + (float)(end.getRed() - start.getRed())/(length-1)*i);
			int green = (int) (start.getGreen() + (float)(end.getGreen() - start.getGreen())/(length-1)*i);
			int blue = (int) (start.getBlue() + (float)(end.getBlue() - start.getBlue())/(length-1)*i);
			sb.append("#" + new TextColor(red, green, blue).toHexString() + magicCodes + decolorized.charAt(i));
		}
		return sb.toString();
	}
}
