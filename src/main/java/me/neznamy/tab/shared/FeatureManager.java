package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.neznamy.tab.shared.cpu.UsageType;
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

public class FeatureManager {

	private Map<String, Feature> features = new ConcurrentHashMap<String, Feature>();
	private List<PlayerInfoPacketListener> playerInfoListeners = new ArrayList<PlayerInfoPacketListener>();
	private List<RawPacketFeature> rawpacketfeatures = new ArrayList<RawPacketFeature>();
	private List<Loadable> loadableFeatures = new ArrayList<Loadable>();
	private List<JoinEventListener> joinListeners = new ArrayList<JoinEventListener>();
	private List<QuitEventListener> quitListeners = new ArrayList<QuitEventListener>();
	private List<WorldChangeListener> worldChangeListeners = new ArrayList<WorldChangeListener>();
	private List<CommandListener> commandListeners = new ArrayList<CommandListener>();
	public List<Refreshable> refreshables = new ArrayList<Refreshable>();
	
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
	
	public boolean isFeatureEnabled(String name) {
		return features.containsKey(name);
	}
	
	public Feature getFeature(String name) {
		return features.get(name);
	}
	
	public void load() {
		loadableFeatures.forEach(f -> f.load());
	}
	
	public void unload() {
		loadableFeatures.forEach(f -> f.unload());
	}
	
	public void refresh(ITabPlayer refreshed, boolean force) {
		refreshables.forEach(r -> r.refresh(refreshed, true));
	}
	
	public void refreshUsedPlaceholders() {
		refreshables.forEach(r -> r.refreshUsedPlaceholders());
	}
	
	public PacketPlayOutPlayerInfo onPacketPlayOutPlayerInfo(ITabPlayer receiver, PacketPlayOutPlayerInfo packet) {
		for (PlayerInfoPacketListener f : playerInfoListeners) {
			long time = System.nanoTime();
			if (packet != null) packet = f.onPacketSend(receiver, packet);
			Shared.cpu.addTime(f.getFeatureType(), UsageType.PACKET_READING, System.nanoTime()-time);
		}
		return packet;
	}
	
	public void onQuit(ITabPlayer disconnectedPlayer) {
		for (QuitEventListener l : quitListeners) {
			long time = System.nanoTime();
			l.onQuit(disconnectedPlayer);
			Shared.cpu.addTime(l.getFeatureType(), UsageType.PLAYER_QUIT_EVENT, System.nanoTime()-time);
		}
	}
	
	public void onJoin(ITabPlayer connectedPlayer) {
		for (JoinEventListener l : joinListeners) {
			long time = System.nanoTime();
			l.onJoin(connectedPlayer);
			Shared.cpu.addTime(l.getFeatureType(), UsageType.PLAYER_JOIN_EVENT, System.nanoTime()-time);
		}
		connectedPlayer.onJoinFinished = true;
	}
	
	public void onWorldChange(ITabPlayer changed, String from, String to) {
		for (WorldChangeListener l : worldChangeListeners) {
			long time = System.nanoTime();
			l.onWorldChange(changed, from, to);
			Shared.cpu.addTime(l.getFeatureType(), UsageType.WORLD_SWITCH_EVENT, System.nanoTime()-time);
		}
	}
	
	public boolean onCommand(ITabPlayer sender, String command) {
		boolean cancel = false;
		for (CommandListener l : commandListeners) {
			long time = System.nanoTime();
			if (l.onCommand(sender, command)) cancel = true;
			Shared.cpu.addTime(l.getFeatureType(), UsageType.COMMAND_PREPROCESS, System.nanoTime()-time);
		}
		return cancel;
	}
	
	public Object onPacketReceive(ITabPlayer receiver, Object packet){
		for (RawPacketFeature f : rawpacketfeatures) {
			long time = System.nanoTime();
			try {
				if (packet != null) packet = f.onPacketReceive(receiver, packet);
			} catch (Throwable e) {
				Shared.errorManager.printError("Feature " + f.getFeatureType() + " failed to read packet", e);
			}
			Shared.cpu.addTime(f.getFeatureType(), UsageType.PACKET_READING, System.nanoTime()-time);
		}
		return packet;
	}
	
	public Object onPacketSend(ITabPlayer receiver, Object packet){
		for (RawPacketFeature f : rawpacketfeatures) {
			long time = System.nanoTime();
			try {
				if (packet != null) packet = f.onPacketSend(receiver, packet);
			} catch (Throwable e) {
				Shared.errorManager.printError("Feature " + f.getFeatureType() + " failed to read packet", e);
			}
			Shared.cpu.addTime(f.getFeatureType(), UsageType.PACKET_READING, System.nanoTime()-time);
		}
		return packet;
	}
}