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

    /** Dummy character to append to disable MiniMessage's color compacting that prevents team color from being detected as last color of prefix */
    private static final char dummyChar = Character.MAX_VALUE;

    @Override
    @NotNull
    public String reformat(@NotNull String text) {
        if (!text.contains("<")) return text; // User did not even attempt to use MiniMessage
        if (text.contains(EnumChatFormat.COLOR_STRING)) return text;
        try {
            String serialized = SERIALIZER.serialize(MiniMessage.miniMessage().deserialize(text + dummyChar));
            return serialized.substring(0, serialized.length()-1); // Remove the dummy char back
        } catch (Throwable ignored) {
            return text;
        }
    }
}