package me.neznamy.tab.platforms.velocity;

import java.util.UUID;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.PlayerListItem;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import net.kyori.text.TextComponent;

public class TabPlayer extends ITabPlayer{

	public Player player;

	public TabPlayer(Player p, String server) {
		player = p;
		world = server;
		channel = ((ConnectedPlayer)player).getConnection().getChannel();
		tablistId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + p.getUsername()).getBytes(Charsets.UTF_8));
		init(p.getUsername(), p.getUniqueId());
		version = ProtocolVersion.fromNumber(player.getProtocolVersion().getProtocol());
	}
	public String getGroupFromPermPlugin() {
		if (Main.server.getPluginManager().getPlugin("LuckPerms").isPresent()) return PluginHooks.LuckPerms_getPrimaryGroup(this);
		return null;
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
	public void setPlayerListName() {
		PlayerListItem.Item playerInfoData = new PlayerListItem.Item(getTablistId()).setDisplayName(TextComponent.of(getName())).setName(getName());
		PlayerListItem packet = new PlayerListItem(3, Lists.newArrayList(playerInfoData));
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all.getVersion().getNetworkId() >= ProtocolVersion.v1_8.getNetworkId()) all.sendPacket(packet);
		}
	}
	public void sendMessage(String message) {
		if (message == null || message.length() == 0) return;
		player.sendMessage(TextComponent.of(message));
	}
	public boolean getTeamPush() {
		return Configs.collision;
	}
}