package me.neznamy.tab.shared.config.helper;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Class for printing hints for a cleaner configuration
 * with the same effect, typically due to redundancy.
 */
public class HintPrinter {

    /**
     * Prints a hint saying layout already includes prevent-spectator-effect feature.
     */
    public void layoutIncludesPreventSpectatorEffect() {
        hint("Layout feature automatically includes prevent-spectator-effect, therefore the feature can be disabled " +
                "for better performance, as it is not needed at all (assuming it is configured to always display some layout).");
    }

    /**
     * Logs the message with "Hint" prefix.
     *
     * @param   message
     *          Message to log
     */
    public void hint(@NotNull String message) {
        TAB.getInstance().getPlatform().logInfo(TabComponent.fromColoredText(EnumChatFormat.GOLD + "[Hint] " + message));
    }
}
