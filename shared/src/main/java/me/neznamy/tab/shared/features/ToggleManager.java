package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class tracks toggling of a feature using commands when "remember-toggle-choice" is enabled
 * in the configuration of that specific feature.
 */
public class ToggleManager {

    /** Player data file to store toggle status in */
    @NotNull
    private final ConfigurationFile playerDataFile;

    /** Name of the section to save the toggled players list as in the player data file */
    @NotNull
    private final String sectionName;

    /** List of players who toggled the feature */
    @NotNull
    private final Set<String> toggledPlayers;

    /**
     * Constructs new instance with given parameters and loads list of toggled players from the file.
     *
     * @param   playerDataFile
     *          File where toggled players are saved
     * @param   sectionName
     *          Name of the section to save the toggled players list as in the player data file
     */
    public ToggleManager(@NotNull ConfigurationFile playerDataFile, @NotNull String sectionName) {
        this.playerDataFile = playerDataFile;
        this.sectionName = sectionName;
        toggledPlayers = new HashSet<>(playerDataFile.getStringList(sectionName, Collections.emptyList()));
    }

    /**
     * Checks for converting player to use UUID instead of name (from the old system).
     *
     * @param   player
     *          Player to check to convert
     */
    public void convert(@NotNull TabPlayer player) {
        if (toggledPlayers.remove(player.getName())) {
            toggledPlayers.add(player.getUniqueId().toString());
            save();
        }
    }

    /**
     * Returns {@code true} if the player has toggled this feature, {@code false} if not.
     *
     * @param   player
     *          Player to check for
     * @return  {@code true} if player has toggled the feature, {@code false} if not
     */
    public boolean contains(@NotNull TabPlayer player) {
        return toggledPlayers.contains(player.getUniqueId().toString());
    }

    /**
     * Adds player to list of toggled players.
     *
     * @param   player
     *          Player to add
     */
    public void add(@NotNull TabPlayer player) {
        if (toggledPlayers.add(player.getUniqueId().toString())) {
            save();
        }
    }

    /**
     * Removes player from the list of toggled players.
     *
     * @param   player
     *          Player to remove
     */
    public void remove(@NotNull TabPlayer player) {
        if (toggledPlayers.remove(player.getUniqueId().toString())) {
            save();
        }
    }

    /**
     * Saves list of toggled players into file.
     */
    private void save() {
        playerDataFile.set(sectionName, new ArrayList<>(toggledPlayers));
    }
}
