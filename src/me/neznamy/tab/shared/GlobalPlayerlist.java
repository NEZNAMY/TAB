package me.neznamy.tab.shared;

import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

public class GlobalPlayerlist {

	public static boolean enabled;

	public static void onJoin(ITabPlayer p) {
		if (!enabled) return;
		PacketPlayOutPlayerInfo add = getAddPacket(p);
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all == p) continue;
			if (all.getWorldName().equals(p.getWorldName())) continue;
			if (!PluginHooks.PremiumVanish_isInvisible(p)) all.sendCustomPacket(add); //not adding vanished players
			if (!PluginHooks.PremiumVanish_isInvisible(all)) p.sendCustomPacket(getAddPacket(all));
		}
	}
	public static void onQuit(ITabPlayer p) {
		if (!enabled) return;
		PacketPlayOutPlayerInfo remove = getRemovePacket(p);
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all == p) continue;
			all.sendCustomPacket(remove);
		}
	}
	public static PacketPlayOutPlayerInfo getRemovePacket(ITabPlayer p) {
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, new PlayerInfoData(p.getName(), p.getTablistId(), null, 0, null, null));
	}
	public static PacketPlayOutPlayerInfo getAddPacket(ITabPlayer p) {
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, new PlayerInfoData(p.getName(), p.getTablistId(), p.getSkin(), (int)p.getPing(), EnumGamemode.CREATIVE, p.getTabFormat(null)));
	}
}