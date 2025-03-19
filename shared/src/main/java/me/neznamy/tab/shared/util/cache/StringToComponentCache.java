package me.neznamy.tab.shared.util.cache;

import me.neznamy.chat.TextColor;
import me.neznamy.chat.component.SimpleTextComponent;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.chat.rgb.RGBUtils;
import me.neznamy.chat.util.TriFunction;
import me.neznamy.tab.shared.hook.MiniMessageHook;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cache for String → TabComponent conversion.
 */
public class StringToComponentCache extends Cache<String, TabComponent> {

    /** Formatter to use Kyori's &lt;gradient:#RRGGBB:#RRGGBB>Text&lt;/gradient> */
    private static final TriFunction<TextColor, String, TextColor, String> kyoriGradientFormatter =
            (start, text, end) -> String.format("<gradient:#%s:#%s>%s</gradient>", start.getHexCode(), end.getHexCode(), text);

    /** Formatter to convert RGB code to use Kyori's &lt;color:#RRGGBB>*/
    private static final Function<TextColor, String> kyoriRGBFormatter = color -> String.format("<color:#%s>", color.getHexCode());

    private static final Pattern tabToKyori = Pattern.compile("(?<!:)(#([0-9A-Fa-f]{6}))(?![:>])");

    /**
     * Constructs new instance with given parameters.
     *
     * @param   name
     *          Cache name
     * @param   cacheSize
     *          Size limit of the cache
     */
    public StringToComponentCache(String name, int cacheSize) {
        super(name, cacheSize, text -> {
            if (MiniMessageHook.isAvailable() && text.indexOf('<') != -1) { // User may have wanted to use MiniMessage

                // Reformat gradients and RGB to kyori format
                String mmFormatted = RGBUtils.getInstance().applyFormats(text, kyoriGradientFormatter, kyoriRGBFormatter);

                // Convert legacy codes into kyori format
                for (TextColor format : TextColor.LEGACY_COLORS.values()) {
                    String sequence = "§" + format.getLegacyColor().getCharacter();
                    if (mmFormatted.contains(sequence)) {
                        String colorName = format == TextColor.UNDERLINE ? "underlined" : format.getLegacyColor().name().toLowerCase(Locale.US);
                        mmFormatted = mmFormatted.replace(sequence, "<" + colorName + ">");
                    }
                }

                // Convert TAB's #RRGGBB to Kyori <color:#RRGGBB>
                mmFormatted = tabToKyori(mmFormatted);

                TabComponent component = MiniMessageHook.parseText(mmFormatted);
                if (component != null) return component;
            }
            return text.contains("#") || text.contains("§x") || text.contains("<") ?
                    TabComponent.fromColoredText(text) : //contains RGB colors or font
                    SimpleTextComponent.text(text); //no RGB
        });
    }

    @NotNull
    private static String tabToKyori(@NotNull String text) {
        Matcher matcher = tabToKyori.matcher(text);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            // Replace standalone #RRGGBB with <color:#RRGGBB>
            matcher.appendReplacement(result, "<color:" + matcher.group(1) + ">");
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
