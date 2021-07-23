package me.neznamy.tab.shared.features;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;

public abstract class TabFeature {

	private String featureName;
	protected List<String> disabledServers;
	protected List<String> disabledWorlds;
	protected Set<TabPlayer> disabledPlayers = new HashSet<>();
	
	protected TabFeature(String featureName) {
		this.featureName = featureName;
	}
	
	protected TabFeature(String featureName, List<String> disabledServers, List<String> disabledWorlds) {
		this.featureName = featureName;
		this.disabledServers = disabledServers;
		this.disabledWorlds = disabledWorlds;
	}
	
	/**
	 * Loads all players and sends packets
	 */
	public void load() {}
	
	/**
	 * Unloads all players and sends clear packets
	 */
	public void unload() {}
	
	/**
	 * Processes command from player
	 * @param sender - command sender
	 * @param message - command line
	 * @return true if event should be cancelled, false if not
	 */
	public boolean onCommand(TabPlayer sender, String message) {return false;}
	
	/**
	 * Processes join event
	 * @param connectedPlayer - player who connected
	 */
	public void onJoin(TabPlayer connectedPlayer) {}
	
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
	public void onWorldChange(TabPlayer changed, String from, String to) {}
	
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
	public boolean onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardDisplayObjective packet) {return false;}
	
	/**
	 * Processes login packet, only available on bungeecord
	 * @param packetReceiver - player receiving client reset packet
	 */
	public void onLoginPacket(TabPlayer packetReceiver) {}
	
	/**
	 * Processes the packet send
	 * @param receiver - player receiving packet
	 * @param packet - received packet
	 */
	public void onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardObjective packet) {}
	
	/**
	 * Processes the packet send and possibly modifies it
	 * @param receiver - player receiving packet
	 * @param packet - received packet
	 */
	public void onPacketSend(TabPlayer receiver, PacketPlayOutPlayerInfo info) {}
	
	/**
	 * Processes raw packet sent by client
	 * @param sender - packet sender
	 * @param packet - packet received
	 * @return modified packet or null if packet should be cancelled
	 * @throws IllegalAccessException 
	 * @throws ClassNotFoundException 
	 */
	public Object onPacketReceive(TabPlayer sender, Object packet) throws IllegalAccessException {return packet;}
	
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
	public void onPacketSend(TabPlayer receiver, Object packet) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {}

	/**
	 * Performs refresh of specified player
	 * @param refreshed - player to refresh
	 * @param force - if refresh should be forced despite refresh seemingly not needed
	 */
	public void refresh(TabPlayer refreshed, boolean force) {}

	/**
	 * Registers this feature as one using specified placeholders
	 * @param placeholders - placeholders to add as used in this feature
	 */
	public void addUsedPlaceholders(Collection<String> placeholders) {
		placeholders.forEach(p -> TAB.getInstance().getPlaceholderManager().getPlaceholderUsage().computeIfAbsent(p, x -> new HashSet<>()).add(this));
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
			if (disabledWorlds.contains("WHITELIST")) contains = !contains;
			if (contains) return true;
		}
		if (disabledServers != null) {
			boolean contains = contains(disabledServers, server);
			if (disabledServers.contains("WHITELIST")) contains = !contains;
			if (contains) return true;
		}
		return false;
	}
	
	protected boolean contains(List<String> list, String element) {
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
}