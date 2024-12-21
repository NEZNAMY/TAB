package me.neznamy.tab.shared.chat.rgb.format;

import org.jetbrains.annotations.NotNull;

/**
 * Formatter for {@code &#RRGGBB}.
 */
public class UnnamedFormat1 implements RGBFormatter {

    @Override
    public @NotNull String reformat(@NotNull String text) {
        return text.contains("§#") ? text.replace("§#", "#") : text;
    }
}