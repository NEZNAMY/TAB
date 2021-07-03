package me.neznamy.tab.shared;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.NameTag;
import me.neznamy.tab.shared.features.types.Feature;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.event.CommandListener;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.features.types.event.QuitEventListener;
import me.neznamy.tab.shared.features.types.event.WorldChangeListener;
import me.neznamy.tab.shared.features.types.packet.DisplayObjectivePacketListener;
import me.neznamy.tab.shared.features.types.packet.LoginPacketListener;
import me.neznamy.tab.shared.features.types.packet.ObjectivePacketListener;
import me.neznamy.tab.shared.features.types.packet.PlayerInfoPacketListener;
import me.neznamy.tab.shared.features.types.packet.RawPacketListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;

/**
 * Feature registration which offers calls to features and measures how long it took them to process
 */
public class FeatureManager {

	//list of registered features
	private Map<String, Feature> features = new LinkedHashMap<>();
	
	//tab instance
	private TAB tab;
	
	/**
	 * Constructs new instance
	 * @param tab - tab instance
	 */
	public FeatureManager(TAB tab) {
		this.tab = tab;
	}
	
	/**
	 * Registers a feature
	 * 
	 * @param featureName - name of feature
	 * @param featureHandler - the handler
	 */
	public void registerFeature(String featureName, Feature featureHandler) {
		features.put(featureName, featureHandler);
	}
	
	/**
	 * Unregisters feature making it no longer receive events. This does not run unload method nor cancel
	 * tasks created by the feature
	 * @param featureName
	 */
	public void unregisterFeature(String featureName) {
		features.remove(featureName);
	}
	
	/**
	 * Returns whether a feature with said name is registered or not
	 * 
	 * @param name - name of feature defined in registerFeature method
	 * @return true if feature exists, false if not
	 */
	public boolean isFeatureEnabled(String name) {
		return features.containsKey(name);
	}
	
	/**
	 * Returns feature handler by it's name
	 * 
	 * @param name - name of feature defined in registerFeature method
	 * @return the feature or null if feature does not exist
	 */
	public Feature getFeature(String name) {
		return features.get(name);
	}
	
	/**
	 * Returns list of all loaded features
	 * @return list of all loaded features
	 */
	public Collection<Feature> getAllFeatures(){
		try {
			return features.values();
		} catch (ConcurrentModificationException e) {
			return getAllFeatures();
		}
	}
	
	/**
	 * Calls load() on all features that implement Loadable
	 * This function is called on plugin startup
	 */
	public void load() {
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof Loadable)) continue;
			((Loadable)f).load();
		}
	}
	
	/**
	 * Calls unload() on all features that implement Loadable
	 * This function is called on plugin unload
	 */
	public void unload() {
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof Loadable)) continue;
			((Loadable)f).unload();
		}
	}
	
	/**
	 * Calls refresh(...) on all features that implement Refreshable
	 * 
	 * @param refreshed - player to be refreshed
	 * @param force - whether refresh should be forced or not
	 */
	public void refresh(TabPlayer refreshed, boolean force) {
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof Refreshable)) continue;
			((Refreshable)f).refresh(refreshed, force);
		}
	}
	
	/**
	 * Calls refreshUsedPlaceholders() on all features that implement Refreshable
	 * This function is called when new placeholders enter the game (usually when a command to assign property is ran)
	 */
	public void refreshUsedPlaceholders() {
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof Refreshable)) continue;
			((Refreshable)f).refreshUsedPlaceholders();
		}
	}
	
	/**
	 * Calls onPacketSend(...) on all features that implement PlayerInfoPacketListener and measures how long it took them to process
	 * 
	 * @param receiver - packet receiver
	 * @param packet - an instance of custom packet class PacketPlayOutPlayerInfo
	 * @return altered packet or null if packet should be cancelled
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public Object onPacketPlayOutPlayerInfo(TabPlayer receiver, Object packet) throws IllegalAccessException, InvocationTargetException {
		if (receiver.getVersion().getMinorVersion() < 8) return packet;
		List<PlayerInfoPacketListener> listeners = new ArrayList<>();
		for (Feature f : getAllFeatures()) {
			if (f instanceof PlayerInfoPacketListener) listeners.add((PlayerInfoPacketListener) f);
		}
		//not deserializing & serializing when there is not anyone to listen to the packet
		if (listeners.isEmpty()) return packet;
		
		long time = System.nanoTime();
		PacketPlayOutPlayerInfo info = tab.getPacketBuilder().readPlayerInfo(packet, receiver.getVersion());
		tab.getCPUManager().addTime(TabFeature.PACKET_DESERIALIZING, UsageType.PACKET_PLAYER_INFO, System.nanoTime()-time);
		for (PlayerInfoPacketListener f : listeners) {
			time = System.nanoTime();
			f.onPacketSend(receiver, info);
			tab.getCPUManager().addTime(f.getFeatureType(), UsageType.PACKET_READING_OUT, System.nanoTime()-time);
		}
		time = System.nanoTime();
		Object pack = info.create(receiver.getVersion());
		tab.getCPUManager().addTime(TabFeature.PACKET_SERIALIZING, UsageType.PACKET_PLAYER_INFO, System.nanoTime()-time);
		return pack;
	}
	
	/**
	 * Calls onQuit(...) on all features that implement QuitEventListener and measures how long it took them to process
	 * 
	 * @param disconnectedPlayer - player who disconnected
	 */
	public void onQuit(TabPlayer disconnectedPlayer) {
		if (disconnectedPlayer == null) return;
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof QuitEventListener)) continue;
			long time = System.nanoTime();
			((QuitEventListener)f).onQuit(disconnectedPlayer);
			tab.getCPUManager().addTime(f.getFeatureType(), UsageType.PLAYER_QUIT_EVENT, System.nanoTime()-time);
		}
		tab.removePlayer(disconnectedPlayer);
	}
	
	/**
	 * Calls onJoin(...) on all features that implement JoinEventListener and measures how long it took them to process
	 * 
	 * @param connectedPlayer - player who connected
	 */
	public void onJoin(TabPlayer connectedPlayer) {
		if (!connectedPlayer.isOnline()) {
			tab.debug("Player " + connectedPlayer.getName() + " was offline during login process.");
			return;
		}
		long millis = System.currentTimeMillis();
		tab.addPlayer(connectedPlayer);
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof JoinEventListener)) continue;
			long time = System.nanoTime();
			((JoinEventListener)f).onJoin(connectedPlayer);
			tab.getCPUManager().addTime(f.getFeatureType(), UsageType.PLAYER_JOIN_EVENT, System.nanoTime()-time);
		}
		((ITabPlayer)connectedPlayer).markAsLoaded();
		tab.debug("Player join of " + connectedPlayer.getName() + " processed in " + (System.currentTimeMillis()-millis) + "ms");
	}
	
	/**
	 * Calls onWorldChange(...) on all features that implement WorldChangeListener and measures how long it took them to process
	 * 
	 * @param changed - player who switched world (or server on proxy)
	 * @param from - name of the previous world/server
	 * @param to - name of the new world/server
	 */
	public void onWorldChange(UUID playerUUID, String to) {
		TabPlayer changed = TAB.getInstance().getPlayer(playerUUID);
		if (changed == null || !changed.isLoaded()) {
			tab.getCPUManager().runTaskLater(100, "processing delayed world/server switch", TabFeature.OTHER, UsageType.WORLD_SWITCH_EVENT, () -> onWorldChange(playerUUID, to));
			return;
		}
		String from = changed.getWorldName();
		((ITabPlayer)changed).setWorldName(to);
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof WorldChangeListener)) continue;
			long time = System.nanoTime();
			((WorldChangeListener)f).onWorldChange(changed, from, to);
			tab.getCPUManager().addTime(f.getFeatureType(), UsageType.WORLD_SWITCH_EVENT, System.nanoTime()-time);
		}
	}
	
	/**
	 * Calls onCommand(...) on all features that implement CommandListener and measures how long it took them to process
	 * 
	 * @param sender - command sender
	 * @param command - command line including /
	 * @return true if command should be cancelled, false if not
	 */
	public boolean onCommand(TabPlayer sender, String command) {
		if (sender == null) return false;
		boolean cancel = false;
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof CommandListener)) continue;
			long time = System.nanoTime();
			if (((CommandListener)f).onCommand(sender, command)) cancel = true;
			tab.getCPUManager().addTime(f.getFeatureType(), UsageType.COMMAND_PREPROCESS, System.nanoTime()-time);
		}
		return cancel;
	}
	
	/**
	 * Calls onPacketReceive(...) on all features that implement RawPacketListener and measures how long it took them to process
	 * 
	 * @param receiver - packet receiver
	 * @param packet - IN packet coming from player
	 * @return altered packet or null if packet should be cancelled
	 */
	public Object onPacketReceive(TabPlayer receiver, Object packet){
		Object newPacket = packet;
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof RawPacketListener)) continue;
			long time = System.nanoTime();
			try {
				if (newPacket != null) newPacket = ((RawPacketListener)f).onPacketReceive(receiver, newPacket);
			} catch (IllegalAccessException e) {
				tab.getErrorManager().printError("Feature " + f.getFeatureType() + " failed to read packet", e);
			}
			tab.getCPUManager().addTime(f.getFeatureType(), UsageType.PACKET_READING_IN, System.nanoTime()-time);
		}
		return newPacket;
	}
	
	/**
	 * Calls onPacketSend(...) on all features that implement RawPacketListener and measures how long it took them to process
	 * 
	 * @param receiver - packet receiver
	 * @param packet - OUT packet coming from the server
	 */
	public void onPacketSend(TabPlayer receiver, Object packet){
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof RawPacketListener)) continue;
			long time = System.nanoTime();
			try {
				((RawPacketListener)f).onPacketSend(receiver, packet);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException e) {
				tab.getErrorManager().printError("Feature " + f.getFeatureType() + " failed to read packet", e);
			}
			tab.getCPUManager().addTime(f.getFeatureType(), UsageType.PACKET_READING_OUT, System.nanoTime()-time);
		}
	}
	
	/**
	 * Calls onLoginPacket on all featurs that implement LoginPacketListener and measures how long it took them to process
	 * @param packetReceiver - player who received the packet
	 */
	public void onLoginPacket(TabPlayer packetReceiver) {
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof LoginPacketListener)) continue;
			long time = System.nanoTime();
			((LoginPacketListener)f).onLoginPacket(packetReceiver);
			tab.getCPUManager().addTime(f.getFeatureType(), UsageType.PACKET_LOGIN, System.nanoTime()-time);
		}
	}
	
	/**
	 * Calls onPacketSend on all featurs that implement DisplayObjectivePacketListener and measures how long it took them to process
	 * @param packetReceiver - player who received the packet
	 * @param packet - the packet
	 * @return true if packet should be cancelled, false if not
	 * @throws IllegalAccessException 
	 */
	public boolean onDisplayObjective(TabPlayer packetReceiver, Object packet) throws IllegalAccessException {
		long time = System.nanoTime();
		PacketPlayOutScoreboardDisplayObjective display = tab.getPacketBuilder().readDisplayObjective(packet, packetReceiver.getVersion());
		tab.getCPUManager().addTime(TabFeature.PACKET_DESERIALIZING, UsageType.PACKET_DISPLAY_OBJECTIVE, System.nanoTime()-time);
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof DisplayObjectivePacketListener)) continue;
			time = System.nanoTime();
			boolean cancel = ((DisplayObjectivePacketListener)f).onPacketSend(packetReceiver, display);
			tab.getCPUManager().addTime(f.getFeatureType(), UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
			if (cancel) return true;
		}
		return false;
	}
	
	/**
	 * Calls onObjective on all featurs that implement ObjectivePacketListener and measures how long it took them to process
	 * @param packetReceiver - player who received the packet
	 * @throws IllegalAccessException 
	 */
	public void onObjective(TabPlayer packetReceiver, Object packet) throws IllegalAccessException {
		long time = System.nanoTime();
		PacketPlayOutScoreboardObjective display = tab.getPacketBuilder().readObjective(packet, packetReceiver.getVersion());
		tab.getCPUManager().addTime(TabFeature.PACKET_DESERIALIZING, UsageType.PACKET_OBJECTIVE, System.nanoTime()-time);
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof ObjectivePacketListener)) continue;
			time = System.nanoTime();
			((ObjectivePacketListener)f).onPacketSend(packetReceiver, display);
			tab.getCPUManager().addTime(f.getFeatureType(), UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
		}
	}
	
	/**
	 * Returns currently loaded nametag feature or null if disabled
	 * @return currently loaded nametag feature
	 */
	public NameTag getNameTagFeature() {
		if (isFeatureEnabled("nametag16")) return (NameTag) getFeature("nametag16");
		return (NameTag) getFeature("nametagx");
	}
}