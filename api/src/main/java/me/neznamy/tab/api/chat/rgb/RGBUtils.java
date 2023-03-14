package me.neznamy.tab.api.chat.rgb;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.chat.rgb.format.*;
import me.neznamy.tab.api.chat.rgb.gradient.CMIGradient;
import me.neznamy.tab.api.chat.rgb.gradient.CommonGradient;
import me.neznamy.tab.api.chat.rgb.gradient.GradientPattern;
import me.neznamy.tab.api.chat.rgb.gradient.KyoriGradient;
import me.neznamy.tab.api.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A helper class to reformat all RGB formats into the default #RRGGBB and apply gradients
 */
public class RGBUtils {

    /** Instance of the class */
    @Getter private static final RGBUtils instance = new RGBUtils();

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
        List<RGBFormatter> list = new ArrayList<>();
        if (ReflectionUtils.classExists("net.kyori.adventure.text.minimessage.MiniMessage")) {
            list.add(new MiniMessageFormat());
        }
        list.add(new BukkitFormat());
        list.add(new CMIFormat());
        list.add(new UnnamedFormat1());
        list.add(new HtmlFormat());
        list.add(new KyoriFormat());
        formats = list.toArray(new RGBFormatter[0]);

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
     * Applies all RGB formats and gradients to text and returns it.
     *
     * @param   text
     *          original text
     * @return  text where everything is converted to #RRGGBB
     */
    public String applyFormats(@NonNull String text) {
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
    public String applyCleanGradients(@NonNull String text) {
        String replaced = text;
        for (GradientPattern pattern : gradients) {
            replaced = pattern.applyPattern(replaced, true);
        }
        return replaced;
    }

    /**
     * Converts TAB's RGB format (#RRGGBB) into bukkit one
     * (&amp;x&amp;r&amp;r&amp;g&amp;g&amp;b&amp;b) for modern
     * clients (1.16+), for legacy clients it will use the closest color.
     *
     * @param   text
     *          text to convert
     * @param   rgbClient
     *          whether client accepts RGB or not
     * @return  converted text
     */
    public String convertToBukkitFormat(String text, boolean rgbClient) {
        if (text == null) return null;
        if (!text.contains("#")) return text; //no rgb codes
        if (rgbClient) {
            //converting random formats to TAB one
            String replaced = applyFormats(text);
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
     * Converts all hex codes in given string to legacy codes.
     * Also removes redundant color codes caused by this operation
     * to properly fit in limits.
     *
     * @param   text
     *          text to convert
     * @return  translated text
     */
    public String convertRGBtoLegacy(String text) {
        if (text == null) return null;
        return IChatBaseComponent.fromColoredText(text).toLegacyText();
    }

    /**
     * Returns true if entered string is a valid 6-digit combination of
     * hexadecimal numbers, false if not
     *
     * @param   string
     *          string to check
     * @return  {@code true} if valid, {@code false} if not
     */
    public boolean isHexCode(@NonNull String string) {
        if (string.length() != 6) return false;
        for (int i=0; i<6; i++) {
            char c = string.charAt(i);
            if (c < 48 || (c > 57 && c < 65) || (c > 70 && c < 97) || c > 102) return false;
        }
        return true;
    }
}