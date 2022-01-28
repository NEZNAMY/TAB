package me.neznamy.tab.shared;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import me.neznamy.tab.api.FeatureManager;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.config.mysql.MySQLUserConfiguration;

/**
 * Feature registration which offers calls to all features and measures how long it took them to process
 */
public class FeatureManagerImpl implements FeatureManager {

	private static final String deserializing = "Packet deserializing";
	private static final String serializing = "Packet serializing";

	//list of registered features
	private final Map<String, TabFeature> features = new LinkedHashMap<>();
	
	private TabFeature[] values = new TabFeature[0];

	@Override
	public void registerFeature(String featureName, TabFeature featureHandler) {
		features.put(featureName, featureHandler);
		values = features.values().toArray(new TabFeature[0]);
	}

	@Override
	public void unregisterFeature(String featureName) {
		features.remove(featureName);
		values = features.values().toArray(new TabFeature[0]);
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
	 * Calls load() on all features
	 * This function is called on plugin startup
	 */
	public void load() {
		for (TabFeature f : values) f.load();
		if (TAB.getInstance().getConfiguration().getUsers() instanceof MySQLUserConfiguration) {
			MySQLUserConfiguration users = (MySQLUserConfiguration) TAB.getInstance().getConfiguration().getUsers();
			for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) users.load(p);
		}
	}

	/**
	 * Calls unload() on all features
	 * This function is called on plugin unload
	 */
	public void unload() {
		for (TabFeature f : values) f.unload();
	}

	/**
	 * Calls refresh(...) on all features
	 * 
	 * @param refreshed - player to be refreshed
	 * @param force - whether refresh should be forced or not
	 */
	public void refresh(TabPlayer refreshed, boolean force) {
		for (TabFeature f : values) f.refresh(refreshed, force);
	}

	/**
	 * Calls onPacketSend(...) on all features
	 * 
	 * @param receiver - packet receiver
	 * @param packet - an instance of custom packet class PacketPlayOutPlayerInfo
	 * @return altered packet or null if packet should be cancelled
	 * @throws	ReflectiveOperationException
	 * 			if reflective operation fails
	 */
	public Object onPacketPlayOutPlayerInfo(TabPlayer receiver, Object packet) throws ReflectiveOperationException {
		if (receiver.getVersion().getMinorVersion() < 8) return packet;
		long time = System.nanoTime();
		PacketPlayOutPlayerInfo info = TAB.getInstance().getPlatform().getPacketBuilder().readPlayerInfo(packet, receiver.getVersion());
		TAB.getInstance().getCPUManager().addTime(deserializing, TabConstants.CpuUsageCategory.PACKET_PLAYER_INFO, System.nanoTime()-time);
		for (TabFeature f : values) {
			if (!f.overridesMethod("onPlayerInfo")) continue;
			time = System.nanoTime();
			f.onPlayerInfo(receiver, info);
			TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.PACKET_PLAYER_INFO, System.nanoTime()-time);
		}
		time = System.nanoTime();
		Object pack = TAB.getInstance().getPlatform().getPacketBuilder().build(info, receiver.getVersion());
		TAB.getInstance().getCPUManager().addTime(serializing, TabConstants.CpuUsageCategory.PACKET_PLAYER_INFO, System.nanoTime()-time);
		return pack;
	}

	/**
	 * Calls onQuit(...) on all features
	 * 
	 * @param disconnectedPlayer - player who disconnected
	 */
	public void onQuit(TabPlayer disconnectedPlayer) {
		if (disconnectedPlayer == null) return;
		long millis = System.currentTimeMillis();
		for (TabFeature f : values) {
			if (!f.overridesMethod("onQuit")) continue;
			long time = System.nanoTime();
			f.onQuit(disconnectedPlayer);
			TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.PLAYER_QUIT, System.nanoTime()-time);
		}
		TAB.getInstance().removePlayer(disconnectedPlayer);
		TAB.getInstance().debug("Player quit of " + disconnectedPlayer.getName() + " processed in " + (System.currentTimeMillis()-millis) + "ms");
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
		for (TabFeature f : values) {
			if (!f.overridesMethod("onJoin")) continue;
			long time = System.nanoTime();
			f.onJoin(connectedPlayer);
			TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.PLAYER_JOIN, System.nanoTime()-time);
			TAB.getInstance().debug("Feature " + f.getClass().getSimpleName() + " processed player join in " + (System.nanoTime()-time)/1000000 + "ms");
		}
		((ITabPlayer)connectedPlayer).markAsLoaded(true);
		TAB.getInstance().debug("Player join of " + connectedPlayer.getName() + " processed in " + (System.currentTimeMillis()-millis) + "ms");
		if (TAB.getInstance().getConfiguration().getUsers() instanceof MySQLUserConfiguration) {
			MySQLUserConfiguration users = (MySQLUserConfiguration) TAB.getInstance().getConfiguration().getUsers();
			users.load(connectedPlayer);
		}
	}

	/**
	 * Calls onWorldChange(...) on all features
	 * 
	 * @param playerUUID - player who switched world
	 * @param to - name of the new world
	 */
	public void onWorldChange(UUID playerUUID, String to) {
		TabPlayer changed = TAB.getInstance().getPlayer(playerUUID);
		if (changed == null) return;
		String from = changed.getWorld();
		((ITabPlayer)changed).setWorld(to);
		for (TabFeature f : values) {
			if (!f.overridesMethod("onWorldChange")) continue;
			long time = System.nanoTime();
			f.onWorldChange(changed, from, to);
			TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.WORLD_SWITCH, System.nanoTime()-time);
		}
		((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder("%world%")).updateValue(changed, to);
	}

	/**
	 * Calls onServerChange(...) on all features
	 * 
	 * @param playerUUID - player who switched server
	 * @param to - name of the new server
	 */
	public void onServerChange(UUID playerUUID, String to) {
		TabPlayer changed = TAB.getInstance().getPlayer(playerUUID);
		String from = changed.getServer();
		((ITabPlayer)changed).setServer(to);
		for (TabFeature f : values) {
			if (!f.overridesMethod("onServerChange")) continue;
			long time = System.nanoTime();
			f.onServerChange(changed, from, to);
			TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.SERVER_SWITCH, System.nanoTime()-time);
		}
		((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder("%server%")).updateValue(changed, to);
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
		for (TabFeature f : values) {
			if (!f.overridesMethod("onCommand")) continue;
			long time = System.nanoTime();
			if (f.onCommand(sender, command)) cancel = true;
			TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.COMMAND_PREPROCESS, System.nanoTime()-time);
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
		for (TabFeature f : values) {
			if (!f.overridesMethod("onPacketReceive")) continue;
			long time = System.nanoTime();
			try {
				cancel = f.onPacketReceive(receiver, packet);
			} catch (ReflectiveOperationException e) {
				TAB.getInstance().getErrorManager().printError("Feature " + f.getFeatureName() + " failed to read packet", e);
			}
			TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.RAW_PACKET_IN, System.nanoTime()-time);
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
		for (TabFeature f : values) {
			if (!f.overridesMethod("onPacketSend")) continue;
			long time = System.nanoTime();
			try {
				f.onPacketSend(receiver, packet);
			} catch (ReflectiveOperationException e) {
				TAB.getInstance().getErrorManager().printError("Feature " + f.getFeatureName() + " failed to read packet", e);
			}
			TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.RAW_PACKET_OUT, System.nanoTime()-time);
		}
	}

	/**
	 * Calls onLoginPacket on all features that implement LoginPacketListener and measures how long it took them to process
	 * @param packetReceiver - player who received the packet
	 */
	public void onLoginPacket(TabPlayer packetReceiver) {
		((ITabPlayer)packetReceiver).clearRegisteredObjectives();
		for (TabFeature f : values) {
			if (!f.overridesMethod("onLoginPacket")) continue;
			long time = System.nanoTime();
			f.onLoginPacket(packetReceiver);
			TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.PACKET_JOIN_GAME, System.nanoTime()-time);
		}
	}

	/**
	 * Calls onPacketSend on all features that implement DisplayObjectivePacketListener and measures how long it took them to process
	 * @param packetReceiver - player who received the packet
	 * @param packet - the packet
	 * @throws	ReflectiveOperationException
	 * 			if reflective operation fails
	 */
	public void onDisplayObjective(TabPlayer packetReceiver, Object packet) throws ReflectiveOperationException {
		long time = System.nanoTime();
		PacketPlayOutScoreboardDisplayObjective display = TAB.getInstance().getPlatform().getPacketBuilder().readDisplayObjective(packet);
		TAB.getInstance().getCPUManager().addTime(deserializing, TabConstants.CpuUsageCategory.PACKET_DISPLAY_OBJECTIVE, System.nanoTime()-time);
		for (TabFeature f : values) {
			if (!f.overridesMethod("onDisplayObjective")) continue;
			time = System.nanoTime();
			f.onDisplayObjective(packetReceiver, display);
			TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.ANTI_OVERRIDE, System.nanoTime()-time);
		}
	}

	/**
	 * Calls onObjective on all features that implement ObjectivePacketListener and measures how long it took them to process
	 * @param packetReceiver - player who received the packet
	 * @throws	ReflectiveOperationException
	 * 			if reflective operation fails
	 */
	public void onObjective(TabPlayer packetReceiver, Object packet) throws ReflectiveOperationException {
		long time = System.nanoTime();
		PacketPlayOutScoreboardObjective display = TAB.getInstance().getPlatform().getPacketBuilder().readObjective(packet);
		TAB.getInstance().getCPUManager().addTime(deserializing, TabConstants.CpuUsageCategory.PACKET_OBJECTIVE, System.nanoTime()-time);
		for (TabFeature f : values) {
			if (!f.overridesMethod("onObjective")) continue;
			time = System.nanoTime();
			f.onObjective(packetReceiver, display);
			TAB.getInstance().getCPUManager().addTime(f, TabConstants.CpuUsageCategory.ANTI_OVERRIDE, System.nanoTime()-time);
		}
	}
}