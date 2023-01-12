package me.neznamy.tab.api.chat.rgb.format;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Call to MiniMessage API to reformat text to &amp;x&amp;R&amp;R&amp;G&amp;G&amp;B&amp;B
 */
public class MiniMessageFormat implements RGBFormatter {

    @Override
    public String reformat(String text) {
        if (!text.contains("<")) return text;
        try {
            return LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(text));
        } catch (Throwable ignored) {
            return text;
        }
    }
}