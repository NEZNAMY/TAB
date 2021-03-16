package me.neznamy.tab.shared.rgb;

import java.util.HashSet;
import java.util.Set;

import me.neznamy.tab.shared.rgb.format.BukkitFormat;
import me.neznamy.tab.shared.rgb.format.CMIFormat;
import me.neznamy.tab.shared.rgb.format.RGBFormatter;
import me.neznamy.tab.shared.rgb.format.UnnamedFormat1;
import me.neznamy.tab.shared.rgb.format.UnnamedFormat2;
import me.neznamy.tab.shared.rgb.gradient.CMIGradient;
import me.neznamy.tab.shared.rgb.gradient.GradientPattern;
import me.neznamy.tab.shared.rgb.gradient.HtmlGradient;
import me.neznamy.tab.shared.rgb.gradient.IridescentGradient;
import me.neznamy.tab.shared.rgb.gradient.KyoriGradient;

/**
 * A helper class to reformat all RGB formats into the default #RRGGBB and apply gradients
 */
public class RGBUtils {

	//instance of class
	private static RGBUtils instance = new RGBUtils();
	
	//list of rgb formatters
	private Set<RGBFormatter> formats = new HashSet<RGBFormatter>();
	
	//list of gradient patterns
	private Set<GradientPattern> gradients = new HashSet<GradientPattern>();

	public RGBUtils() {
		registerRGBFormatter(new BukkitFormat());
		registerRGBFormatter(new CMIFormat());
		registerRGBFormatter(new UnnamedFormat1());
		registerRGBFormatter(new UnnamedFormat2());
		
		registerGradient(new CMIGradient());
		registerGradient(new HtmlGradient());
		registerGradient(new IridescentGradient());
		registerGradient(new KyoriGradient());
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
	public String applyFormats(String text) {
		String replaced = text;
		for (RGBFormatter formatter : formats) {
			replaced = formatter.reformat(replaced);
		}
		for (GradientPattern pattern : gradients) {
			replaced = pattern.applyPattern(replaced);
		}
		return replaced;
	}
	
	/**
	 * Registers RGB formatter
	 * @param formatter - formatter to register
	 */
	public void registerRGBFormatter(RGBFormatter formatter) {
		formats.add(formatter);
	}
	
	/**
	 * Registers gradient pattern
	 * @param pattern - gradient pattern to register
	 */
	public void registerGradient(GradientPattern pattern) {
		gradients.add(pattern);
	}
}