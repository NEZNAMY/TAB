package me.neznamy.tab.shared.config.helper;

import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.shared.TAB;
import me.neznamy.chat.component.SimpleTextComponent;
import me.neznamy.tab.shared.features.sorting.types.SortingType;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Logger for runtime errors from poor configuration
 * causing the plugin to not work as expected.
 */
public class RuntimeErrorPrinter {

    /**
     * Logs a warning if placeholder did not return a valid number for a bossbar property.
     *
     * @param   bossBar
     *          Bossbar where property is configured
     * @param   output
     *          Output after parsing placeholders in configured value
     * @param   configuredValue
     *          Value configured for the bossbar
     * @param   player
     *          Player who the parsing failed for
     * @param   property
     *          Name of used bossbar property
     * @param   expectation
     *          Expected values in the property
     */
    public void invalidBossBarProperty(@NotNull BossBar bossBar, @NotNull String output,
                                        @NotNull String configuredValue, @NotNull TabPlayer player,
                                        @NotNull String property, @NotNull String expectation) {
        // Placeholders are not initialized, because bridge did not respond yet (typically on join)
        if (player instanceof ProxyTabPlayer && !((ProxyTabPlayer)player).isBridgeConnected()) return;

        if (configuredValue.contains("%")) {
            error(String.format("Placeholder \"%s\" used in %s of BossBar \"%s\" returned \"%s\" for player %s, which cannot be evaluated to %s.",
                    configuredValue, property, bossBar.getName(), output, player.getName(), expectation));

        } else {
            error(String.format("BossBar \"%s\" has invalid input configured for %s (\"%s\"). Expecting a%s or a placeholder returning one.",
                    bossBar.getName(), property, configuredValue, expectation));
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
                placeholder, type.getDisplayName(), output, player.getName()));
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

    public void invalidNumberForBelowName(@NotNull TabPlayer target, @NotNull String configuredValue, @NotNull String output) {
        // Placeholders are not initialized, because bridge did not respond yet (typically on join)
        if (target instanceof ProxyTabPlayer && !((ProxyTabPlayer)target).isBridgeConnected()) return;

        error(String.format("Belowname value is configured to show \"%s\", but returned \"%s\" for player %s, which cannot be evaluated to a number.",
                configuredValue, output, target.getName()));
    }

    public void floatInBelowName(@NotNull TabPlayer target, @NotNull String configuredValue, @NotNull String output) {
        // Placeholders are not initialized, because bridge did not respond yet (typically on join)
        if (target instanceof ProxyTabPlayer && !((ProxyTabPlayer)target).isBridgeConnected()) return;

        error(String.format("Belowname value is configured to show \"%s\", but returned \"%s\" " +
                        "for player %s, which is a decimal number. Truncating to an integer.",
                configuredValue, output, target.getName()));
    }

    public void invalidNumberForPlayerlistObjective(@NotNull TabPlayer target, @NotNull String configuredValue, @NotNull String output) {
        // Placeholders are not initialized, because bridge did not respond yet (typically on join)
        if (target instanceof ProxyTabPlayer && !((ProxyTabPlayer)target).isBridgeConnected()) return;

        error(String.format("Playerlist objective value is configured to show \"%s\", but returned \"%s\" for player %s, which cannot be evaluated to a number.",
                configuredValue, output, target.getName()));
    }

    public void floatInPlayerlistObjective(@NotNull TabPlayer target, @NotNull String configuredValue, @NotNull String output) {
        // Placeholders are not initialized, because bridge did not respond yet (typically on join)
        if (target instanceof ProxyTabPlayer && !((ProxyTabPlayer)target).isBridgeConnected()) return;

        error(String.format("Playerlist objective value is configured to show \"%s\", but returned \"%s\" " +
                        "for player %s, which is a decimal number. Truncating to an integer.",
                configuredValue, output, target.getName()));
    }

    /**
     * Logs a warning if player's group is not in sorting list.
     *
     * @param   list
     *          Configured sorting list
     * @param   group
     *          Player's group
     * @param   player
     *          Player with the group
     */
    public void groupNotInSortingList(@NotNull Collection<String> list, @NotNull String group, @NotNull TabPlayer player) {
        // Ignore if groups are taken from bridge and it did not respond yet
        if (player instanceof ProxyTabPlayer && !((ProxyTabPlayer)player).isBridgeConnected()) return;

        error(String.format("Player %s's group (%s) is not in sorting list! Sorting list: %s. Player will be sorted on the bottom.",
                player.getName(), group, String.join(",", list)));
    }

    /**
     * Logs a warning if player does not have any of the defined sorting permissions.
     *
     * @param   list
     *          Configured permissions
     * @param   player
     *          Player with none of the permissions
     */
    public void noPermissionFromSortingList(@NotNull Collection<String> list, @NotNull TabPlayer player) {
        error(String.format("Player %s does not have any of the defined permissions in sorting list! Sorting list: %s. Player will be sorted on the bottom.",
                player.getName(), String.join(",", list)));
    }

    /**
     * Logs a warning if placeholder value is not in predefined values.
     *
     * @param   placeholder
     *          Configured sorting placeholder
     * @param   list
     *          Configured predefined values
     * @param   output
     *          Output of the placeholder
     * @param   player
     *          Player with the output
     */
    public void valueNotInPredefinedValues(@NotNull String placeholder, @NotNull Collection<String> list,
                                           @NotNull String output, @NotNull TabPlayer player) {
        error(String.format("Sorting placeholder %s with pre-defined values [%s] returned \"%s\" for player %s, " +
                        "which is not defined. Player will be sorted on the bottom.",
                placeholder, String.join(",", list), output, player.getName()));
    }

    /**
     * Logs a warning if MineSkin ID is invalid.
     *
     * @param   id
     *          MineSkin ID
     */
    public void unknownMineSkin(@NotNull String id) {
        error("Failed to load skin by id: No skin with the id '" + id + "' was found");
    }

    /**
     * Logs a warning if player with given name does not exist.
     *
     * @param   name
     *          Given player name
     */
    public void unknownPlayerSkin(@NotNull String name) {
        error("Failed to load skin by player: No user with the name '" + name + "' was found");
    }

    /**
     * Logs the message.
     *
     * @param   message
     *          Message to log
     */
    public void error(@NotNull String message) {
        TAB.getInstance().getPlatform().logWarn(new SimpleTextComponent(message.replace('§', '&')));
    }
}
