package me.neznamy.tab.api.chat.rgb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.chat.TextColor;
import me.neznamy.tab.api.chat.rgb.format.BukkitFormat;
import me.neznamy.tab.api.chat.rgb.format.CMIFormat;
import me.neznamy.tab.api.chat.rgb.format.HtmlFormat;
import me.neznamy.tab.api.chat.rgb.format.RGBFormatter;
import me.neznamy.tab.api.chat.rgb.format.UnnamedFormat1;
import me.neznamy.tab.api.chat.rgb.gradient.CommonGradient;
import me.neznamy.tab.api.chat.rgb.gradient.GradientPattern;
import me.neznamy.tab.api.chat.rgb.gradient.KyoriGradient;

/**
 * A helper class to reformat all RGB formats into the default #RRGGBB and apply gradients
 */
public class RGBUtils {

	//instance of class
	private static RGBUtils instance = new RGBUtils();
	
	//rgb formatters
	private RGBFormatter[] formats;
	
	//gradient patterns
	private GradientPattern[] gradients;
	
	//TAB's RGB format
	private final Pattern tabPattern = Pattern.compile("#[0-9a-fA-F]{6}");
	private final Pattern tabPatternLegacy = Pattern.compile("#[0-9a-fA-F]{6}\\|.");

	public RGBUtils() {
		formats = new RGBFormatter[] {
				new BukkitFormat(),
				new CMIFormat(),
				new UnnamedFormat1(),
				new HtmlFormat()
		};
		gradients = new GradientPattern[] {
				//{#RRGGBB>}text{#RRGGBB<}
				new CommonGradient(Pattern.compile("\\{#[0-9a-fA-F]{6}>\\}[^\\{]*\\{#[0-9a-fA-F]{6}<\\}"), 
						Pattern.compile("\\{#[0-9a-fA-F]{6}\\|.>\\}[^\\{]*\\{#[0-9a-fA-F]{6}<\\}"), 
						"{#", 9, 2, 10, 8),
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
	 * @return instance
	 */
	public static RGBUtils getInstance() {
		return instance;
	}
	
	/**
	 * Applies all RGB formats and gradients to text and returns it
	 * @param text - original text
	 * @return text where everything is converted to #RRGGBB
	 */
	public String applyFormats(String text, boolean ignorePlaceholders) {
		String replaced = text;
		for (RGBFormatter formatter : formats) {
			replaced = formatter.reformat(replaced);
		}
		for (GradientPattern pattern : gradients) {
			replaced = pattern.applyPattern(replaced, ignorePlaceholders);
		}
		return replaced;
	}
	
	public String convertToBukkitFormat(String text, boolean rgbClient) {
		if (!text.contains("#")) return text; //no rgb codes
		if (rgbClient) {
			//converting random formats to TAB one
			String replaced = applyFormats(text, false);
			Matcher m = tabPatternLegacy.matcher(replaced);
			while (m.find()) {
				String hexcode = m.group();
				String fixed = "&x&" + hexcode.charAt(1) + "&" + hexcode.charAt(2) + "&" + hexcode.charAt(3) + "&" + hexcode.charAt(4) + "&" + hexcode.charAt(5) + "&" + hexcode.charAt(6);
				replaced = replaced.replace(hexcode, EnumChatFormat.color(fixed));
			}
			m = tabPattern.matcher(replaced);
			while (m.find()) {
				String hexcode = m.group();
				String fixed = "&x&" + hexcode.charAt(1) + "&" + hexcode.charAt(2) + "&" + hexcode.charAt(3) + "&" + hexcode.charAt(4) + "&" + hexcode.charAt(5) + "&" + hexcode.charAt(6);
				replaced = replaced.replace(hexcode, EnumChatFormat.color(fixed));
			}
			return replaced;
		} else {
			return IChatBaseComponent.fromColoredText(text).toLegacyText();
		}
	}

	/**
	 * Converts all hex codes in given string to legacy codes
	 * @param text - text to translate
	 * @return - translated text
	 */
	public String convertRGBtoLegacy(String text) {
		if (text == null) return null;
		if (!text.contains("#")) return EnumChatFormat.color(text);
		String applied = applyFormats(text, false);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < applied.length(); i++){
			char c = applied.charAt(i);
			if (c == '#') {
				try {
					if (containsLegacyCode(applied, i)) {
						sb.append(new TextColor(applied.substring(i, i+7), EnumChatFormat.getByChar(applied.charAt(i+8))).getLegacyColor().getFormat());
						i += 8;
					} else {
						sb.append(new TextColor(applied.substring(i, i+7)).getLegacyColor().getFormat());
						i += 6;
					}
				} catch (Exception e) {
					//not a valid RGB code
					sb.append(c);
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	/**
	 * Returns true if text contains legacy color request at defined RGB index start
	 * @param text - text to check
	 * @param i - current index start
	 * @return true if legacy color is defined, false if not
	 */
	private static boolean containsLegacyCode(String text, int i) {
		if (text.length() - i < 9 || text.charAt(i+7) != '|') return false;
		return EnumChatFormat.getByChar(text.charAt(i+8)) != null;
	}
}