package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.packet.PlayerInfoPacketListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

/**
 * Sets ping of all players in the packet to configured value to prevent hacked clients from seeing exact ping value of each player
 */
public class PingSpoof implements PlayerInfoPacketListener, Loadable {

	//fake ping value
	private int value;
	
	/**
	 * Constructs new instance and loads config options
	 */
	public PingSpoof() {
		value = TAB.getInstance().getConfiguration().getConfig().getInt("ping-spoof.value", 0);
		TAB.getInstance().debug(String.format("Loaded PingSpoof feature with parameters value=%s", value));
	}
	
	@Override
	public void onPacketSend(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (info.getAction() != EnumPlayerInfoAction.UPDATE_LATENCY && info.getAction() != EnumPlayerInfoAction.ADD_PLAYER) return;
		for (PlayerInfoData playerInfoData : info.getEntries()) {
			playerInfoData.setLatency(value);
		}
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.PING_SPOOF;
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
		for (TabPlayer p : TAB.getInstance().getPlayers()) {
			list.add(new PlayerInfoData(p.getUniqueId(), realPing ? (int) p.getPing() : 0));
		}
		for (TabPlayer p : TAB.getInstance().getPlayers()) {
			p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_LATENCY, list), getFeatureType());
		}
	}
}