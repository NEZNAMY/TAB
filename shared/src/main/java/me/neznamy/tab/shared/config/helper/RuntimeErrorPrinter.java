package me.neznamy.tab.shared.config.helper;

import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.sorting.types.SortingType;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Logger for runtime errors from poor configuration
 * causing the plugin to not work as expected.
 */
public class RuntimeErrorPrinter {

    /**
     * Logs a warning if placeholder did not return a valid number for bossbar progress.
     *
     * @param   bossBar
     *          Bossbar where progress is configured
     * @param   output
     *          Output after parsing placeholders in configured value
     * @param   configuredValue
     *          Value configured for the bossbar
     * @param   player
     *          Player who the parsing failed for
     */
    public void invalidNumberForBossBarProgress(@NotNull BossBar bossBar, @NotNull String output,
                                                @NotNull String configuredValue, @NotNull TabPlayer player) {
        // Placeholders are not initialized, because bridge did not respond yet (typically on join)
        if (player instanceof ProxyTabPlayer && !((ProxyTabPlayer)player).isBridgeConnected()) return;

        if (configuredValue.contains("%")) {
            error(String.format("Placeholder \"%s\" used in progress of BossBar \"%s\" returned \"%s\" for player %s, " +
                            "which cannot be evaluated to a number between 0 and 100.",
                    configuredValue, bossBar.getName(), output, player.getName()));

        } else {
            error(String.format("BossBar \"%s\" has invalid input configured for progress (\"%s\"). " +
                            "Expecting a number between 0 and 100 or a placeholder returning one.",
                    bossBar.getName(), configuredValue));
        }
    }

    /**
     * Logs a warning if numeric sorting received input that is not a valid number.
     *
     * @param   type
     *          Numeric sorting type where value is used
     * @param   placeholder
     *          Configured sorting placeholder
     * @param   output
     *          Output returned by the placeholder
     * @param   player
     *          Player the placeholder returned output for
     */
    public void invalidInputForNumericSorting(@NotNull SortingType type, @NotNull String placeholder,
                                              @NotNull String output, @NotNull TabPlayer player) {
        // Placeholders are not initialized, because bridge did not respond yet (typically on join)
        if (player instanceof ProxyTabPlayer && !((ProxyTabPlayer)player).isBridgeConnected()) return;

        error(String.format("Placeholder %s used in sorting type %s returned \"%s\" for player %s, which is not a valid number.",
                placeholder, type, output, player.getName()));
    }

    /**
     * Logs a warning if placeholder for numeric condition did not return a number.
     *
     * @param   placeholder
     *          Configured placeholder
     * @param   output
     *          Output returned by the placeholder
     * @param   player
     *          Player the output was received for
     */
    public void invalidNumberForCondition(@NotNull String placeholder, @NotNull String output, @NotNull TabPlayer player) {
        // Placeholders are not initialized, because bridge did not respond yet (typically on join)
        if (player instanceof ProxyTabPlayer && !((ProxyTabPlayer)player).isBridgeConnected()) return;

        error(String.format("Placeholder %s used in a numeric condition returned \"%s\" for player %s, which is not a valid number.",
                placeholder, output, player.getName()));
    }

    /**
     * Logs the message.
     *
     * @param   message
     *          Message to log
     */
    private void error(@NotNull String message) {
        TAB.getInstance().getPlatform().logWarn(IChatBaseComponent.fromColoredText(message));
    }
}
