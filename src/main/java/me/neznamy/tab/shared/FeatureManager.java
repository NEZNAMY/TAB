package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.NameTag;
import me.neznamy.tab.shared.features.interfaces.CommandListener;
import me.neznamy.tab.shared.features.interfaces.Feature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.LoginPacketListener;
import me.neznamy.tab.shared.features.interfaces.PlayerInfoPacketListener;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.RawPacketFeature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.features.interfaces.RespawnEventListener;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

/**
 * Feature registration which offers calls to features and measures how long it took them to process
 */
public class FeatureManager {

	//list of registered features
	private Map<String, Feature> features = new LinkedHashMap<String, Feature>();
	
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
		return features.values();
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
	 * @throws Exception 
	 */
	public Object onPacketPlayOutPlayerInfo(TabPlayer receiver, Object packet) throws Exception {
		List<PlayerInfoPacketListener> listeners = new ArrayList<PlayerInfoPacketListener>();
		for (Feature f : getAllFeatures()) {
			if (f instanceof PlayerInfoPacketListener) listeners.add((PlayerInfoPacketListener) f);
		}
		//not deserializing & serializing when there is not anyone to listen to the packet
		if (listeners.isEmpty()) return packet;
		
		long time = System.nanoTime();
		PacketPlayOutPlayerInfo info = UniversalPacketPlayOut.builder.readPlayerInfo(packet, receiver.getVersion());
		Shared.cpu.addTime(TabFeature.PACKET_DESERIALIZING, UsageType.PACKET_PLAYER_INFO, System.nanoTime()-time);
		for (PlayerInfoPacketListener f : listeners) {
			time = System.nanoTime();
			((PlayerInfoPacketListener)f).onPacketSend(receiver, info);
			Shared.cpu.addTime(f.getFeatureType(), UsageType.PACKET_READING, System.nanoTime()-time);
		}
		time = System.nanoTime();
		Object pack = info.create(receiver.getVersion());
		Shared.cpu.addTime(TabFeature.PACKET_SERIALIZING, UsageType.PACKET_PLAYER_INFO, System.nanoTime()-time);
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
			Shared.cpu.addTime(f.getFeatureType(), UsageType.PLAYER_QUIT_EVENT, System.nanoTime()-time);
		}
		Shared.data.remove(disconnectedPlayer.getUniqueId());
	}
	
	/**
	 * Calls onJoin(...) on all features that implement JoinEventListener and measures how long it took them to process
	 * 
	 * @param connectedPlayer - player who connected
	 */
	public void onJoin(TabPlayer connectedPlayer) {
		Shared.data.put(connectedPlayer.getUniqueId(), connectedPlayer);
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof JoinEventListener)) continue;
			long time = System.nanoTime();
			((JoinEventListener)f).onJoin(connectedPlayer);
			Shared.cpu.addTime(f.getFeatureType(), UsageType.PLAYER_JOIN_EVENT, System.nanoTime()-time);
		}
		connectedPlayer.markAsLoaded();
	}
	
	/**
	 * Calls onWorldChange(...) on all features that implement WorldChangeListener and measures how long it took them to process
	 * 
	 * @param changed - player who switched world (or server on proxy)
	 * @param from - name of the previous world/server
	 * @param to - name of the new world/server
	 */
	public void onWorldChange(TabPlayer changed, String to) {
		if (changed == null || !changed.isLoaded()) return;
		String from = changed.getWorldName();
		changed.setWorldName(to);
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof WorldChangeListener)) continue;
			long time = System.nanoTime();
			((WorldChangeListener)f).onWorldChange(changed, from, to);
			Shared.cpu.addTime(f.getFeatureType(), UsageType.WORLD_SWITCH_EVENT, System.nanoTime()-time);
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
			Shared.cpu.addTime(f.getFeatureType(), UsageType.COMMAND_PREPROCESS, System.nanoTime()-time);
		}
		return cancel;
	}
	
	/**
	 * Calls onPacketReceive(...) on all features that implement RawPacketFeature and measures how long it took them to process
	 * 
	 * @param receiver - packet receiver
	 * @param packet - IN packet coming from player
	 * @return altered packet or null if packet should be cancelled
	 */
	public Object onPacketReceive(TabPlayer receiver, Object packet){
		Object newPacket = packet;
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof RawPacketFeature)) continue;
			long time = System.nanoTime();
			try {
				if (newPacket != null) newPacket = ((RawPacketFeature)f).onPacketReceive(receiver, newPacket);
			} catch (Throwable e) {
				Shared.errorManager.printError("Feature " + f.getFeatureType() + " failed to read packet", e);
			}
			Shared.cpu.addTime(f.getFeatureType(), UsageType.PACKET_READING, System.nanoTime()-time);
		}
		return newPacket;
	}
	
	/**
	 * Calls onPacketSend(...) on all features that implement RawPacketFeature and measures how long it took them to process
	 * 
	 * @param receiver - packet receiver
	 * @param packet - OUT packet coming from the server
	 */
	public void onPacketSend(TabPlayer receiver, Object packet){
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof RawPacketFeature)) continue;
			long time = System.nanoTime();
			try {
				((RawPacketFeature)f).onPacketSend(receiver, packet);
			} catch (Throwable e) {
				Shared.errorManager.printError("Feature " + f.getFeatureType() + " failed to read packet", e);
			}
			Shared.cpu.addTime(f.getFeatureType(), UsageType.PACKET_READING, System.nanoTime()-time);
		}
	}
	
	/**
	 * Calls onRespawn on all featurs that implement RespawnEventListener and measures how long it took them to process
	 * @param respawned - player who respawned
	 */
	public void onRespawn(TabPlayer respawned) {
		if (respawned == null) return;
		for (Feature f : getAllFeatures()) {
			if (!(f instanceof RespawnEventListener)) continue;
			long time = System.nanoTime();
			((RespawnEventListener)f).onRespawn(respawned);
			Shared.cpu.addTime(f.getFeatureType(), UsageType.PLAYER_RESPAWN_EVENT, System.nanoTime()-time);
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
			Shared.cpu.addTime(f.getFeatureType(), UsageType.PACKET_LOGIN, System.nanoTime()-time);
		}
	}
	
	public NameTag getNameTagFeature() {
		if (isFeatureEnabled("nametag16")) return (NameTag) getFeature("nametag16");
		return (NameTag) getFeature("nametagx");
	}
}