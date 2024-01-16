package me.neznamy.tab.shared.chat.rgb.format;

import me.neznamy.tab.shared.chat.EnumChatFormat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Call to MiniMessage API to reformat text to &amp;x&amp;R&amp;R&amp;G&amp;G&amp;B&amp;B
 */
public class MiniMessageFormat implements RGBFormatter {

    /** Serializer that uses &x format on all platforms, even those that do not have it enabled by default */
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors().useUnusualXRepeatedCharacterHexFormat().build();

    @Override
    @NotNull
    public String reformat(@NotNull String text) {
        if (!text.contains("<")) return text; // User did not even attempt to use MiniMessage
        if (text.contains(EnumChatFormat.COLOR_STRING)) return text;
        try {
            return SERIALIZER.serialize(MiniMessage.miniMessage().deserialize(text));
        } catch (Throwable ignored) {
            return text;
        }
    }
}