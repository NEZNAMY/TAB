package me.neznamy.tab.api.chat.rgb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.TextColor;
import me.neznamy.tab.api.chat.rgb.format.BukkitFormat;
import me.neznamy.tab.api.chat.rgb.format.CMIFormat;
import me.neznamy.tab.api.chat.rgb.format.HtmlFormat;
import me.neznamy.tab.api.chat.rgb.format.RGBFormatter;
import me.neznamy.tab.api.chat.rgb.format.UnnamedFormat1;
import me.neznamy.tab.api.chat.rgb.gradient.CMIGradient;
import me.neznamy.tab.api.chat.rgb.gradient.CommonGradient;
import me.neznamy.tab.api.chat.rgb.gradient.GradientPattern;
import me.neznamy.tab.api.chat.rgb.gradient.KyoriGradient;

/**
 * A helper class to reformat all RGB formats into the default #RRGGBB and apply gradients
 */
public class RGBUtils {

	/** Instance of the class */
	private static final RGBUtils instance = new RGBUtils();
	
	/** Registered RGB formatters */
	private final RGBFormatter[] formats;
	
	/** Registered gradient patterns */
	private final GradientPattern[] gradients;
	
	/** TAB's RGB pattern, used to convert text to bukkit format for boss bar */
	private final Pattern tabPattern = Pattern.compile("#[0-9a-fA-F]{6}");

	/** RGB pattern for legacy codes */
	private final Pattern tabPatternLegacy = Pattern.compile("#[0-9a-fA-F]{6}\\|.");

	/**
	 * Constructs new instance and loads all RGB patterns and gradients
	 */
	public RGBUtils() {
		formats = new RGBFormatter[] {
				new BukkitFormat(),
				new CMIFormat(),
				new UnnamedFormat1(),
				new HtmlFormat()
		};
		gradients = new GradientPattern[] {
				//{#RRGGBB>}text{#RRGGBB<}
				new CMIGradient(),
				//<#RRGGBB>Text</#RRGGBB>
				new CommonGradient(Pattern.compile("<#[0-9a-fA-F]{6}>[^<]*</#[0-9a-fA-F]{6}>"), 
						Pattern.compile("<#[0-9a-fA-F]{6}\\|.>[^<]*</#[0-9a-fA-F]{6}>"), 
						"<#", 9, 2, 9, 7),
				//<$#RRGGBB>Text<$#RRGGBB>
				new CommonGradient(Pattern.compile("<\\$#[0-9a-fA-F]{6}>[^<]*<\\$#[0-9a-fA-F]{6}>"), 
						Pattern.compile("<\\$#[0-9a-fA-F]{6}\\|.>[^<]*<\\$#[0-9a-fA-F]{6}>"), 
						"<$", 10, 3, 10, 7),
				new KyoriGradient()
		};
	}
	
	/**
	 * Returns instance of this class
	 * @return	instance
	 */
	public static RGBUtils getInstance() {
		return instance;
	}
	
	/**
	 * Applies all RGB formats and gradients to text and returns it.
	 * This method is called on every property initialization to convert formats
	 * once instead of on every refresh, if possible. Because of that, parameter
	 * {@code ignorePlaceholders} exists to not break raw placeholder identifiers
	 * @param	text
	 * 			original text
	 * @param	ignorePlaceholders
	 * 			whether placeholders should be ignored or not
	 * @return	text where everything is converted to #RRGGBB
	 */
	public String applyFormats(String text, boolean ignorePlaceholders) {
		String replaced = text;
		for (GradientPattern pattern : gradients) {
			replaced = pattern.applyPattern(replaced, ignorePlaceholders);
		}
		for (RGBFormatter formatter : formats) {
			replaced = formatter.reformat(replaced);
		}
		return replaced;
	}

	/**
	 * Converts TAB's RGB format (#RRGGBB) into bukkit one (&x&r&r&g&g&b&b)
	 * for modern clients (1.16+), for legacy clients it will use the closest color.
	 * @param	text
	 * 			text to convert
	 * @param	rgbClient
	 * 			whether client accepts RGB or not
	 * @return	converted text
	 */
	public String convertToBukkitFormat(String text, boolean rgbClient) {
		if (text == null) return null;
		if (!text.contains("#")) return text; //no rgb codes
		if (rgbClient) {
			//converting random formats to TAB one
			String replaced = applyFormats(text, false);
			for (Pattern p : new Pattern[]{tabPatternLegacy, tabPattern}) {
				Matcher m = p.matcher(replaced);
				while (m.find()) {
					String hexCode = m.group();
					String fixed = "&x&" + hexCode.charAt(1) + "&" + hexCode.charAt(2) + "&" + hexCode.charAt(3) + "&" + hexCode.charAt(4) + "&" + hexCode.charAt(5) + "&" + hexCode.charAt(6);
					replaced = replaced.replace(hexCode, EnumChatFormat.color(fixed));
				}
			}
			return replaced;
		} else {
			return convertRGBtoLegacy(text);
		}
	}

	/**
	 * Converts all hex codes in given string to legacy codes
	 * @param	text
	 * 			text to convert
	 * @return	translated text
	 */
	public String convertRGBtoLegacy(String text) {
		if (text == null) return null;
		if (!text.contains("#")) return EnumChatFormat.color(text);
		String applied = applyFormats(text, false);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < applied.length(); i++){
			char c = applied.charAt(i);
			if (c == '#' && applied.length() > i+6) {
				String hexCode = applied.substring(i+1, i+7);
				if (isHexCode(hexCode)) {
					if (containsLegacyCode(applied, i)) {
						sb.append(new TextColor(hexCode, EnumChatFormat.getByChar(applied.charAt(i+8))).getLegacyColor().getFormat());
						i += 8;
					} else {
						sb.append(new TextColor(hexCode).getLegacyColor().getFormat());
						i += 6;
					}
				} else {
					sb.append(c);
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Returns true if entered string is a valid 6-digit combination of
	 * hexadecimal numbers, false if not
	 * @param	string
	 * 			string to check
	 * @return	{@code true} if valid, {@code false} if not
	 */
	public boolean isHexCode(String string) {
		if (string.length() != 6) return false;
		for (int i=0; i<6; i++) {
			char c = string.charAt(i);
			if (c < 48 || (c > 57 && c < 65) || (c > 70 && c < 97) || c > 102) return false;
		}
		return true;
	}
	
	/**
	 * Returns true if text contains legacy color request at defined RGB index start
	 * @param	text
	 * 			text to check
	 * @param	i
	 * 			current index start
	 * @return	{@code true} if legacy color is defined and valid, {@code false} otherwise
	 */
	private static boolean containsLegacyCode(String text, int i) {
		if (text.length() - i < 9 || text.charAt(i+7) != '|') return false;
		return EnumChatFormat.getByChar(text.charAt(i+8)) != null;
	}
}