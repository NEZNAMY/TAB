package me.neznamy.tab.shared.features.scoreboard.lines;

import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * An interface representing a line in the scoreboard.
 */
public interface ScoreboardLine {

    /**
     * Registers the line for the specified player.
     *
     * @param   player
     *          the player for whom the line should be registered
     */
    void register(@NotNull TabPlayer player);

    /**
     * Refreshes the line for the specified player.
     *
     * @param   player
     *          the player for whom the line should be refreshed
     */
    void refresh(@NotNull TabPlayer player);

    /**
     * Unregisters the line for the specified player.
     *
     * @param   player
     *          the player for whom the line should be unregistered
     */
    void unregister(@NotNull TabPlayer player);

    /**
     * Returns last color codes used in provided text.
     *
     * @param   input
     *          text to get last colors from
     * @return  last colors used in provided text or empty string if nothing was found
     */
    @NotNull
    default String getLastColors(@NotNull String input) {
        StringBuilder result = new StringBuilder();
        int length = input.length();
        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if ((section == '§' || section == '&') && (index < length - 1)) {
                char c = input.charAt(index + 1);
                if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(String.valueOf(c))) {
                    result.insert(0, '§');
                    result.insert(1, c);
                    if ("0123456789AaBbCcDdEeFfRr".contains(String.valueOf(c))) {
                        break;
                    }
                }
            }
        }
        return result.toString();
    }
}
