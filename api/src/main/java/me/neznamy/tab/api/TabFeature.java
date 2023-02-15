package me.neznamy.tab.api;

import java.util.*;

import lombok.Getter;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;

/**
 * Abstract class representing a core feature of the plugin.
 * <p>
 * It receives all kinds of events and can react to them.
 */
public abstract class TabFeature {

    /** Feature's name displayed in /tab cpu */
    @Getter private final String featureName;

    /** Feature's function name displayed in place of refreshing in /tab cpu */
    @Getter private final String refreshDisplayName;

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

    /**
     * List of methods overridden (implemented) by each feature.
     * If a method is not overridden, it is not called at all.
     * This avoids a massive spam for every single method
     * in every single feature in /tab cpu output
     */
    private final List<String> methodOverrides = new ArrayList<>();

    /**
     * Constructs new instance with given parameters and loads method overrides
     *
     * @param   featureName
     *          Feature's name in /tab cpu
     * @param   refreshDisplayName
     *          "refreshing" cpu display type name of the feature
     */
    protected TabFeature(String featureName, String refreshDisplayName) {
        this.featureName = featureName;
        this.refreshDisplayName = refreshDisplayName;
        try {
            if (getClass().getMethod("onCommand", TabPlayer.class, String.class).getDeclaringClass() != TabFeature.class)
                methodOverrides.add("onCommand");
            if (getClass().getMethod("onJoin", TabPlayer.class).getDeclaringClass() != TabFeature.class)
                methodOverrides.add("onJoin");
            if (getClass().getMethod("onQuit", TabPlayer.class).getDeclaringClass() != TabFeature.class)
                methodOverrides.add("onQuit");
            if (getClass().getMethod("onWorldChange", TabPlayer.class, String.class, String.class).getDeclaringClass() != TabFeature.class)
                methodOverrides.add("onWorldChange");
            if (getClass().getMethod("onServerChange", TabPlayer.class, String.class, String.class).getDeclaringClass() != TabFeature.class)
                methodOverrides.add("onServerChange");
            if (getClass().getMethod("onDisplayObjective", TabPlayer.class, PacketPlayOutScoreboardDisplayObjective.class).getDeclaringClass() != TabFeature.class) {
                methodOverrides.add("onDisplayObjective");
                TabAPI.getInstance().getFeatureManager().markDisplayObjective();
            }
            if (getClass().getMethod("onLoginPacket", TabPlayer.class).getDeclaringClass() != TabFeature.class)
                methodOverrides.add("onLoginPacket");
            if (getClass().getMethod("onObjective", TabPlayer.class, PacketPlayOutScoreboardObjective.class).getDeclaringClass() != TabFeature.class) {
                methodOverrides.add("onObjective");
                TabAPI.getInstance().getFeatureManager().markObjective();
            }
            if (getClass().getMethod("onPlayerInfo", TabPlayer.class, PacketPlayOutPlayerInfo.class).getDeclaringClass() != TabFeature.class)
                methodOverrides.add("onPlayerInfo");
            if (getClass().getMethod("onPacketReceive", TabPlayer.class, Object.class).getDeclaringClass() != TabFeature.class)
                methodOverrides.add("onPacketReceive");
            if (getClass().getMethod("onPacketSend", TabPlayer.class, Object.class).getDeclaringClass() != TabFeature.class)
                methodOverrides.add("onPacketSend");
            if (getClass().getMethod("refresh", TabPlayer.class, boolean.class).getDeclaringClass() != TabFeature.class)
                methodOverrides.add("refresh");
            if (getClass().getMethod("onVanishStatusChange", TabPlayer.class).getDeclaringClass() != TabFeature.class)
                methodOverrides.add("onVanishStatusChange");
        } catch (NoSuchMethodException e) {
            //this will never happen
        }
    }

    /**
     * Constructs new instance with given parameters and loads method overrides.
     * Also loads lists of disabled worlds and servers of this feature with config
     * section path specified with {@code configSection} parameter.
     *
     * @param   featureName
     *          Feature's name in /tab cpu
     * @param   refreshDisplayName
     *          "refreshing" cpu display type name of the feature
     * @param   configSection
     *          Configuration section of the feature to load disabled
     *          servers / worlds from
     */
    protected TabFeature(String featureName, String refreshDisplayName, String configSection) {
        this(featureName, refreshDisplayName);
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
     * Loads all online players and sends packets
     */
    public void load() {
        //empty by default
    }

    /**
     * Unloads all players and sends clear packets
     */
    public void unload() {
        //empty by default
    }

    /**
     * Processes command from player. This is typically a toggle command.
     *
     * @param   sender
     *          command sender
     * @param   message
     *          command line
     * @return  {@code true} if event should be cancelled, {@code true} if not
     */
    public boolean onCommand(TabPlayer sender, String message) {
        return false;
    }

    /**
     * Processes join event
     *
     * @param   connectedPlayer
     *          player who connected
     */
    public void onJoin(TabPlayer connectedPlayer) {
        //empty by default
    }

    /**
     * Processes quit event
     *
     * @param   disconnectedPlayer
     *          player who disconnected
     */
    public void onQuit(TabPlayer disconnectedPlayer) {
        //empty by default
    }

    /**
     * Processes world switch
     *
     * @param   changed
     *          player who switched world
     * @param   from
     *          world player changed from
     * @param   to
     *          world player changed to
     */
    public void onWorldChange(TabPlayer changed, String from, String to) {
        //empty by default
    }

    /**
     * Processes server switch
     *
     * @param   changed
     *          player who switched server
     * @param   from
     *          server player changed from
     * @param   to
     *          server player changed to
     */
    public void onServerChange(TabPlayer changed, String from, String to) {
        //empty by default
    }

    /**
     * Processes the packet send and returns true if packet should be cancelled
     *
     * @param   receiver
     *          player receiving packet
     * @param   packet
     *          received packet
     */
    public void onDisplayObjective(TabPlayer receiver, PacketPlayOutScoreboardDisplayObjective packet) {
        //empty by default
    }

    /**
     * Processes login packet, only available on BungeeCord
     *
     * @param   packetReceiver
     *          player receiving client reset packet
     */
    public void onLoginPacket(TabPlayer packetReceiver) {
        //empty by default
    }

    /**
     * Processes the packet send
     *
     * @param   receiver
     *          player receiving packet
     * @param   packet
     *          received packet
     */
    public void onObjective(TabPlayer receiver, PacketPlayOutScoreboardObjective packet) {
        //empty by default
    }

    /**
     * Processes the packet send and possibly modifies it
     *
     * @param   receiver
     *          player receiving packet
     * @param   info
     *          received packet
     */
    public void onPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
        //empty by default
    }

    /**
     * Processes raw packet sent by client
     *
     * @param   sender
     *          packet sender
     * @param   packet
     *          packet received
     * @return  true if false should be cancelled, false if not
     * @throws  ReflectiveOperationException
     *          if thrown by reflective operation
     */
    public boolean onPacketReceive(TabPlayer sender, Object packet) throws ReflectiveOperationException {
        return false;
    }

    /**
     * Processes raw packet sent to client
     *
     * @param   receiver
     *          packet receiver
     * @param   packet
     *          the packet
     * @throws  ReflectiveOperationException
     *          if thrown by reflective operation
     */
    public void onPacketSend(TabPlayer receiver, Object packet) throws ReflectiveOperationException {
        //empty by default
    }

    /**
     * Performs refresh of specified player
     *
     * @param   refreshed
     *          player to refresh
     * @param   force
     *          if refresh should be forced despite refresh seemingly not needed
     */
    public void refresh(TabPlayer refreshed, boolean force) {
        //empty by default
    }

    /**
     * Processes vanish status change of player
     *
     * @param   player
     *          Player who changed vanish status
     */
    public void onVanishStatusChange(TabPlayer player) {
        //empty by default
    }

    /**
     * Registers this feature as one using specified placeholders
     *
     * @param   placeholders
     *          placeholders to add as used in this feature
     */
    public void addUsedPlaceholders(Collection<String> placeholders) {
        if (placeholders.isEmpty()) return;
        placeholders.forEach(p -> TabAPI.getInstance().getPlaceholderManager().addUsedPlaceholder(p, this));
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
     * Returns {@code true} if method with specified name is overridden
     * in the implementation, {@code false} if not
     *
     * @param   method
     *          Method to check
     * @return  {@code true} if overridden, {@code false} if not
     */
    public boolean overridesMethod(String method) {
        return methodOverrides.contains(method);
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
}