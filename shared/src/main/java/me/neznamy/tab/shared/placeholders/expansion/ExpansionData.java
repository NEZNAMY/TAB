package me.neznamy.tab.shared.placeholders.expansion;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import me.neznamy.tab.shared.proxy.message.outgoing.ExpansionPlaceholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Class holding data for TAB's PlaceholderAPI expansion for fast access.
 */
@RequiredArgsConstructor
public class ExpansionData {

    /** Player to whom this data belongs */
    @NotNull
    private final TabPlayer player;

    /** Map of placeholder identifiers to their values */
    @NotNull
    private final Map<String, String> data = new HashMap<>();

    /**
     * Sets scoreboard visibility placeholder to specified value.
     *
     * @param   visible
     *          Whether scoreboard is visible or not
     */
    public void setScoreboardVisible(boolean visible) {
        setValue("scoreboard_visible", visible ? "Enabled" : "Disabled");
    }

    /**
     * Sets scoreboard name placeholder to specified value.
     *
     * @param   name
     *          Name of currently visible scoreboard
     */
    public void setScoreboardName(@NotNull String name) {
        setValue("scoreboard_name", name);
    }

    /**
     * Sets bossbar visibility placeholder to specified value.
     *
     * @param   visible
     *          Whether bossbar is visible or not
     */
    public void setBossBarVisible(boolean visible) {
        setValue("bossbar_visible", visible ? "Enabled" : "Disabled");
    }

    /**
     * Sets nametag visibility toggle choice placeholder to specified value.
     *
     * @param   visible
     *          Whether player wants to see nametags or not
     */
    public void setNameTagVisibility(boolean visible) {
        setValue("nametag_visibility", visible ? "Enabled" : "Disabled");
    }

    /**
     * Sets internal placeholder value.
     *
     * @param   placeholder
     *          Identifier of internal placeholder
     * @param   value
     *          Placeholder value
     */
    public void setPlaceholderValue(@NotNull String placeholder, @NotNull String value) {
        setValue("placeholder_" + placeholder.substring(1, placeholder.length()-1), value);
    }

    /**
     * Sets property value placeholder to specified placeholder.
     *
     * @param   property
     *          Name of property
     * @param   value
     *          Value of property
     */
    public void setPropertyValue(@NotNull String property, @NotNull String value) {
        setValue(property, value);
    }

    /**
     * Sets raw property value placeholder to specified placeholder.
     *
     * @param   property
     *          Name of property
     * @param   value
     *          Raw value of property
     */
    public void setRawPropertyValue(@NotNull String property, @NotNull String value) {
        setValue(property + "_raw", value);
    }

    /**
     * Sets expansion placeholder to specified value.
     *
     * @param   key
     *          Name of placeholder
     * @param   value
     *          Placeholder value
     */
    private void setValue(@NotNull String key, @NotNull String value) {
        data.put(key, value);
        if (player instanceof ProxyTabPlayer) {
            ((ProxyTabPlayer)player).sendPluginMessage(new ExpansionPlaceholder(key, value));
        }
    }

    /**
     * Gets expansion placeholder value. If no value is set, returns null.
     *
     * @param   key
     *          Name of placeholder
     * @return  Placeholder value
     */
    @Nullable
    public String getValue(@NotNull String key) {
        return data.get(key);
    }

    /**
     * Resends all values to the player, typically on server switch on proxy.
     */
    public void resendAllValues() {
        if (player instanceof ProxyTabPlayer) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                ((ProxyTabPlayer)player).sendPluginMessage(new ExpansionPlaceholder(entry.getKey(), entry.getValue()));
            }
        }
    }
}
