package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.NameTag;
import me.neznamy.tab.shared.features.interfaces.CommandListener;
import me.neznamy.tab.shared.features.interfaces.Feature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.PlayerInfoPacketListener;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.RawPacketFeature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;

/**
 * Feature registration which offers calls to features and measures how long it took them to process
 */
public class FeatureManager {

	//all features, sometimes we need to get feature by it's name
	private Map<String, Feature> features = new ConcurrentHashMap<String, Feature>();
	
	//lists of feature types to use on forEach in methods
	private List<PlayerInfoPacketListener> playerInfoListeners = new ArrayList<PlayerInfoPacketListener>();
	private List<RawPacketFeature> rawpacketfeatures = new ArrayList<RawPacketFeature>();
	private List<Loadable> loadableFeatures = new ArrayList<Loadable>();
	private List<JoinEventListener> joinListeners = new ArrayList<JoinEventListener>();
	private List<QuitEventListener> quitListeners = new ArrayList<QuitEventListener>();
	private List<WorldChangeListener> worldChangeListeners = new ArrayList<WorldChangeListener>();
	private List<CommandListener> commandListeners = new ArrayList<CommandListener>();
	public List<Refreshable> refreshables = new ArrayList<Refreshable>();
	
	/**
	 * Registers a feature by adding it to the core map as well as all lists where applicable
	 * 
	 * @param featureName - name of feature
	 * @param featureHandler - the handler
	 */
	public void registerFeature(String featureName, Feature featureHandler) {
		features.put(featureName, featureHandler);
		if (featureHandler instanceof Loadable) {
			loadableFeatures.add((Loadable) featureHandler);
		}
		if (featureHandler instanceof PlayerInfoPacketListener) {
			playerInfoListeners.add((PlayerInfoPacketListener) featureHandler);
		}
		if (featureHandler instanceof RawPacketFeature) {
			rawpacketfeatures.add((RawPacketFeature) featureHandler);
		}
		if (featureHandler instanceof JoinEventListener) {
			joinListeners.add((JoinEventListener) featureHandler);
		}
		if (featureHandler instanceof QuitEventListener) {
			quitListeners.add((QuitEventListener) featureHandler);
		}
		if (featureHandler instanceof WorldChangeListener) {
			worldChangeListeners.add((WorldChangeListener) featureHandler);
		}
		if (featureHandler instanceof CommandListener) {
			commandListeners.add((CommandListener) featureHandler);
		}
		if (featureHandler instanceof Refreshable) {
			refreshables.add((Refreshable) featureHandler);
		}
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
	 * Calls load() on all features that implement Loadable
	 * This function is called on plugin startup
	 */
	public void load() {
		loadableFeatures.forEach(f -> f.load());
	}
	
	/**
	 * Calls unload() on all features that implement Loadable
	 * This function is called on plugin unload
	 */
	public void unload() {
		loadableFeatures.forEach(f -> f.unload());
	}
	
	/**
	 * Calls refresh(...) on all features that implement Refreshable
	 * 
	 * @param refreshed - player to be refreshed
	 * @param force - whether refresh should be forced or not
	 */
	public void refresh(TabPlayer refreshed, boolean force) {
		refreshables.forEach(r -> r.refresh(refreshed, true));
	}
	
	/**
	 * Calls refreshUsedPlaceholders() on all features that implement Refreshable
	 * This function is called when new placeholders enter the game (usually when a command to assign property is ran)
	 */
	public void refreshUsedPlaceholders() {
		refreshables.forEach(r -> r.refreshUsedPlaceholders());
	}
	
	/**
	 * Calls onPacketSend(...) on all features that implement PlayerInfoPacketListener and measures how long it took them to process
	 * 
	 * @param receiver - packet receiver
	 * @param packet - an instance of custom packet class PacketPlayOutPlayerInfo
	 * @return altered packet or null if packet should be cancelled
	 */
	public void onPacketPlayOutPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo packet) {
		for (PlayerInfoPacketListener f : playerInfoListeners) {
			long time = System.nanoTime();
			f.onPacketSend(receiver, packet);
			Shared.cpu.addTime(f.getFeatureType(), UsageType.PACKET_READING, System.nanoTime()-time);
		}
	}
	
	/**
	 * Calls onQuit(...) on all features that implement QuitEventListener and measures how long it took them to process
	 * 
	 * @param disconnectedPlayer - player who disconnected
	 */
	public void onQuit(TabPlayer disconnectedPlayer) {
		for (QuitEventListener l : quitListeners) {
			long time = System.nanoTime();
			l.onQuit(disconnectedPlayer);
			Shared.cpu.addTime(l.getFeatureType(), UsageType.PLAYER_QUIT_EVENT, System.nanoTime()-time);
		}
	}
	
	/**
	 * Calls onJoin(...) on all features that implement JoinEventListener and measures how long it took them to process
	 * 
	 * @param connectedPlayer - player who connected
	 */
	public void onJoin(TabPlayer connectedPlayer) {
		for (JoinEventListener l : joinListeners) {
			long time = System.nanoTime();
			l.onJoin(connectedPlayer);
			Shared.cpu.addTime(l.getFeatureType(), UsageType.PLAYER_JOIN_EVENT, System.nanoTime()-time);
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
	public void onWorldChange(TabPlayer changed, String from, String to) {
		for (WorldChangeListener l : worldChangeListeners) {
			long time = System.nanoTime();
			l.onWorldChange(changed, from, to);
			Shared.cpu.addTime(l.getFeatureType(), UsageType.WORLD_SWITCH_EVENT, System.nanoTime()-time);
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
		boolean cancel = false;
		for (CommandListener l : commandListeners) {
			long time = System.nanoTime();
			if (l.onCommand(sender, command)) cancel = true;
			Shared.cpu.addTime(l.getFeatureType(), UsageType.COMMAND_PREPROCESS, System.nanoTime()-time);
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
		for (RawPacketFeature f : rawpacketfeatures) {
			long time = System.nanoTime();
			try {
				if (newPacket != null) newPacket = f.onPacketReceive(receiver, newPacket);
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
		for (RawPacketFeature f : rawpacketfeatures) {
			long time = System.nanoTime();
			try {
				f.onPacketSend(receiver, packet);
			} catch (Throwable e) {
				Shared.errorManager.printError("Feature " + f.getFeatureType() + " failed to read packet", e);
			}
			Shared.cpu.addTime(f.getFeatureType(), UsageType.PACKET_READING, System.nanoTime()-time);
		}
	}
	
	public NameTag getNameTagFeature() {
		if (features.containsKey("nametag16")) return (NameTag) features.get("nametag16");
		return (NameTag) features.get("nametagx");
	}
}