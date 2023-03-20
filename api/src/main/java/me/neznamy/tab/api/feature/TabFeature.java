package me.neznamy.tab.api.feature;

import java.util.*;

import lombok.NonNull;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;

/**
 * Abstract class representing a core feature of the plugin.
 */
public abstract class TabFeature {

    /** Servers where the feature is disabled (or enabled if using whitelist mode) */
    protected String[] disabledServers = new String[0];

    /** Flag tracking whether disabled server list is a whitelist or blacklist */
    private boolean serverWhitelistMode = false;

    /** Worlds where the feature is disabled (or enabled if using whitelist mode) */
    protected String[] disabledWorlds = new String[0];

    /** Flag tracking whether disabled world list is a whitelist or blacklist */
    private boolean worldWhitelistMode = false;

    /** Players located in currently disabled worlds / servers */
    private final Set<TabPlayer> disabledPlayers = Collections.newSetFromMap(new WeakHashMap<>());

    protected TabFeature() {}

    /**
     * Constructs new instance with given parameters and loads method overrides.
     * Also loads lists of disabled worlds and servers of this feature with config
     * section path specified with {@code configSection} parameter.
     *
     * @param   configSection
     *          Configuration section of the feature to load disabled
     *          servers / worlds from
     */
    protected TabFeature(@NonNull String configSection) {
        List<String> disabledServers = TabAPI.getInstance().getConfig().getStringList(configSection + ".disable-in-servers");
        List<String> disabledWorlds = TabAPI.getInstance().getConfig().getStringList(configSection + ".disable-in-worlds");
        if (disabledServers != null) {
            this.disabledServers = disabledServers.toArray(new String[0]);
            serverWhitelistMode = disabledServers.contains("WHITELIST");
        }
        if (disabledWorlds != null) {
            this.disabledWorlds = disabledWorlds.toArray(new String[0]);
            worldWhitelistMode = disabledWorlds.contains("WHITELIST");
        }
    }

    /**
     * Returns {@code true} if world or server is disabled, {@code false} if not
     *
     * @param   server
     *          server to check
     * @param   world
     *          world to check
     * @return  {@code true} if feature should be disabled, {@code false} if not
     */
    public boolean isDisabled(String server, String world) {
        boolean contains = contains(disabledWorlds, world);
        if (worldWhitelistMode) contains = !contains;
        if (contains) return true;
        contains = contains(disabledServers, server);
        if (serverWhitelistMode) contains = !contains;
        return contains;
    }

    /**
     * Returns {@code true} if list contains the specified element or element
     * ends with {@code "*"} and element meeting that requirement is present,
     * {@code false} otherwise.
     *
     * @param   list
     *          List to check
     * @param   element
     *          Element to find
     * @return  {@code true} if element was found, {@code false} if not
     */
    protected boolean contains(String[] list, String element) {
        if (element == null) return false;
        for (String s : list) {
            if (s.endsWith("*")) {
                if (element.toLowerCase().startsWith(s.substring(0, s.length()-1).toLowerCase())) return true;
            } else {
                if (element.equalsIgnoreCase(s)) return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if player is currently located in a server
     * or world, which is marked as disabled, {@code false} if not
     *
     * @param   p
     *          Player to check
     * @return  {@code true} if player is in disabled server / world,
     *          {@code false} if not
     */
    public boolean isDisabledPlayer(TabPlayer p) {
        return disabledPlayers.contains(p);
    }

    /**
     * Adds specified player into list of players in disabled
     * servers / worlds.
     *
     * @param   p
     *          Player to add
     */
    public void addDisabledPlayer(TabPlayer p) {
        if (disabledPlayers.contains(p)) return;
        disabledPlayers.add(p);
    }

    /**
     * Removes specified player from list of players in disabled
     * servers / worlds
     *
     * @param   p
     *          Player to check
     */
    public void removeDisabledPlayer(TabPlayer p) {
        disabledPlayers.remove(p);
    }

    /**
     * Returns name of this feature displayed in /tab cpu
     *
     * @return  name of this feature display in /tab cpu
     */
    public abstract String getFeatureName();
}