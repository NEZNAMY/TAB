package me.neznamy.tab.shared.hook;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
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

    /** Flag tracking whether MiniMessage is available on the server or not */
    @Getter
    private static final boolean available = ReflectionUtils.classExists("net.kyori.adventure.text.minimessage.MiniMessage") &&
            ReflectionUtils.classExists("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");

    /** Minimessage deserializer with disabled component post-processing */
    @Nullable
    private static final MiniMessage mm = available ? MiniMessage.builder().postProcessor(c->c).build() : null;

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
        if (text.contains("§")) {
            TAB.getInstance().getErrorManager().printError("Cannot convert \"" + text + "\" into a MiniMessage component, because it contains legacy colors", null);
            return null;
        }
        try {
            return new AdventureComponent(mm.deserialize(text));
        } catch (Throwable t) {
            TAB.getInstance().getErrorManager().printError("Failed to convert \"" + text + "\" into a MiniMessage component", t);
            return null;
        }
    }
}
