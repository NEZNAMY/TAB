package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;

import com.earth2me.essentials.Essentials;
import com.mojang.authlib.GameProfile;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.legacy.bossbar.BossColor;
import com.viaversion.viaversion.api.legacy.bossbar.BossFlag;
import com.viaversion.viaversion.api.legacy.bossbar.BossStyle;

import io.netty.channel.Channel;
import me.libraryaddict.disguise.DisguiseAPI;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.rgb.RGBUtils;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;

/**
 * TabPlayer for Bukkit
 */
public class BukkitTabPlayer extends ITabPlayer {

	//nms handle
	private Object handle;

	//nms player connection
	private Object playerConnection;
	
	//player's visible boss bars
	private Map<UUID, BossBar> bossbars = new HashMap<>();
	private Map<UUID, com.viaversion.viaversion.api.legacy.bossbar.BossBar> viaBossbars = new HashMap<>();

	/**
	 * Constructs new instance with given parameter
	 * @param p - bukkit player
	 */
	public BukkitTabPlayer(Player p, int protocolVersion){
		super(p, p.getUniqueId(), p.getName(), "N/A", p.getWorld().getName());
		try {
			handle = NMSStorage.getInstance().getHandle.invoke(player);
			playerConnection = NMSStorage.getInstance().PLAYER_CONNECTION.get(handle);
			if (NMSStorage.getInstance().CHANNEL != null)
				channel = (Channel) NMSStorage.getInstance().CHANNEL.get(NMSStorage.getInstance().NETWORK_MANAGER.get(playerConnection));
		} catch (InvocationTargetException | IllegalAccessException e) {
			TAB.getInstance().getErrorManager().printError("Failed to get playerConnection or channel of " + p.getName(), e);
		}
		version = ProtocolVersion.fromNetworkId(protocolVersion);
	}

	@Override
	public boolean hasPermission(String permission) {
		return getPlayer().hasPermission(permission);
	}

	@Override
	public int getPing() {
		try {
			int ping = NMSStorage.getInstance().PING.getInt(handle);
			if (ping > 10000 || ping < 0) ping = -1;
			return ping;
		} catch (IllegalAccessException e) {
			return -1;
		}
	}

	@Override
	public void sendPacket(Object nmsPacket) {
		if (nmsPacket == null || !getPlayer().isOnline()) return;
		long time = System.nanoTime();
		try {
			if (nmsPacket instanceof PacketPlayOutBoss) {
				if (NMSStorage.getInstance().getMinorVersion() >= 9) {
					handle((PacketPlayOutBoss) nmsPacket);
				} else {
					handleVia((PacketPlayOutBoss) nmsPacket);
				}
			} else {
				NMSStorage.getInstance().sendPacket.invoke(playerConnection, nmsPacket);
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			TAB.getInstance().getErrorManager().printError("An error occurred when sending " + nmsPacket.getClass().getSimpleName(), e);
		}
		TAB.getInstance().getCPUManager().addMethodTime("sendPacket", System.nanoTime()-time);
	}
	
	private void handle(PacketPlayOutBoss packet) {
		BossBar bar;
		switch (packet.getOperation()) {
		case ADD:
			bar = Bukkit.createBossBar(RGBUtils.getInstance().convertToBukkitFormat(packet.getName(), getVersion().getMinorVersion() >= 16 && NMSStorage.getInstance().getMinorVersion() >= 16), 
					BarColor.valueOf(packet.getColor().name()), 
					BarStyle.valueOf(packet.getOverlay().getBukkitName()));
			if (packet.isCreateWorldFog()) bar.addFlag(BarFlag.CREATE_FOG);
			if (packet.isDarkenScreen()) bar.addFlag(BarFlag.DARKEN_SKY);
			if (packet.isPlayMusic()) bar.addFlag(BarFlag.PLAY_BOSS_MUSIC);
			bar.setProgress(packet.getPct());
			bossbars.put(packet.getId(), bar);
			bar.addPlayer(getPlayer());
			break;
		case REMOVE:
			bossbars.get(packet.getId()).removePlayer(getPlayer());
			bossbars.remove(packet.getId());
			break;
		case UPDATE_PCT:
			bossbars.get(packet.getId()).setProgress(packet.getPct());
			break;
		case UPDATE_NAME:
			bossbars.get(packet.getId()).setTitle(RGBUtils.getInstance().convertToBukkitFormat(packet.getName(), getVersion().getMinorVersion() >= 16 && NMSStorage.getInstance().getMinorVersion() >= 16));
			break;
		case UPDATE_STYLE:
			bossbars.get(packet.getId()).setColor(BarColor.valueOf(packet.getColor().name()));
			bossbars.get(packet.getId()).setStyle(BarStyle.valueOf(packet.getOverlay().getBukkitName()));
			break;
		case UPDATE_PROPERTIES:
			bar = bossbars.get(packet.getId());
			processFlag(bar, packet.isCreateWorldFog(), BarFlag.CREATE_FOG);
			processFlag(bar, packet.isDarkenScreen(), BarFlag.DARKEN_SKY);
			processFlag(bar, packet.isPlayMusic(), BarFlag.PLAY_BOSS_MUSIC);
			break;
		default:
			break;
		}
	}
	
	private void handleVia(PacketPlayOutBoss packet) {
		com.viaversion.viaversion.api.legacy.bossbar.BossBar bar;
		switch (packet.getOperation()) {
		case ADD:
			bar = Via.getAPI().legacyAPI().createLegacyBossBar(RGBUtils.getInstance().convertToBukkitFormat(packet.getName(), getVersion().getMinorVersion() >= 16), 
					packet.getPct(),
					BossColor.valueOf(packet.getColor().name()), 
					BossStyle.valueOf(packet.getOverlay().getBukkitName()));
			//fog missing from via API
			if (packet.isDarkenScreen()) bar.addFlag(BossFlag.DARKEN_SKY);
			if (packet.isPlayMusic()) bar.addFlag(BossFlag.PLAY_BOSS_MUSIC);
			viaBossbars.put(packet.getId(), bar);
			bar.addPlayer(getPlayer().getUniqueId());
			break;
		case REMOVE:
			viaBossbars.get(packet.getId()).removePlayer(getPlayer().getUniqueId());
			viaBossbars.remove(packet.getId());
			break;
		case UPDATE_PCT:
			viaBossbars.get(packet.getId()).setHealth(packet.getPct());
			break;
		case UPDATE_NAME:
			viaBossbars.get(packet.getId()).setTitle(RGBUtils.getInstance().convertToBukkitFormat(packet.getName(), getVersion().getMinorVersion() >= 16));
			break;
		case UPDATE_STYLE:
			viaBossbars.get(packet.getId()).setColor(BossColor.valueOf(packet.getColor().name()));
			viaBossbars.get(packet.getId()).setStyle(BossStyle.valueOf(packet.getOverlay().getBukkitName()));
			break;
		case UPDATE_PROPERTIES:
			bar = viaBossbars.get(packet.getId());
			//fog missing from via API
			processFlagVia(bar, packet.isDarkenScreen(), BossFlag.DARKEN_SKY);
			processFlagVia(bar, packet.isPlayMusic(), BossFlag.PLAY_BOSS_MUSIC);
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
	
	private void processFlagVia(com.viaversion.viaversion.api.legacy.bossbar.BossBar bar, boolean targetValue, BossFlag flag) {
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
		return getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY);
	}

	@Override
	public boolean isDisguised() {
		try {
			if (!((BukkitPlatform)TAB.getInstance().getPlatform()).isLibsdisguisesEnabled()) return false;
			return DisguiseAPI.isDisguised(getPlayer());
		} catch (NoClassDefFoundError | ExceptionInInitializerError e) {
			TAB.getInstance().getErrorManager().printError("Failed to check disguise status using LibsDisguises", e);
			return false;
		}
	}

	@Override
	public Object getSkin() {
		try {
			return ((GameProfile)NMSStorage.getInstance().getProfile.invoke(handle)).getProperties();
		} catch (InvocationTargetException | IllegalAccessException e) {
			TAB.getInstance().getErrorManager().printError("Failed to get skin of " + getName(), e);
			return null;
		}
	}

	@Override
	public Player getPlayer() {
		return (Player) player;
	}

	@Override
	public boolean isOnline() {
		return getPlayer().isOnline();
	}

	@Override
	public boolean isVanished() {
		Essentials essentials = ((BukkitPlatform)TAB.getInstance().getPlatform()).getEssentials();
		if (essentials != null && essentials.getUser(getUniqueId()).isVanished()) return true;
		List<MetadataValue> metadata = getPlayer().getMetadata("vanished");
		return !metadata.isEmpty() && metadata.get(0).asBoolean();
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getGamemode() {
		return getPlayer().getGameMode().getValue();
	}
}