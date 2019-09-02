package me.neznamy.tab.bukkit;

import java.lang.reflect.Field;

import org.bukkit.GameMode;

import com.mojang.authlib.GameProfile;

import me.neznamy.tab.bukkit.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.bukkit.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.bukkit.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.bukkit.unlimitedtags.NameTagX;
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
			Shared.scheduleRepeatingTask(refresh, "refreshing tablist prefix/suffix", Feature.PLAYERLIST, new Runnable() {
				public void run() {
					for (ITabPlayer p : Shared.getPlayers()) if (!p.disabledTablistNames) p.updatePlayerListName(false);
				}
			});
		}
	}
	public static void unload() {
		if (enable) for (ITabPlayer p : Shared.getPlayers()) p.setPlayerListName();
	}
	public static void modifyPacket(PacketPlayOutPlayerInfo packet, ITabPlayer receiver) throws Exception{
		if (packet.getPlayers().isEmpty()) return; //yes some plugins send packets like that
		
		PlayerInfoData playerInfoData = packet.getPlayers().get(0);
		GameProfile gameProfile = playerInfoData.getGameProfile();
		ITabPlayer packetPlayer = Shared.getPlayer(gameProfile.getId());

		if (packet.getAction() == EnumPlayerInfoAction.UPDATE_GAME_MODE || packet.getAction() == EnumPlayerInfoAction.ADD_PLAYER) {
			if (Configs.doNotMoveSpectators && playerInfoData.getGameMode() == GameMode.SPECTATOR && gameProfile.getId() != receiver.getUniqueId()) playerInfoData.setGameMode(GameMode.CREATIVE);
		}
		if (packet.getAction() == EnumPlayerInfoAction.UPDATE_DISPLAY_NAME) {
			if (packetPlayer == null || packetPlayer.disabledTablistNames || receiver.getVersion().getNumber() < ProtocolVersion.v1_8.getNumber()) return;
			String format = packetPlayer.getTabFormat(receiver);
			playerInfoData.setPlayerListName(format);
		}
		if (packet.getAction() == EnumPlayerInfoAction.ADD_PLAYER) {
			if (packetPlayer != null) {
				//player
				if (!packetPlayer.disabledTablistNames && receiver.getVersion().getNumber() >= ProtocolVersion.v1_8.getNumber()) {
					String format = packetPlayer.getTabFormat(receiver);
					playerInfoData.setPlayerListName(format);
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
						GameProfile_properties.set(clone, gameProfile.getProperties());
						GameProfile_legacy.set(clone, gameProfile.isLegacy());
						playerInfoData.setGameProfile(clone);
						packet.getPlayers().set(0, playerInfoData);
					}
				}
			}
		}
	}
	
	public static Field GameProfile_properties;
	public static Field GameProfile_legacy;
	
	static{
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			try {
				(GameProfile_properties = GameProfile.class.getDeclaredField("properties")).setAccessible(true);
				(GameProfile_legacy = GameProfile.class.getDeclaredField("legacy")).setAccessible(true);
			} catch (Throwable e) {
				Shared.error("Failed to initialize Playerlist class", e);
			}
		}
	}
}