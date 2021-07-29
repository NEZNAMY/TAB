package me.neznamy.tab.platforms.velocity;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.api.util.GameProfile.Property;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PluginMessageHandler;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Flag;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.identity.Identity;

/**
 * TabPlayer for Velocity
 */
public class VelocityTabPlayer extends ProxyTabPlayer {

	//the velocity player
	private Player player;
	
	// uuid used in tablist
	private UUID tablistId;
	
	//player's visible boss bars
	private Map<UUID, BossBar> bossbars = new HashMap<>();

	/**
	 * Constructs new instance for given player
	 * @param p - velocity player
	 */
	public VelocityTabPlayer(Player p, PluginMessageHandler plm) {
		super(plm);
		player = p;
		Optional<ServerConnection> server = p.getCurrentServer();
		if (server.isPresent()) {
			world = server.get().getServerInfo().getName();
		} else {
			//tab reload while a player is connecting, how unfortunate
			world = "<null>";
		}
		name = p.getUsername();
		uniqueId = p.getUniqueId();
		UUID offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
		tablistId = TAB.getInstance().getConfiguration().getConfig().getBoolean("use-online-uuid-in-tablist", true) ? uniqueId : offlineId;
		version = ProtocolVersion.fromNetworkId(player.getProtocolVersion().getProtocol());
		init();
	}
	
	@Override
	public boolean hasPermission0(String permission) {
		return player.hasPermission(permission);
	}
	
	@Override
	public long getPing() {
		return player.getPing();
	}
	
	@Override
	public void sendPacket(Object packet) {
		if (packet == null || !player.isActive()) return;
		if (packet instanceof PacketPlayOutChat){
			handle((PacketPlayOutChat) packet);
		}
		if (packet instanceof PacketPlayOutPlayerListHeaderFooter) {
			handle((PacketPlayOutPlayerListHeaderFooter) packet);
		}
		if (packet instanceof PacketPlayOutBoss) {
			handle((PacketPlayOutBoss) packet);
		}
		if (packet instanceof PacketPlayOutPlayerInfo) {
			handle((PacketPlayOutPlayerInfo) packet);
		}
	}

	private void handle(PacketPlayOutChat packet) {
		player.sendMessage(Identity.nil(), Main.stringToComponent(packet.getMessage().toString(getVersion())), MessageType.valueOf(packet.getType().name()));
	}
	
	private void handle(PacketPlayOutPlayerListHeaderFooter packet) {
		player.getTabList().setHeaderAndFooter(Main.stringToComponent(packet.getHeader().toString(getVersion())), Main.stringToComponent(packet.getFooter().toString(getVersion())));
	}
	
	private void handle(PacketPlayOutBoss packet) {
		Set<Flag> flags = new HashSet<>();
		BossBar bar;
		switch (packet.getOperation()) {
		case ADD:
			if (packet.isCreateWorldFog()) flags.add(Flag.CREATE_WORLD_FOG);
			if (packet.isDarkenScreen()) flags.add(Flag.DARKEN_SCREEN);
			if (packet.isPlayMusic()) flags.add(Flag.PLAY_BOSS_MUSIC);
			bar = BossBar.bossBar(Main.stringToComponent(IChatBaseComponent.optimizedComponent(packet.getName()).toString(getVersion())), 
					packet.getPct(), 
					Color.valueOf(packet.getColor().toString()), 
					Overlay.valueOf(packet.getOverlay().toString()), 
					flags);
			bossbars.put(packet.getId(), bar);
			player.showBossBar(bar);
			break;
		case REMOVE:
			player.hideBossBar(bossbars.get(packet.getId()));
			bossbars.remove(packet.getId());
			break;
		case UPDATE_PCT:
			bossbars.get(packet.getId()).percent(packet.getPct());
			break;
		case UPDATE_NAME:
			bossbars.get(packet.getId()).name(Main.stringToComponent(IChatBaseComponent.optimizedComponent(packet.getName()).toString(getVersion())));
			break;
		case UPDATE_STYLE:
			bar = bossbars.get(packet.getId());
			bar.overlay(Overlay.valueOf(packet.getOverlay().toString()));
			bar.color(Color.valueOf(packet.getColor().toString()));
			break;
		case UPDATE_PROPERTIES:
			if (packet.isCreateWorldFog()) flags.add(Flag.CREATE_WORLD_FOG);
			if (packet.isDarkenScreen()) flags.add(Flag.DARKEN_SCREEN);
			if (packet.isPlayMusic()) flags.add(Flag.PLAY_BOSS_MUSIC);
			bossbars.get(packet.getId()).flags(flags);
			break;
		default:
			break;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void handle(PacketPlayOutPlayerInfo packet) {
		for (PlayerInfoData data : packet.getEntries()) {
			switch (packet.getAction()) {
			case ADD_PLAYER:
				if (player.getTabList().containsEntry(data.getUniqueId())) continue;
				player.getTabList().addEntry(TabListEntry.builder()
						.tabList(player.getTabList())
						.displayName(data.getDisplayName() == null ? null : Main.stringToComponent(data.getDisplayName().toString(getVersion())))
						.gameMode(data.getGameMode().ordinal()-1)
						.profile(new GameProfile(data.getUniqueId(), data.getName(), (List<Property>) data.getSkin()))
						.latency(data.getLatency())
						.build());
				break;
			case REMOVE_PLAYER:
				player.getTabList().removeEntry(data.getUniqueId());
				break;
			case UPDATE_DISPLAY_NAME:
				for (TabListEntry entry : player.getTabList().getEntries()) {
					if (entry.getProfile().getId().equals(data.getUniqueId())) entry.setDisplayName(data.getDisplayName() == null ? null : Main.stringToComponent(data.getDisplayName().toString(getVersion())));
				}
				break;
			case UPDATE_LATENCY:
				for (TabListEntry entry : player.getTabList().getEntries()) {
					if (entry.getProfile().getId().equals(data.getUniqueId())) entry.setLatency(data.getLatency());
				}
				break;
			case UPDATE_GAME_MODE:
				for (TabListEntry entry : player.getTabList().getEntries()) {
					if (entry.getProfile().getId().equals(data.getUniqueId())) entry.setGameMode(data.getGameMode().ordinal()-1);
				}
				break;
			default:
				break;
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
	public boolean isOnline() {
		return player.isActive();
	}

	@Override
	public int getGamemode() {
		return 0; //shrug
	}
}