package me.neznamy.tab.platforms.velocity;

import com.google.common.collect.Lists;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.PlayerListItem;

import me.lucko.luckperms.LuckPerms;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;

public class TabPlayer extends ITabPlayer{

	public Player player;
	public String server;

	public TabPlayer(Player p, String server) {
		player = p;
		this.server = server;
		init(p.getUsername(), p.getUniqueId());
		version = ProtocolVersion.fromNumber(player.getProtocolVersion().getProtocol());
	}
	public String getGroupFromPermPlugin() {
		if (Main.server.getPluginManager().getPlugin("LuckPerms").isPresent()) return LuckPerms.getApi().getUser(player.getUniqueId()).getPrimaryGroup();
		return null;
	}
	public String[] getGroupsFromPermPlugin() {
		return new String[] {getGroupFromPermPlugin()};
	}
	public String getWorldName() {
		return server;
	}
	public boolean hasPermission(String permission) {
		return player.hasPermission(permission);
	}
	public long getPing() {
		return player.getPing();
	}
	public void sendPacket(Object nmsPacket) {
		if (nmsPacket != null) ((ConnectedPlayer)player).getMinecraftConnection().write(nmsPacket);
	}
	public void setPlayerListName() {
		PlayerListItem.Item playerInfoData = new PlayerListItem.Item(getOfflineId()).setDisplayName((Component) Shared.mainClass.createComponent(getName())).setName(getName());
		PlayerListItem packet = new PlayerListItem(3, Lists.newArrayList(playerInfoData));
		for (ITabPlayer all : Shared.getPlayers()) all.sendPacket(packet);
	}
	public void sendMessage(String message) {
		if (message == null || message.length() == 0) return;
		player.sendMessage(TextComponent.of(message));
	}
	protected void loadChannel() {
		channel = ((ConnectedPlayer)player).getMinecraftConnection().getChannel();
	}
	public boolean getTeamPush() {
		return Configs.collision;
	}
}