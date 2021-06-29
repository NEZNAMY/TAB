package me.neznamy.tab.shared.rgb.format;

/**
 * Abstract class for different RGB patterns
 */
public interface RGBFormatter {

	/**
	 * Reformats RGB codes in provided text into #RRGGBB format
	 * @param text - text to format
	 * @return reformatted text
	 */
	public String reformat(String text);
}