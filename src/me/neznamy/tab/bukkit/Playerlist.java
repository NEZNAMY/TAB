package me.neznamy.tab.bukkit;

import java.lang.reflect.Field;

import org.bukkit.GameMode;

import com.mojang.authlib.GameProfile;

import me.neznamy.tab.bukkit.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.bukkit.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.bukkit.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public class Playerlist {

	public static boolean enable;
	public static int refresh;

	public static void load() {
		if (enable) {
			for (ITabPlayer p : Shared.getPlayers()) {
				if (!p.disabledTablistNames) p.updatePlayerListName(true);
			}
			Shared.scheduleRepeatingTask(refresh, "refreshing tablist prefix/suffix", "playerlist", new Runnable() {
				
				public void run() {
					for (ITabPlayer p : Shared.getPlayers()) {
						if (!p.disabledTablistNames) p.updatePlayerListName(false);
					}
				}
			});
		}
	}
	public static void unload() {
		if (enable) for (ITabPlayer p : Shared.getPlayers()) p.setPlayerListName();
	}
	public static void modifyPacket(PacketPlayOutPlayerInfo packet, ITabPlayer receiver) throws Exception{
		if (!enable) return;
		if (packet.getAction() == EnumPlayerInfoAction.UPDATE_LATENCY) return;

		if (packet.getPlayers().isEmpty()) return; //yes some plugins send packets like that
		PlayerInfoData playerInfoData = packet.getPlayers().get(0);
		GameProfile gameProfile = playerInfoData.getGameProfile();
		ITabPlayer packetPlayer = Shared.getPlayer(gameProfile.getId());

		if (packet.getAction() == EnumPlayerInfoAction.UPDATE_GAME_MODE) {
			if (Configs.doNotMoveSpectators && playerInfoData.getGameMode() == GameMode.SPECTATOR && gameProfile.getId() != receiver.getUniqueId()) playerInfoData.setGameMode(GameMode.CREATIVE);
		}
		if (packet.getAction() == EnumPlayerInfoAction.UPDATE_DISPLAY_NAME) {
			if (packetPlayer == null || packetPlayer.disabledTablistNames) return;
			String format = packetPlayer.getTabFormat(receiver);
			playerInfoData.setPlayerListName(format);
		}
		if (packet.getAction() == EnumPlayerInfoAction.ADD_PLAYER) {
			if (Shared.getPlayer(gameProfile.getId()) == null) {
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
						GameProfile_properties.set(clone, gameProfile.getProperties());
						GameProfile_legacy.set(clone, gameProfile.isLegacy());
						playerInfoData.setGameProfile(clone);
						packet.getPlayers().clear();
						packet.getPlayers().add(playerInfoData);
					}
				}
			} else {
				//player
				if (packetPlayer == null) {
					Shared.error("Data of player " + gameProfile.getName() + " did not exist when reading PacketPlayOutPlayerInfo!?");
					return;
				}
				if (!packetPlayer.disabledTablistNames) {
					String format = packetPlayer.getTabFormat(receiver);
					playerInfoData.setPlayerListName(format);
					if (Configs.doNotMoveSpectators && playerInfoData.getGameMode() == GameMode.SPECTATOR && gameProfile.getId() != receiver.getUniqueId()) playerInfoData.setGameMode(GameMode.CREATIVE);
				}
			}
		}
	}
	
	public static Field GameProfile_properties;
	public static Field GameProfile_legacy;
	
	static{
		try {
			(GameProfile_properties = GameProfile.class.getDeclaredField("properties")).setAccessible(true);
			(GameProfile_legacy = GameProfile.class.getDeclaredField("legacy")).setAccessible(true);
		} catch (Exception e) {
			Shared.error("Failed to initialize Playerlist class", e);
		}
	}
}