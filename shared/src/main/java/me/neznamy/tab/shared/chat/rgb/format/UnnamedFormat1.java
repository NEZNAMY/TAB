package me.neznamy.tab.shared.chat.rgb.format;

import me.neznamy.tab.shared.chat.EnumChatFormat;
import org.jetbrains.annotations.NotNull;

/**
 * Formatter for {@code &#RRGGBB}.
 */
public class UnnamedFormat1 implements RGBFormatter {

    private final String identifier = EnumChatFormat.COLOR_STRING + "#";

    @Override
    public @NotNull String reformat(@NotNull String text) {
        return text.contains(identifier) ? text.replace(identifier, "#") : text;
    }
}