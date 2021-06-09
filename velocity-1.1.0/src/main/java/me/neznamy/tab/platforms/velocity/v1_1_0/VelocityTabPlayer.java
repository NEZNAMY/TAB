package me.neznamy.tab.platforms.velocity.v1_1_0;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.api.util.GameProfile.Property;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Flag;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.identity.Identity;

/**
 * TabPlayer for Velocity
 */
public class VelocityTabPlayer extends ITabPlayer {

	//the velocity player
	private Player player;
	
	// uuid used in tablist
	private UUID tablistId;
	
	//player's visible boss bars
	private Map<UUID, BossBar> bossbars = new HashMap<UUID, BossBar>();

	/**
	 * Constructs new instance for given player
	 * @param p - velocity player
	 */
	public VelocityTabPlayer(Player p) {
		player = p;
		if (p.getCurrentServer().isPresent()) {
			world = p.getCurrentServer().get().getServerInfo().getName();
		} else {
			//tab reload while a player is connecting, how unfortunate
			world = "<null>";
		}
		name = p.getUsername();
		uniqueId = p.getUniqueId();
		UUID offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
		tablistId = TAB.getInstance().getConfiguration().config.getBoolean("use-online-uuid-in-tablist", true) ? uniqueId : offlineId;
		version = ProtocolVersion.fromNetworkId(player.getProtocolVersion().getProtocol());
		init();
	}
	
	@Override
	public boolean hasPermission(String permission) {
		if (TAB.getInstance().isSupportBukkitPermission()) {
			String merge = "hasPermission:" + permission;
			Main.plm.requestAttribute(this, merge);
			if (!attributes.containsKey(merge)) return false;
			return Boolean.parseBoolean(attributes.get(merge));
		}
		return player.hasPermission(permission);
	}
	
	@Override
	public long getPing() {
		return player.getPing();
	}
	
	@Override
	public void sendPacket(Object packet) {
		if (packet == null || !player.isActive()) return;
		if (packet instanceof UniversalPacketPlayOut){
			handleCustomPacket((UniversalPacketPlayOut) packet);
			return;
		}
		channel.writeAndFlush(packet, channel.voidPromise());
	}
	
	@SuppressWarnings("unchecked")
	private void handleCustomPacket(UniversalPacketPlayOut packet) {
		if (packet instanceof PacketPlayOutChat){
			player.sendMessage(Identity.nil(), Main.stringToComponent(((PacketPlayOutChat)packet).message.toString(getVersion())), MessageType.valueOf(((PacketPlayOutChat)packet).type.name()));
		}
		if (packet instanceof PacketPlayOutPlayerListHeaderFooter) {
			player.getTabList().setHeaderAndFooter(Main.stringToComponent(((PacketPlayOutPlayerListHeaderFooter) packet).header.toString(getVersion())), Main.stringToComponent(((PacketPlayOutPlayerListHeaderFooter) packet).footer.toString(getVersion())));
		}
		if (packet instanceof PacketPlayOutBoss) {
			PacketPlayOutBoss boss = (PacketPlayOutBoss) packet;
			Set<Flag> flags;
			BossBar bar;
			switch (boss.operation) {
			case ADD:
				flags = new HashSet<Flag>();
				if (boss.createWorldFog) flags.add(Flag.CREATE_WORLD_FOG);
				if (boss.darkenScreen) flags.add(Flag.DARKEN_SCREEN);
				if (boss.playMusic) flags.add(Flag.PLAY_BOSS_MUSIC);
				bar = BossBar.bossBar(Main.stringToComponent(IChatBaseComponent.optimizedComponent(boss.name).toString(getVersion())), boss.pct, Color.valueOf(boss.color.toString()), Overlay.valueOf(boss.overlay.toString()), flags);
				bossbars.put(boss.id, bar);
				player.showBossBar(bar);
				break;
			case REMOVE:
				player.hideBossBar(bossbars.get(boss.id));
				bossbars.remove(boss.id);
				break;
			case UPDATE_PCT:
				bossbars.get(boss.id).percent(boss.pct);
				break;
			case UPDATE_NAME:
				bossbars.get(boss.id).name(Main.stringToComponent(IChatBaseComponent.optimizedComponent(boss.name).toString(getVersion())));
				break;
			case UPDATE_STYLE:
				bar = bossbars.get(boss.id);
				//compensating for an already fixed bug for those who did not update Velocity
				player.hideBossBar(bar);
				bar.overlay(Overlay.valueOf(boss.overlay.toString()));
				bar.color(Color.valueOf(boss.color.toString()));
				player.showBossBar(bar);
				break;
			case UPDATE_PROPERTIES:
				flags = new HashSet<Flag>();
				if (boss.createWorldFog) flags.add(Flag.CREATE_WORLD_FOG);
				if (boss.darkenScreen) flags.add(Flag.DARKEN_SCREEN);
				if (boss.playMusic) flags.add(Flag.PLAY_BOSS_MUSIC);
				bossbars.get(boss.id).flags(flags);
				break;
			default:
				break;
			}
		}
		if (packet instanceof PacketPlayOutPlayerInfo) {
			for (PlayerInfoData data : ((PacketPlayOutPlayerInfo)packet).entries) {
				switch (((PacketPlayOutPlayerInfo) packet).action) {
				case ADD_PLAYER:
					player.getTabList().addEntry(TabListEntry.builder()
							.tabList(player.getTabList())
							.displayName(Main.stringToComponent(data.displayName.toString(getVersion())))
							.gameMode(data.gameMode.ordinal()-1)
							.profile(new GameProfile(data.uniqueId, data.name, (List<Property>) data.skin))
							.latency(data.latency)
							.build());
					break;
				case REMOVE_PLAYER:
					player.getTabList().removeEntry(data.uniqueId);
					break;
				case UPDATE_DISPLAY_NAME:
					for (TabListEntry entry : player.getTabList().getEntries()) {
						if (entry.getProfile().getId().equals(data.uniqueId)) entry.setDisplayName(Main.stringToComponent(data.displayName.toString(getVersion())));
					}
					break;
				case UPDATE_LATENCY:
					for (TabListEntry entry : player.getTabList().getEntries()) {
						if (entry.getProfile().getId().equals(data.uniqueId)) entry.setLatency(data.latency);
					}
					break;
				case UPDATE_GAME_MODE:
					for (TabListEntry entry : player.getTabList().getEntries()) {
						if (entry.getProfile().getId().equals(data.uniqueId)) entry.setGameMode(data.gameMode.ordinal()-1);
					}
					break;
				}
			}
		}
	}
	
	@Override
	public Object getSkin() {
		return player.getGameProfile().getProperties();
	}
	
	@Override
	public Player getPlayer() {
		return player;
	}
	
	@Override
	public UUID getTablistUUID() {
		return tablistId;
	}

	@Override
	public boolean isVanished() {
		Main.plm.requestAttribute(this, "vanished");
		if (!attributes.containsKey("vanished")) return false;
		return Boolean.parseBoolean(attributes.get("vanished"));
	}
	
	@Override
	public boolean isDisguised() {
		Main.plm.requestAttribute(this, "disguised");
		if (!attributes.containsKey("disguised")) return false;
		return Boolean.parseBoolean(attributes.get("disguised"));
	}
	
	@Override
	public boolean hasInvisibilityPotion() {
		Main.plm.requestAttribute(this, "invisible");
		if (!attributes.containsKey("invisible")) return false;
		return Boolean.parseBoolean(attributes.get("invisible"));
	}
	
	@Override
	public boolean isOnline() {
		return player.isActive();
	}

	@Override
	public int getGamemode() {
		return 0; //shrug
	}
}