package me.neznamy.tab.shared.util.cache;

import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TextColor;
import me.neznamy.tab.shared.chat.component.SimpleTextComponent;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.hook.MiniMessageHook;
import me.neznamy.tab.shared.util.function.TriFunction;
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
                for (EnumChatFormat format : EnumChatFormat.VALUES) {
                    if (mmFormatted.contains(format.getFormat())) {
                        String colorName = format == EnumChatFormat.UNDERLINE ? "underlined" : format.name().toLowerCase(Locale.US);
                        mmFormatted = mmFormatted.replace(format.getFormat(), "<" + colorName + ">");
                    }
                }

                // Convert TAB's #RRGGBB to Kyori <color:#RRGGBB>
                mmFormatted = tabToKyori(mmFormatted);

                TabComponent component = MiniMessageHook.parseText(mmFormatted);
                if (component != null) return component;
            }
            return text.contains("#") || text.contains("§x") || text.contains("<") ?
                    TabComponent.fromColoredText(text) : //contains RGB colors or font
                    new SimpleTextComponent(text); //no RGB
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
