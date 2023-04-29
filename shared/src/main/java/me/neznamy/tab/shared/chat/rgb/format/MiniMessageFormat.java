package me.neznamy.tab.shared.chat.rgb.format;

import lombok.NonNull;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Call to MiniMessage API to reformat text to &amp;x&amp;R&amp;R&amp;G&amp;G&amp;B&amp;B
 */
public class MiniMessageFormat implements RGBFormatter {

    @Override
    public @NotNull String reformat(@NonNull String text) {
        if (!text.contains("<")) return text;
        try {
            return LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(text));
        } catch (Throwable ignored) {
            return text;
        }
    }
}