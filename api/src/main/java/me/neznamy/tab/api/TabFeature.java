package me.neznamy.tab.api;

import java.util.*;

import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;

public abstract class TabFeature {

	private final String featureName;
	private final String refreshDisplayName;
	protected final String[] disabledServers;
	private final boolean serverWhitelistMode;
	protected final String[] disabledWorlds;
	private final boolean worldWhitelistMode;
	private final Set<TabPlayer> disabledPlayers = Collections.newSetFromMap(new WeakHashMap<>());
	private final List<String> methodOverrides = new ArrayList<>();
	
	protected TabFeature(String featureName, String refreshDisplayName) {
		this(featureName, refreshDisplayName, null, null);
	}
	
	protected TabFeature(String featureName, String refreshDisplayName, List<String> disabledServers, List<String> disabledWorlds) {
		this.featureName = featureName;
		this.refreshDisplayName = refreshDisplayName;
		if (disabledServers != null) {
			this.disabledServers = disabledServers.toArray(new String[0]);
			serverWhitelistMode = disabledServers.contains("WHITELIST");
		} else {
			this.disabledServers = new String[0];
			serverWhitelistMode = false;
		}
		if (disabledWorlds != null) {
			this.disabledWorlds = disabledWorlds.toArray(new String[0]);
			worldWhitelistMode = disabledWorlds.contains("WHITELIST");
		} else {
			this.disabledWorlds = new String[0];
			worldWhitelistMode = false;
		}
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
		} catch (NoSuchMethodException e) {
			//this will never happen
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
		//empty by default
	}
	
	/**
	 * Processes the packet send and returns true if packet should be cancelled
	 * @param receiver - player receiving packet
	 * @param packet - received packet
	 */
	public void onDisplayObjective(TabPlayer receiver, PacketPlayOutScoreboardDisplayObjective packet) {
		//empty by default
	}
	
	/**
	 * Processes login packet, only available on BungeeCord
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
	 * @param info - received packet
	 */
	public void onPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		//empty by default
	}
	
	/**
	 * Processes raw packet sent by client
	 * @param sender - packet sender
	 * @param packet - packet received
	 * @return true if false should be cancelled, false if not
	 * @throws	ReflectiveOperationException
	 * 			if thrown by reflective operation
	 */
	public boolean onPacketReceive(TabPlayer sender, Object packet) throws ReflectiveOperationException {
		return false;
	}
	
	/**
	 * Processes raw packet sent to client
	 * @param receiver - packet receiver
	 * @param packet - the packet
	 * @throws	ReflectiveOperationException
	 * 			if thrown by reflective operation
	 */
	public void onPacketSend(TabPlayer receiver, Object packet) throws ReflectiveOperationException {
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
		boolean contains = contains(disabledWorlds, world);
		if (worldWhitelistMode) contains = !contains;
		if (contains) return true;
		contains = contains(disabledServers, server);
		if (serverWhitelistMode) contains = !contains;
		return contains;
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

	public boolean isDisabledPlayer(TabPlayer p) {
		return disabledPlayers.contains(p);
	}
	
	public void addDisabledPlayer(TabPlayer p) {
		if (disabledPlayers.contains(p)) return;
		disabledPlayers.add(p);
	}
	
	public void removeDisabledPlayer(TabPlayer p) {
		disabledPlayers.remove(p);
	}

	public String getRefreshDisplayName() {
		return refreshDisplayName;
	}
}