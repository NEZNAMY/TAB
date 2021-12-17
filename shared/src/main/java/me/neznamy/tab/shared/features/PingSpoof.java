package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TAB;

/**
 * Sets ping of all players in the packet to configured value to prevent hacked clients from seeing exact ping value of each player
 */
public class PingSpoof extends TabFeature {

	//fake ping value
	private final int value = TAB.getInstance().getConfiguration().getConfig().getInt("ping-spoof.value", 0);
	
	/**
	 * Constructs new instance and loads config options
	 */
	public PingSpoof() {
		super("Ping spoof", null);
		TAB.getInstance().debug(String.format("Loaded PingSpoof feature with parameters value=%s", value));
	}
	
	@Override
	public void onPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (info.getAction() != EnumPlayerInfoAction.UPDATE_LATENCY && info.getAction() != EnumPlayerInfoAction.ADD_PLAYER) return;
		for (PlayerInfoData playerInfoData : info.getEntries()) {
			if (TAB.getInstance().getPlayerByTablistUUID(playerInfoData.getUniqueId()) != null) playerInfoData.setLatency(value);
		}
	}

	@Override
	public void load() {
		updateAll(false);
	}

	@Override
	public void unload() {
		updateAll(true);
	}
	
	private void updateAll(boolean realPing) {
		List<PlayerInfoData> list = new ArrayList<>();
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			list.add(new PlayerInfoData(p.getUniqueId(), realPing ? p.getPing() : value));
		}
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_LATENCY, list), this);
		}
	}
}