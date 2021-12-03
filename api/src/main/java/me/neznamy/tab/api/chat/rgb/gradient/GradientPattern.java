package me.neznamy.tab.api.chat.rgb.gradient;

import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.TextColor;

/**
 * Abstract class for applying different gradient patterns
 */
public interface GradientPattern {

	/**
	 * Applies gradients in provided text and returns text using only #RRGGBB
	 * @param text - text to be reformatted
	 * @return reformatted text
	 */
	String applyPattern(String text, boolean ignorePlaceholders);
	
	/**
	 * Returns gradient text based on start color, text and end color
	 * @param start - start color
	 * @param text - text to be reformatted
	 * @param end - end color
	 * @return reformatted text
	 */	
	default String asGradient(TextColor start, String text, TextColor end) {
		//lazy support for magic codes in gradients
		String magicCodes = EnumChatFormat.getLastColors(text);
		String deColorized = text.substring(magicCodes.length());
		StringBuilder sb = new StringBuilder();
		int length = deColorized.length();
		if (length == 1) {
			sb.append("#");
			sb.append(new TextColor(start.getRed(), start.getGreen(), start.getBlue()).getHexCode());
			if (start.isLegacyColorForced()) sb.append("|").append(start.getLegacyColor().getCharacter());
			sb.append(magicCodes);
			sb.append(deColorized);
			return sb.toString();
		}
		for (int i=0; i<length; i++) {
			int red = (int) (start.getRed() + (float)(end.getRed() - start.getRed())/(length-1)*i);
			int green = (int) (start.getGreen() + (float)(end.getGreen() - start.getGreen())/(length-1)*i);
			int blue = (int) (start.getBlue() + (float)(end.getBlue() - start.getBlue())/(length-1)*i);
			sb.append("#");
			sb.append(new TextColor(red, green, blue).getHexCode());
			if (start.isLegacyColorForced()) sb.append("|").append(start.getLegacyColor().getCharacter());
			sb.append(magicCodes);
			sb.append(deColorized.charAt(i));
		}
		return sb.toString();
	}
}