package me.neznamy.tab.shared.rgb;

import java.util.ArrayList;
import java.util.List;

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

	//list of rgb formatters
	private List<RGBFormatter> formats = new ArrayList<RGBFormatter>();
	
	//list of gradient patterns
	private List<GradientPattern> gradients = new ArrayList<GradientPattern>();

	public RGBUtils() {
		formats.add(new BukkitFormat());
		formats.add(new CMIFormat());
		formats.add(new UnnamedFormat1());
		formats.add(new UnnamedFormat2());
		
		gradients.add(new CMIGradient());
		gradients.add(new HtmlGradient());
		gradients.add(new IridescentGradient());
		gradients.add(new KyoriGradient());
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
}