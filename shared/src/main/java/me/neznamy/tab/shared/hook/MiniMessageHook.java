package me.neznamy.tab.shared.hook;

import me.neznamy.tab.shared.chat.AdventureComponent;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class for hooking into MiniMessage to support its syntax.
 */
public class MiniMessageHook {

    /** Minimessage deserializer with disabled component post-processing */
    @Nullable
    private static final MiniMessage mm = ReflectionUtils.classExists("net.kyori.adventure.text.minimessage.MiniMessage") &&
            ReflectionUtils.classExists("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer") ?
            MiniMessage.builder().postProcessor(c->c).build() : null;

    /**
     * Attempts to parse the text into an adventure component using MiniMessage syntax. If MiniMessage is
     * not available or the text failed to parse for any reason, {@code null} is returned.
     *
     * @param   text
     *          Text to attempt to parse
     * @return  Parsed component or {@code null} if unable to parse
     */
    @Nullable
    public static TabComponent parseText(@NotNull String text) {
        if (mm == null) return null;
        if (!text.contains("<")) return null; // User did not even attempt to use MiniMessage
        if (text.contains("§")) return null;
        try {
            return new AdventureComponent(mm.deserialize(text));
        } catch (Throwable ignored) {
            return null;
        }
    }
}
