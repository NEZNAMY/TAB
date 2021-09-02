package me.neznamy.tab.api;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;

public abstract class TabFeature {

	private String featureName;
	protected String[] disabledServers = new String[0];
	private boolean serverWhitelistMode;
	protected String[] disabledWorlds = new String[0];
	private boolean worldWhitelistMode;
	protected Set<TabPlayer> disabledPlayers = new HashSet<>();
	
	private List<String> methodOverrides = new ArrayList<>();
	
	protected TabFeature(String featureName) {
		this.featureName = featureName;
		try {
			if (getClass().getMethod("onCommand", TabPlayer.class, String.class).getDeclaringClass() != TabFeature.class)
				methodOverrides.add("onCommand");
			if (getClass().getMethod("onJoin", TabPlayer.class).getDeclaringClass() != TabFeature.class)
				methodOverrides.add("onJoin");
			//quit contains something by default so always calling it
			if (getClass().getMethod("onWorldChange", TabPlayer.class, String.class, String.class).getDeclaringClass() != TabFeature.class)
				methodOverrides.add("onWorldChange");
			if (getClass().getMethod("onServerChange", TabPlayer.class, String.class, String.class).getDeclaringClass() != TabFeature.class)
				methodOverrides.add("onServerChange");
			if (getClass().getMethod("onDisplayObjective", TabPlayer.class, PacketPlayOutScoreboardDisplayObjective.class).getDeclaringClass() != TabFeature.class) 
				methodOverrides.add("onDisplayObjective");
			if (getClass().getMethod("onLoginPacket", TabPlayer.class).getDeclaringClass() != TabFeature.class) 
				methodOverrides.add("onLoginPacket");
			if (getClass().getMethod("onObjective", TabPlayer.class, PacketPlayOutScoreboardObjective.class).getDeclaringClass() != TabFeature.class)
				methodOverrides.add("onObjective");
			if (getClass().getMethod("onPlayerInfo", TabPlayer.class, PacketPlayOutPlayerInfo.class).getDeclaringClass() != TabFeature.class)
				methodOverrides.add("onPlayerInfo");
			if (getClass().getMethod("onPacketReceive", TabPlayer.class, Object.class).getDeclaringClass() != TabFeature.class)
				methodOverrides.add("onPacketReceive");
			if (getClass().getMethod("onPacketSend", TabPlayer.class, Object.class).getDeclaringClass() != TabFeature.class)
				methodOverrides.add("onPacketSend");
			if (getClass().getMethod("refresh", TabPlayer.class, boolean.class).getDeclaringClass() != TabFeature.class)
				methodOverrides.add("refresh");
		} catch (Exception e) {
			//this will never happen
		}
	}
	
	protected TabFeature(String featureName, List<String> disabledServers, List<String> disabledWorlds) {
		this(featureName);
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
	 * Loads all players and sends packets
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
	 * Processes command from player
	 * @param sender - command sender
	 * @param message - command line
	 * @return true if event should be cancelled, false if not
	 */
	public boolean onCommand(TabPlayer sender, String message) {
		return false;
	}
	
	/**
	 * Processes join event
	 * @param connectedPlayer - player who connected
	 */
	public void onJoin(TabPlayer connectedPlayer) {
		//empty by default
	}
	
	/**
	 * Processes quit event
	 * @param disconnectedPlayer - player who disconnected
	 */
	public void onQuit(TabPlayer disconnectedPlayer) {
		disabledPlayers.remove(disconnectedPlayer);
	}
	
	/**
	 * Processes world switch
	 * @param changed - player who switched world
	 * @param from - world player changed from
	 * @param to - world player changed to
	 */
	public void onWorldChange(TabPlayer changed, String from, String to) {
		//empty by default
	}
	
	/**
	 * Processes server switch
	 * @param changed - player who switched server
	 * @param from - server player changed from
	 * @param to - server player changed to
	 */
	public void onServerChange(TabPlayer changed, String from, String to) {
		onWorldChange(changed, null, null);
	}
	
	/**
	 * Processes the packet send and returns true if packet should be cancelled
	 * @param receiver - player receiving packet
	 * @param packet - received packet
	 * @return true if packet should be cancelled, false if not
	 */
	public boolean onDisplayObjective(TabPlayer receiver, PacketPlayOutScoreboardDisplayObjective packet) {
		return false;
	}
	
	/**
	 * Processes login packet, only available on bungeecord
	 * @param packetReceiver - player receiving client reset packet
	 */
	public void onLoginPacket(TabPlayer packetReceiver) {
		//empty by default
	}
	
	/**
	 * Processes the packet send
	 * @param receiver - player receiving packet
	 * @param packet - received packet
	 */
	public void onObjective(TabPlayer receiver, PacketPlayOutScoreboardObjective packet) {
		//empty by default
	}
	
	/**
	 * Processes the packet send and possibly modifies it
	 * @param receiver - player receiving packet
	 * @param packet - received packet
	 */
	public void onPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		//empty by default
	}
	
	/**
	 * Processes raw packet sent by client
	 * @param sender - packet sender
	 * @param packet - packet received
	 * @return true if false should be cancelled, false if not
	 * @throws IllegalAccessException
	 */
	public boolean onPacketReceive(TabPlayer sender, Object packet) throws IllegalAccessException {
		return false;
	}
	
	/**
	 * Processes raw packet sent to client
	 * @param receiver - packet receiver
	 * @param packet - the packet
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws InstantiationException 
	 */
	public void onPacketSend(TabPlayer receiver, Object packet) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
		//empty by default
	}

	/**
	 * Performs refresh of specified player
	 * @param refreshed - player to refresh
	 * @param force - if refresh should be forced despite refresh seemingly not needed
	 */
	public void refresh(TabPlayer refreshed, boolean force) {
		//empty by default
	}

	/**
	 * Registers this feature as one using specified placeholders
	 * @param placeholders - placeholders to add as used in this feature
	 */
	public void addUsedPlaceholders(Collection<String> placeholders) {
		if (placeholders.isEmpty()) return;
		placeholders.forEach(p -> TabAPI.getInstance().getPlaceholderManager().addUsedPlaceholder(p, this));
	}
	
	/**
	 * Returns true if world or server is disabled, false if not
	 * @param world - world to check
	 * @param server - server to check
	 * @return true if feature should be disabled, false if not
	 */
	public boolean isDisabled(String server, String world) {
		if (disabledWorlds != null) {
			boolean contains = contains(disabledWorlds, world);
			if (worldWhitelistMode) contains = !contains;
			if (contains) return true;
		}
		if (disabledServers != null) {
			boolean contains = contains(disabledServers, server);
			if (serverWhitelistMode) contains = !contains;
			if (contains) return true;
		}
		return false;
	}
	
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
	 * Returns name of the feature displayed in /tab cpu.
	 * @return name of the feature displayed in /tab cpu
	 */
	public String getFeatureName() {
		return featureName;
	}

	public boolean overridesMethod(String method) {
		return methodOverrides.contains(method);
	}

	public Set<TabPlayer> getDisabledPlayers() {
		return disabledPlayers;
	}
}