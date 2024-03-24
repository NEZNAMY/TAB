package me.neznamy.tab.shared.chat.rgb.format;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formatter for &lt;#RRGGBB&gt;
 */
public class KyoriFormat implements RGBFormatter {

    private final Pattern pattern = Pattern.compile("<#[0-9a-fA-F]{6}>");

    @Override
    public @NotNull String reformat(@NotNull String text) {
        if (!text.contains("<#")) return text;
        String replaced = text;
        Matcher m = pattern.matcher(replaced);
        while (m.find()) {
            String hexCode = m.group();
            String fixed = hexCode.substring(2, 8);
            replaced = replaced.replace(hexCode, "#" + fixed);
        }
        return replaced;
    }
}