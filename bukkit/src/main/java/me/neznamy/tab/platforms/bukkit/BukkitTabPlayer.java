package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.viaversion.viaversion.api.Via;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.potion.PotionEffectType;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.rgb.RGBUtils;

/**
 * TabPlayer for Bukkit
 */
public class BukkitTabPlayer extends ITabPlayer {

	//bukkit player
	private Player player;

	//nms handle
	private Object handle;

	//nms player connection
	private Object playerConnection;
	
	//player's visible boss bars
	private Map<UUID, BossBar> bossbars = new HashMap<UUID, BossBar>();

	/**
	 * Constructs new instance with given parameter
	 * @param p - bukkit player
	 */
	public BukkitTabPlayer(Player p, int protocolVersion){
		player = p;
		world = p.getWorld().getName();
		try {
			handle = NMSStorage.getInstance().getHandle.invoke(player);
			playerConnection = NMSStorage.getInstance().PLAYER_CONNECTION.get(handle);
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().printError("Failed to get playerConnection of " + p.getName(), e);
		}
		try {
			if (NMSStorage.getInstance().CHANNEL != null)
				channel = (Channel) NMSStorage.getInstance().CHANNEL.get(NMSStorage.getInstance().NETWORK_MANAGER.get(playerConnection));
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().printError("Failed to get channel of " + p.getName(), e);
		}
		uniqueId = p.getUniqueId();
		name = p.getName();
		version = ProtocolVersion.fromNetworkId(protocolVersion);
		init();
	}

	@Override
	public boolean hasPermission(String permission) {
		return player.hasPermission(permission);
	}

	@Override
	public long getPing() {
		try {
			int ping = NMSStorage.getInstance().PING.getInt(handle);
			if (ping > 10000 || ping < 0) ping = -1;
			return ping;
		} catch (Exception e) {
			return -1;
		}
	}

	@Override
	public void sendPacket(Object nmsPacket) {
		if (nmsPacket == null || !player.isOnline()) return;
		try {
			if (((BukkitPlatform)TAB.getInstance().getPlatform()).viaversion && nmsPacket instanceof ByteBuf) {
				try {
					Via.getAPI().sendRawPacket(uniqueId, (ByteBuf) nmsPacket);
				} catch (IllegalArgumentException e) {
					//java.lang.IllegalArgumentException: This player is not controlled by ViaVersion!
					//this is only used to send 1.9 bossbar packets on 1.8 servers, no idea why it does this sometimes
				}
				return;
			}
			if (nmsPacket instanceof PacketPlayOutBoss) {
				handle((PacketPlayOutBoss) nmsPacket);
				return;
			}
			NMSStorage.getInstance().sendPacket.invoke(playerConnection, nmsPacket);
		} catch (Throwable e) {
			TAB.getInstance().getErrorManager().printError("An error occurred when sending " + nmsPacket.getClass().getSimpleName(), e);
		}
	}
	
	private void handle(PacketPlayOutBoss packet) {
		Set<BarFlag> flags = new HashSet<BarFlag>();
		BossBar bar;
		switch (packet.operation) {
		case ADD:
			if (packet.createWorldFog) flags.add(BarFlag.CREATE_FOG);
			if (packet.darkenScreen) flags.add(BarFlag.DARKEN_SKY);
			if (packet.playMusic) flags.add(BarFlag.PLAY_BOSS_MUSIC);
			bar = Bukkit.createBossBar(RGBUtils.getInstance().convertToBukkitFormat(packet.name, getVersion().getMinorVersion() >= 16 && NMSStorage.getInstance().minorVersion >= 16), 
					BarColor.valueOf(packet.color.name()), 
					BarStyle.valueOf(packet.overlay.name().replace("PROGRESS", "SOLID").replace("NOTCHED", "SEGMENTED")),
					flags.toArray(new BarFlag[0]));
			bar.setProgress(packet.pct);
			bossbars.put(packet.id, bar);
			bar.addPlayer(player);
			break;
		case REMOVE:
			bossbars.get(packet.id).removePlayer(player);
			bossbars.remove(packet.id);
			break;
		case UPDATE_PCT:
			bossbars.get(packet.id).setProgress(packet.pct);
			break;
		case UPDATE_NAME:
			bossbars.get(packet.id).setTitle(RGBUtils.getInstance().convertToBukkitFormat(packet.name, getVersion().getMinorVersion() >= 16 && NMSStorage.getInstance().minorVersion >= 16));
			break;
		case UPDATE_STYLE:
			bossbars.get(packet.id).setColor(BarColor.valueOf(packet.color.name()));
			bossbars.get(packet.id).setStyle(BarStyle.valueOf(packet.overlay.name().replace("PROGRESS", "SOLID").replace("NOTCHED", "SEGMENTED")));
			break;
		case UPDATE_PROPERTIES:
			bar = bossbars.get(packet.id);
			processFlag(bar, packet.createWorldFog, BarFlag.CREATE_FOG);
			processFlag(bar, packet.darkenScreen, BarFlag.DARKEN_SKY);
			processFlag(bar, packet.playMusic, BarFlag.PLAY_BOSS_MUSIC);
			break;
		default:
			break;
		}
	}
	
	private void processFlag(BossBar bar, boolean targetValue, BarFlag flag) {
		if (targetValue) {
			if (!bar.hasFlag(flag)) {
				bar.addFlag(flag);
			}
		} else {
			if (bar.hasFlag(flag)) {
				bar.removeFlag(flag);
			}
		}
	}

	@Override
	public boolean hasInvisibilityPotion() {
		return player.hasPotionEffect(PotionEffectType.INVISIBILITY);
	}

	@Override
	public boolean isDisguised() {
		return isDisguisedLD() || isDisguisediDis();
	}

	/**
	 * Returns disguise status of player using LibsDisguises
	 * @return disguise status of player using LibsDisguises
	 */
	private boolean isDisguisedLD() {
		try {
			if (!((BukkitPlatform)TAB.getInstance().getPlatform()).libsdisguises) return false;
			return (boolean) Class.forName("me.libraryaddict.disguise.DisguiseAPI").getMethod("isDisguised", Entity.class).invoke(null, player);
		} catch (Throwable e) {
			return TAB.getInstance().getErrorManager().printError(false, "Failed to check disguise status using LibsDisguises", e);
		}
	}

	/**
	 * Returns disguise status of player using iDisguise
	 * @return disguise status of player using iDisguise
	 */
	private boolean isDisguisediDis() {
		try {
			if (!((BukkitPlatform)TAB.getInstance().getPlatform()).idisguise) return false;
			RegisteredServiceProvider<?> provider = Bukkit.getServicesManager().getRegistration(Class.forName("de.robingrether.idisguise.api.DisguiseAPI"));
			Object iDisguise = provider.getProvider();
			Method m = iDisguise.getClass().getMethod("isDisguised", Player.class);
			m.setAccessible(true);
			return (boolean) m.invoke(iDisguise, player);
		} catch (Throwable e) {
			return TAB.getInstance().getErrorManager().printError(false, "Failed to check disguise status using iDisguise", e);
		}
	}

	@Override
	public Object getSkin() {
		try {
			return Class.forName("com.mojang.authlib.GameProfile").getMethod("getProperties").invoke(NMSStorage.getInstance().getProfile.invoke(handle));
		} catch (Throwable e) {
			return TAB.getInstance().getErrorManager().printError(null, "Failed to get skin of " + getName(), e);
		}
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public boolean isOnline() {
		return player.isOnline();
	}

	@Override
	public boolean isVanished() {
		try {
			if (((BukkitPlatform)TAB.getInstance().getPlatform()).essentials) {
				Object essentials = Bukkit.getPluginManager().getPlugin("Essentials");
				Object user = essentials.getClass().getMethod("getUser", Player.class).invoke(essentials, player);
				boolean vanished = (boolean) user.getClass().getMethod("isVanished").invoke(user);
				if (vanished) return true;
			}
			if (player.hasMetadata("vanished") && !player.getMetadata("vanished").isEmpty()) {
				return player.getMetadata("vanished").get(0).asBoolean();
			}
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().printError("Failed to check vanish status of " + player.getName(), e);
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getGamemode() {
		return player.getGameMode().getValue();
	}
}