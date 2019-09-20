package me.neznamy.tab.platforms.bungee;

import java.lang.reflect.Field;

import me.lucko.luckperms.LuckPerms;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import net.alpenblock.bungeeperms.BungeePerms;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Action;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

public class TabPlayer extends ITabPlayer{

	public ProxiedPlayer player;

	public TabPlayer(ProxiedPlayer p) throws Exception {
		player = p;
		world = p.getServer().getInfo().getName();
		channel = ((ChannelWrapper) wrapperField.get(player.getPendingConnection())).getHandle();
		tablistId = p.getUniqueId();
		init(p.getName(), p.getUniqueId());
		version = ProtocolVersion.fromNumber(player.getPendingConnection().getVersion());
	}
	public String getGroupFromPermPlugin() {
		if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) return LuckPerms.getApi().getUser(player.getUniqueId()).getPrimaryGroup();
		if (ProxyServer.getInstance().getPluginManager().getPlugin("BungeePerms") != null) return BungeePerms.getInstance().getPermissionsManager().getMainGroup(BungeePerms.getInstance().getPermissionsManager().getUser(player.getUniqueId())).getName();
		return player.getGroups().toArray(new String[0])[0];
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
		if (nmsPacket != null) player.unsafe().sendPacket((DefinedPacket) nmsPacket);
	}
	public void setPlayerListName() {
		Item playerInfoData = new Item();
		playerInfoData.setDisplayName((String) Shared.mainClass.createComponent(getName()));
		playerInfoData.setUsername(getName());
		playerInfoData.setUuid(getTablistId());
		PlayerListItem packet = new PlayerListItem();
		packet.setAction(Action.UPDATE_DISPLAY_NAME);
		packet.setItems(new Item[] {playerInfoData});
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all.getVersion().getNetworkId() >= ProtocolVersion.v1_8.getNetworkId()) all.sendPacket(packet);
		}
	}
	@SuppressWarnings("deprecation")
	public void sendMessage(String message) {
		if (message == null || message.length() == 0) return;
		player.sendMessage(message);
	}
	public boolean getTeamPush() {
		return Configs.collision;
	}
	private static Field wrapperField = PacketPlayOut.getFields(InitialHandler.class).get("ch");
}