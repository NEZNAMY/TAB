package me.neznamy.tab.shared.chat.rgb.format;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Formatter for &amp;#RRGGBB
 */
public class UnnamedFormat1 implements RGBFormatter {

    @Override
    public @NotNull String reformat(@NonNull String text) {
        return text.contains("&#") ? text.replace("&#", "#") : text;
    }
}