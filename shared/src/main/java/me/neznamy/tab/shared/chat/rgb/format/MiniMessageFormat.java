package me.neznamy.tab.shared.chat.rgb.format;

import me.neznamy.tab.shared.chat.EnumChatFormat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Call to MiniMessage API to reformat text to &amp;x&amp;R&amp;R&amp;G&amp;G&amp;B&amp;B
 */
public class MiniMessageFormat implements RGBFormatter {

    @Override
    public @NotNull String reformat(@NotNull String text) {
        if (!text.contains("<")) return text; // User did not even attempt to use MiniMessage
        String modified = text;
        // Scoreboard uses &f at the beginning to prevent unwanted colors, may as well translate them all at this point
        for (EnumChatFormat format : EnumChatFormat.VALUES) {
            if (modified.contains(format.getFormat())) {
                modified = modified.replace(format.getFormat(), "<" + format.name().toLowerCase(Locale.US) + ">");
            }
        }
        try {
            return LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(modified));
        } catch (Throwable ignored) {
            return text;
        }
    }
}