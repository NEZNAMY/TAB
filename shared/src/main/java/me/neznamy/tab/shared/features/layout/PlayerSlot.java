package me.neznamy.tab.shared.features.layout;

import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.Playerlist;

public class PlayerSlot {

	private Layout layout;
	private UUID id;
	private int slot;
	private TabPlayer player;
	
	public PlayerSlot(Layout layout, UUID id, int slot) {
		this.layout = layout;
		this.id = id;
		this.slot = slot;
	}
	
	public void setPlayer(TabPlayer newPlayer) {
		if (player == newPlayer) return;
		this.player = newPlayer;
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			all.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, new PlayerInfoData(id)), layout);
			onJoin(all);
		}
	}
	
	public void onJoin(TabPlayer p) {
		PlayerInfoData data;
		if (player != null) {
			data = new PlayerInfoData(layout.formatSlot(slot), id, player.getSkin(), (int) player.getPing(), EnumGamemode.SURVIVAL, 
					((Playerlist)TAB.getInstance().getFeatureManager().getFeature("playerlist")).getTabFormat(player, p));
		} else {
			data = new PlayerInfoData(layout.formatSlot(slot), id, null, 0, EnumGamemode.SURVIVAL, new IChatBaseComponent(""));
		}
		p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, data), layout);
	}
}
