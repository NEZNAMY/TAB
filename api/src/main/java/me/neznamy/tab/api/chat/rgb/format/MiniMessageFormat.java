package me.neznamy.tab.api.chat.rgb.format;

import me.neznamy.tab.api.chat.EnumChatFormat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Formatter for &lt;#RRGGBB>
 */
public class MiniMessageFormat implements RGBFormatter {

    @Override
    public String reformat(String text) {
        if (!text.contains("<")) return text;
        String format = text.replace(EnumChatFormat.RESET.getFormat(), ""); //remove &r from header/footer newline inserts
        try {
            return LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(format));
        } catch (Exception ignored) {
            return text;
        }
    }
}