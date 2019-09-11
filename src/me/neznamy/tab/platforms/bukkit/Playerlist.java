package me.neznamy.tab.platforms.bukkit;

import java.util.Map.Entry;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.platforms.bukkit.unlimitedtags.NameTagX;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.Shared.Feature;

public class Playerlist {

	public static boolean enable;
	public static int refresh;

	public static void load() {
		if (enable) {
			for (ITabPlayer p : Shared.getPlayers()) if (!p.disabledTablistNames) p.updatePlayerListName(true);
			Shared.scheduleRepeatingTask(refresh, "refreshing tablist prefix/suffix", Feature.PLAYERLIST_1, new Runnable() {
				public void run() {
					for (ITabPlayer p : Shared.getPlayers()) if (!p.disabledTablistNames) p.updatePlayerListName(false);
				}
			});
		}
	}
	public static void unload() {
		if (enable) for (ITabPlayer p : Shared.getPlayers()) p.setPlayerListName();
	}
	public static void modifyPacket(PacketPlayOutPlayerInfo packet, ITabPlayer receiver){
		if (packet.getPlayers().isEmpty()) return; //yes some plugins send packets like that
		
		PlayerInfoData playerInfoData = packet.getPlayers().get(0);
		GameProfile gameProfile = (GameProfile) playerInfoData.profile;
		ITabPlayer packetPlayer = Shared.getPlayer(gameProfile.getId());

		if (packet.getAction() == EnumPlayerInfoAction.UPDATE_GAME_MODE || packet.getAction() == EnumPlayerInfoAction.ADD_PLAYER) {
			if (Configs.doNotMoveSpectators && playerInfoData.gamemode == EnumGamemode.SPECTATOR && gameProfile.getId() != receiver.getUniqueId()) playerInfoData.gamemode = EnumGamemode.CREATIVE;
		}
		if (packet.getAction() == EnumPlayerInfoAction.UPDATE_DISPLAY_NAME) {
			if (packetPlayer == null || packetPlayer.disabledTablistNames || receiver.getVersion().getNumber() < ProtocolVersion.v1_8.getNumber()) return;
			playerInfoData.playerListName = packetPlayer.getTabFormat(receiver);
		}
		if (packet.getAction() == EnumPlayerInfoAction.ADD_PLAYER) {
			if (packetPlayer != null) {
				//player
				if (!packetPlayer.disabledTablistNames && receiver.getVersion().getNumber() >= ProtocolVersion.v1_8.getNumber()) {
					playerInfoData.playerListName = packetPlayer.getTabFormat(receiver);
				}
			} else {
				//NPC
				if (NameTagX.enable && Configs.modifyNPCnames) {
					if (gameProfile.getName().length() <= 15) {
						String name;
						if (gameProfile.getName().length() <= 14) {
							name = gameProfile.getName() + "§r";
						} else {
							name = gameProfile.getName() + " ";
						}
						GameProfile clone = new GameProfile(gameProfile.getId(), name);
						for (Entry<String, Property> e : gameProfile.getProperties().entries()) {
							clone.getProperties().put(e.getKey(), e.getValue());
						}
						playerInfoData.profile = clone;
					}
				}
			}
		}
	}
}