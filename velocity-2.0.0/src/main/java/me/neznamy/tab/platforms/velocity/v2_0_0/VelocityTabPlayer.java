package me.neznamy.tab.platforms.velocity.v2_0_0;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.velocitypowered.api.proxy.connection.Player;

import io.netty.channel.Channel;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
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
public class VelocityTabPlayer extends ITabPlayer{

	//the velocity player
	private Player player;
	
	//offline uuid used in tablist
	private UUID offlineId;
	
	//player's visible boss bars
	private Map<UUID, BossBar> bossbars = new HashMap<UUID, BossBar>();

	/**
	 * Constructs new instance for given player
	 * @param p - velocity player
	 * @throws Exception - when reflection fails
	 */
	public VelocityTabPlayer(Player p) throws Exception {
		player = p;
		if (p.connectedServer().isPresent()) {
			world = p.connectedServer().get().serverInfo().name();
		} else {
			//tab reload while a player is connecting, how unfortunate
			world = "<null>";
		}
		Object minecraftConnection = player.getClass().getMethod("getConnection").invoke(player);
		channel = (Channel) minecraftConnection.getClass().getMethod("getChannel").invoke(minecraftConnection);
		name = p.username();
		uniqueId = p.id();
		offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
		version = ProtocolVersion.fromNetworkId(player.protocolVersion().protocol());
		init();
	}
	
	@Override
	public boolean hasPermission(String permission) {
		return player.hasPermission(permission);
	}
	
	@Override
	public long getPing() {
		return player.ping();
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
	
	private void handleCustomPacket(UniversalPacketPlayOut packet) {
		if (packet instanceof PacketPlayOutChat){
			player.sendMessage(Identity.nil(), Main.stringToComponent(((PacketPlayOutChat)packet).message.toString(getVersion())), MessageType.valueOf(((PacketPlayOutChat)packet).type.name()));
		}
		if (packet instanceof PacketPlayOutPlayerListHeaderFooter) {
			player.tabList().setHeaderAndFooter(Main.stringToComponent(((PacketPlayOutPlayerListHeaderFooter) packet).header.toString(getVersion())), Main.stringToComponent(((PacketPlayOutPlayerListHeaderFooter) packet).footer.toString(getVersion())));
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
				bossbars.remove(boss.id);
				player.hideBossBar(bossbars.get(boss.id));
				break;
			case UPDATE_PCT:
				bossbars.get(boss.id).progress(boss.pct);
				break;
			case UPDATE_NAME:
				bossbars.get(boss.id).name(Main.stringToComponent(IChatBaseComponent.optimizedComponent(boss.name).toString(getVersion())));
				break;
			case UPDATE_STYLE:
				bossbars.get(boss.id).overlay(Overlay.valueOf(boss.overlay.toString()));
				bossbars.get(boss.id).color(Color.valueOf(boss.color.toString()));
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
	}
	
	@Override
	public Object getSkin() {
		return player.gameProfile().getProperties();
	}
	
	@Override
	public Player getPlayer() {
		return player;
	}
	
	@Override
	public UUID getTablistUUID() {
		return TAB.getInstance().getConfiguration().config.getBoolean("use-online-uuid-in-tablist", true) ? uniqueId : offlineId;
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
}