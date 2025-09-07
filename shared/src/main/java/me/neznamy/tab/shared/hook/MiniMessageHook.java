package me.neznamy.tab.shared.hook;

import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.hook.AdventureHook;
import me.neznamy.tab.shared.TAB;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class for hooking into MiniMessage to support its syntax.
 */
public class MiniMessageHook {

    /** Minimessage deserializer with disabled component post-processing */
    @Nullable
    private static final MiniMessage mm = createMiniMessage();

    @Nullable
    private static MiniMessage createMiniMessage() {
        try {
            return MiniMessage.builder().postProcessor(c -> c).build();
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns {@code true} if MiniMessage is available on the server, {@code false} if not.
     *
     * @return  {@code true} if MiniMessage is available on the server, {@code false} if not
     */
    public static boolean isAvailable() {
        return mm != null && TAB.getInstance().getConfiguration().getConfig().getComponents().isMinimessageSupport();
    }

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
        try {
            return AdventureHook.convert(mm.deserialize(text));
        } catch (Throwable t) {
            TAB.getInstance().getErrorManager().printError("Failed to convert \"" + text + "\" into a MiniMessage component", t);
            return null;
        }
    }
}
