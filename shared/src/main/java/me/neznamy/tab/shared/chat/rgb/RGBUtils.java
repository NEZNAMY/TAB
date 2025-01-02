package me.neznamy.tab.shared.chat.rgb;

import lombok.Getter;
import me.neznamy.tab.shared.chat.rgb.format.*;
import me.neznamy.tab.shared.chat.rgb.gradient.CMIGradient;
import me.neznamy.tab.shared.chat.rgb.gradient.CommonGradient;
import me.neznamy.tab.shared.chat.rgb.gradient.GradientPattern;
import me.neznamy.tab.shared.chat.rgb.gradient.NexEngineGradient;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * A helper class to reformat all RGB formats into the default #RRGGBB and apply gradients
 */
public class RGBUtils {

    /** Instance of the class */
    @Getter private static final RGBUtils instance = new RGBUtils();

    /** Registered RGB formatters */
    private final RGBFormatter[]  formats = {
            new BukkitFormat(),
            new CMIFormat(),
            new UnnamedFormat1(),
            new HtmlFormat(),
            new KyoriFormat()
    };

    /** Registered gradient patterns */
    private final GradientPattern[] gradients = {
            new CMIGradient(), //{#RRGGBB>}Text{#RRGGBB<}
            new CommonGradient(Pattern.compile("<#[0-9a-fA-F]{6}>.*?</#[0-9a-fA-F]{6}>"), "<#", 2, 9, 7), //<#RRGGBB>Text</#RRGGBB>
            new CommonGradient(Pattern.compile("<\\$#[0-9a-fA-F]{6}>.*?<\\$#[0-9a-fA-F]{6}>"), "<$", 3, 10, 7), //<$#RRGGBB>Text<$#RRGGBB>
            new NexEngineGradient() // <gradient:#RRGGBB>Text</gradient:#RRGGBB>
    };

    /**
     * Applies all RGB formats and gradients to text and returns it.
     *
     * @param   text
     *          original text
     * @return  text where everything is converted to #RRGGBB
     */
    public @NotNull String applyFormats(@NotNull String text) {
        String replaced = text;
        for (GradientPattern pattern : gradients) {
            replaced = pattern.applyPattern(replaced, false);
        }
        for (RGBFormatter formatter : formats) {
            replaced = formatter.reformat(replaced);
        }
        return replaced;
    }

    /**
     * Applies all gradient formats to text and returns it. This only affects
     * usage where no placeholder is used inside.
     *
     * @param   text
     *          original text
     * @return  text where all gradients with static text are converted to #RRGGBB
     */
    public @NotNull String applyCleanGradients(@NotNull String text) {
        String replaced = text;
        for (GradientPattern pattern : gradients) {
            replaced = pattern.applyPattern(replaced, true);
        }
        return replaced;
    }
}