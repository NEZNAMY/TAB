package me.neznamy.tab.platforms.velocity;

import java.util.UUID;

import com.google.common.base.Charsets;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;

import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.placeholders.Placeholders;
import net.kyori.text.TextComponent;

public class TabPlayer extends ITabPlayer{

	public Player player;

	public TabPlayer(Player p, String server) {
		player = p;
		world = server;
		channel = ((ConnectedPlayer)player).getConnection().getChannel();
		tablistId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + p.getUsername()).getBytes(Charsets.UTF_8));
		uniqueId = p.getUniqueId();
		name = p.getUsername();
		version = ProtocolVersion.fromNumber(player.getProtocolVersion().getProtocol());
		init();
	}
	public String getGroupFromPermPlugin() {
		if (Main.server.getPluginManager().getPlugin("LuckPerms").isPresent()) return PluginHooks.LuckPerms_getPrimaryGroup(this);
		return "null";
	}
	public String[] getGroupsFromPermPlugin() {
		return new String[] {getGroupFromPermPlugin()};
	}
	public boolean hasPermission(String permission) {
		return player.hasPermission(permission);
	}
	public long getPing() {
		return player.getPing();
	}
	public void sendPacket(Object nmsPacket) {
		if (nmsPacket != null) ((ConnectedPlayer)player).getConnection().write(nmsPacket);
	}
	public void sendMessage(String message) {
		if (message == null || message.length() == 0) return;
		player.sendMessage(TextComponent.of(Placeholders.color(message)));
	}
	@Override
	public Object getSkin() {
		return player.getGameProfile().getProperties();
	}
}