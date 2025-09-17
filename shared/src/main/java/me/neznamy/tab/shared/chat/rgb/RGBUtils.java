package me.neznamy.tab.shared.chat.rgb;

import lombok.Getter;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.rgb.format.BukkitFormat;
import me.neznamy.tab.shared.chat.rgb.format.CommonFormatter;
import me.neznamy.tab.shared.chat.rgb.format.RGBFormatter;
import me.neznamy.tab.shared.chat.rgb.gradient.CMIGradient;
import me.neznamy.tab.shared.chat.rgb.gradient.CommonGradient;
import me.neznamy.tab.shared.chat.rgb.gradient.GradientPattern;
import me.neznamy.tab.shared.chat.rgb.gradient.NexEngineGradient;
import me.neznamy.tab.shared.util.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * A helper class to reformat all RGB formats into the default #RRGGBB and apply gradients
 */
public class RGBUtils {

    /** Instance of the class */
    @Getter private static final RGBUtils instance = new RGBUtils();

    /** Registered RGB formatters */
    private final RGBFormatter[] formats = {
            new BukkitFormat(),   // &x&r&r&g&g&b&b
            new CommonFormatter(Pattern.compile("\\{#[0-9a-fA-F]{6}}"), "{#"),  // CMI's {#RRGGBB}
            new CommonFormatter(Pattern.compile("§#[0-9a-fA-F]{6}"), "§#"),     // &#RRGGBB
            new CommonFormatter(Pattern.compile("#<[0-9a-fA-F]{6}>"), "#<")     // #<RRGGBB> // "HTML"
    };

    /** Registered gradient patterns */
    private final GradientPattern[] gradients = {
            new CMIGradient(), //{#RRGGBB>}Text{#RRGGBB<}
            new CommonGradient(Pattern.compile("<#[0-9a-fA-F]{6}>(?:(?!<#[0-9a-fA-F]{6}>).)*?</#[0-9a-fA-F]{6}>"), "<#", 2, 9, 7), //<#RRGGBB>Text</#RRGGBB> // "HTML"
            new CommonGradient(Pattern.compile("<\\$#[0-9a-fA-F]{6}>.*?<\\$#[0-9a-fA-F]{6}>"), "<$", 3, 10, 7), //<$#RRGGBB>Text<$#RRGGBB> // ?
            new NexEngineGradient() // <gradient:#RRGGBB>Text</gradient:#RRGGBB>
    };

    /**
     * Applies all gradient formats to text and returns it.
     *
     * @param   text
     *          original text
     * @param   gradientFunction
     *          Function for reformatting gradient to new text
     * @param   rgbFunction
     *          Function for converting RGB codes to desired format
     * @return  text where all gradients are converted to desired format
     */
    @NotNull
    public String applyFormats(@NotNull String text, @NotNull TriFunction<TabTextColor, String, TabTextColor, String> gradientFunction,
                               @NotNull Function<TabTextColor, String> rgbFunction) {
        String replaced = text;
        for (GradientPattern pattern : gradients) {
            replaced = pattern.applyPattern(replaced, gradientFunction);
        }
        for (RGBFormatter formatter : formats) {
            replaced = formatter.reformat(replaced, rgbFunction);
        }
        return replaced;
    }
}