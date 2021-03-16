package me.neznamy.tab.shared.rgb.format;

/**
 * Formatter for &#RRGGBB
 */
public class UnnamedFormat1 extends RGBFormatter {

	@Override
	public String reformat(String text) {
		return text.replace("&#", "#");
	}
}