package me.neznamy.tab.shared;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.parser.ParseException;

import me.neznamy.tab.api.FeatureManager;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.cpu.UsageType;

/**
 * Feature registration which offers calls to features and measures how long it took them to process
 */
public class FeatureManagerImpl implements FeatureManager {

	private String deserializing = "Packet deserializing";
	private String serializing = "Packet serializing";

	//list of registered features
	private Map<String, TabFeature> features = new LinkedHashMap<>();

	@Override
	public void registerFeature(String featureName, TabFeature featureHandler) {
		features.put(featureName, featureHandler);
	}

	@Override
	public void unregisterFeature(String featureName) {
		features.remove(featureName);
	}

	@Override
	public boolean isFeatureEnabled(String name) {
		return features.containsKey(name);
	}

	@Override
	public TabFeature getFeature(String name) {
		return features.get(name);
	}

	/**
	 * Returns list of all loaded features
	 * @return list of all loaded features
	 */
	public Collection<TabFeature> getAllFeatures(){
		try {
			return new ArrayList<>(features.values());
		} catch (ConcurrentModificationException e) {
			return getAllFeatures();
		}
	}

	/**
	 * Calls load() on all features
	 * This function is called on plugin startup
	 */
	public void load() {
		getAllFeatures().forEach(TabFeature::load);
	}

	/**
	 * Calls unload() on all features
	 * This function is called on plugin unload
	 */
	public void unload() {
		getAllFeatures().forEach(TabFeature::unload);
	}

	/**
	 * Calls refresh(...) on all features
	 * 
	 * @param refreshed - player to be refreshed
	 * @param force - whether refresh should be forced or not
	 */
	public void refresh(TabPlayer refreshed, boolean force) {
		getAllFeatures().forEach(f -> f.refresh(refreshed, force));
	}

	/**
	 * Calls onPacketSend(...) on all features
	 * 
	 * @param receiver - packet receiver
	 * @param packet - an instance of custom packet class PacketPlayOutPlayerInfo
	 * @return altered packet or null if packet should be cancelled
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ParseException 
	 */
	public Object onPacketPlayOutPlayerInfo(TabPlayer receiver, Object packet) throws IllegalAccessException, InvocationTargetException, InstantiationException, ParseException {
		if (receiver.getVersion().getMinorVersion() < 8) return packet;
		long time = System.nanoTime();
		PacketPlayOutPlayerInfo info = TAB.getInstance().getPlatform().getPacketBuilder().readPlayerInfo(packet, receiver.getVersion());
		TAB.getInstance().getCPUManager().addTime(deserializing, UsageType.PACKET_PLAYER_INFO, System.nanoTime()-time);
		for (TabFeature f : getAllFeatures()) {
			if (!f.isOnPacketSendInfoOverride()) continue;
			time = System.nanoTime();
			f.onPlayerInfo(receiver, info);
			TAB.getInstance().getCPUManager().addTime(f, UsageType.PACKET_PLAYER_INFO, System.nanoTime()-time);
		}
		time = System.nanoTime();
		Object pack = info.build(receiver.getVersion());
		TAB.getInstance().getCPUManager().addTime(serializing, UsageType.PACKET_PLAYER_INFO, System.nanoTime()-time);
		return pack;
	}

	/**
	 * Calls onQuit(...) on all features
	 * 
	 * @param disconnectedPlayer - player who disconnected
	 */
	public void onQuit(TabPlayer disconnectedPlayer) {
		if (disconnectedPlayer == null) return;
		for (TabFeature f : getAllFeatures()) {
			long time = System.nanoTime();
			f.onQuit(disconnectedPlayer);
			TAB.getInstance().getCPUManager().addTime(f, UsageType.PLAYER_QUIT_EVENT, System.nanoTime()-time);
		}
		TAB.getInstance().removePlayer(disconnectedPlayer);
	}

	/**
	 * Calls onJoin(...) on all features
	 * 
	 * @param connectedPlayer - player who connected
	 */
	public void onJoin(TabPlayer connectedPlayer) {
		if (!connectedPlayer.isOnline()) {
			TAB.getInstance().debug("Player " + connectedPlayer.getName() + " was offline during login process.");
			return;
		}
		long millis = System.currentTimeMillis();
		TAB.getInstance().addPlayer(connectedPlayer);
		for (TabFeature f : getAllFeatures()) {
			long time = System.nanoTime();
			f.onJoin(connectedPlayer);
			TAB.getInstance().getCPUManager().addTime(f, UsageType.PLAYER_JOIN_EVENT, System.nanoTime()-time);
		}
		((ITabPlayer)connectedPlayer).markAsLoaded();
		TAB.getInstance().debug("Player join of " + connectedPlayer.getName() + " processed in " + (System.currentTimeMillis()-millis) + "ms");
	}

	/**
	 * Calls onWorldChange(...) on all features
	 * 
	 * @param changed - player who switched world
	 * @param from - name of the previous world
	 * @param to - name of the new world
	 */
	public void onWorldChange(UUID playerUUID, String to) {
		TabPlayer changed = TAB.getInstance().getPlayer(playerUUID);
		if (changed == null || !changed.isLoaded()) {
			TAB.getInstance().getCPUManager().runTaskLater(100, "processing delayed world/server switch", "Other", UsageType.WORLD_SWITCH_EVENT, () -> onWorldChange(playerUUID, to));
			return;
		}
		String from = changed.getWorld();
		((ITabPlayer)changed).setWorld(to);
		for (TabFeature f : getAllFeatures()) {
			long time = System.nanoTime();
			f.onWorldChange(changed, from, to);
			TAB.getInstance().getCPUManager().addTime(f, UsageType.WORLD_SWITCH_EVENT, System.nanoTime()-time);
		}
	}

	/**
	 * Calls onServerChange(...) on all features
	 * 
	 * @param changed - player who switched server
	 * @param from - name of the previous server
	 * @param to - name of the new server
	 */
	public void onServerChange(UUID playerUUID, String to) {
		TabPlayer changed = TAB.getInstance().getPlayer(playerUUID);
		String from = changed.getServer();
		((ITabPlayer)changed).setServer(to);
		for (TabFeature f : getAllFeatures()) {
			long time = System.nanoTime();
			f.onServerChange(changed, from, to);
			TAB.getInstance().getCPUManager().addTime(f, UsageType.SERVER_SWITCH_EVENT, System.nanoTime()-time);
		}
	}

	/**
	 * Calls onCommand(...) on all features
	 * 
	 * @param sender - command sender
	 * @param command - command line including /
	 * @return true if command should be cancelled, false if not
	 */
	public boolean onCommand(TabPlayer sender, String command) {
		if (sender == null) return false;
		boolean cancel = false;
		for (TabFeature f : getAllFeatures()) {
			if (!f.isOnCommandOverride()) continue;
			long time = System.nanoTime();
			if (f.onCommand(sender, command)) cancel = true;
			TAB.getInstance().getCPUManager().addTime(f, UsageType.COMMAND_PREPROCESS, System.nanoTime()-time);
		}
		return cancel;
	}

	/**
	 * Calls onPacketReceive(...) on all features
	 * 
	 * @param receiver - packet receiver
	 * @param packet - IN packet coming from player
	 * @return altered packet or null if packet should be cancelled
	 */
	public boolean onPacketReceive(TabPlayer receiver, Object packet){
		boolean cancel = false;
		for (TabFeature f : getAllFeatures()) {
			if (!f.isOnPacketReceiveOverride()) continue;
			long time = System.nanoTime();
			try {
				cancel = f.onPacketReceive(receiver, packet);
			} catch (IllegalAccessException e) {
				TAB.getInstance().getErrorManager().printError("Feature " + f.getFeatureName() + " failed to read packet", e);
			}
			TAB.getInstance().getCPUManager().addTime(f, UsageType.PACKET_READING_IN, System.nanoTime()-time);
		}
		return cancel;
	}

	/**
	 * Calls onPacketSend(...) on all features
	 * 
	 * @param receiver - packet receiver
	 * @param packet - OUT packet coming from the server
	 */
	public void onPacketSend(TabPlayer receiver, Object packet){
		for (TabFeature f : getAllFeatures()) {
			if (!f.isOnPacketSendOverride()) continue;
			long time = System.nanoTime();
			try {
				f.onPacketSend(receiver, packet);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException e) {
				TAB.getInstance().getErrorManager().printError("Feature " + f.getFeatureName() + " failed to read packet", e);
			}
			TAB.getInstance().getCPUManager().addTime(f, UsageType.PACKET_READING_OUT, System.nanoTime()-time);
		}
	}

	/**
	 * Calls onLoginPacket on all featurs that implement LoginPacketListener and measures how long it took them to process
	 * @param packetReceiver - player who received the packet
	 */
	public void onLoginPacket(TabPlayer packetReceiver) {
		for (TabFeature f : getAllFeatures()) {
			long time = System.nanoTime();
			f.onLoginPacket(packetReceiver);
			TAB.getInstance().getCPUManager().addTime(f, UsageType.PACKET_LOGIN, System.nanoTime()-time);
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
		PacketPlayOutScoreboardDisplayObjective display = TAB.getInstance().getPlatform().getPacketBuilder().readDisplayObjective(packet, packetReceiver.getVersion());
		TAB.getInstance().getCPUManager().addTime(deserializing, UsageType.PACKET_DISPLAY_OBJECTIVE, System.nanoTime()-time);
		for (TabFeature f : getAllFeatures()) {
			if (!f.isOnPacketSendDisplayOverride()) continue;
			time = System.nanoTime();
			boolean cancel = f.onDisplayObjective(packetReceiver, display);
			TAB.getInstance().getCPUManager().addTime(f, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
			if (cancel) return true;
		}
		return false;
	}

	/**
	 * Calls onObjective on all featurs that implement ObjectivePacketListener and measures how long it took them to process
	 * @param packetReceiver - player who received the packet
	 * @throws IllegalAccessException 
	 * @throws ParseException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	public void onObjective(TabPlayer packetReceiver, Object packet) throws IllegalAccessException, ParseException, IllegalArgumentException, InvocationTargetException {
		long time = System.nanoTime();
		PacketPlayOutScoreboardObjective display = TAB.getInstance().getPlatform().getPacketBuilder().readObjective(packet, packetReceiver.getVersion());
		TAB.getInstance().getCPUManager().addTime(deserializing, UsageType.PACKET_OBJECTIVE, System.nanoTime()-time);
		for (TabFeature f : getAllFeatures()) {
			if (!f.isOnPacketSendObjectiveOverride()) continue;
			time = System.nanoTime();
			f.onObjective(packetReceiver, display);
			TAB.getInstance().getCPUManager().addTime(f, UsageType.ANTI_OVERRIDE, System.nanoTime()-time);
		}
	}
}