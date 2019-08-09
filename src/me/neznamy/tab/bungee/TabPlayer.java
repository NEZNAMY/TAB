package me.neznamy.tab.bungee;

import java.lang.reflect.Field;
import java.util.UUID;

import me.lucko.luckperms.LuckPerms;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import net.alpenblock.bungeeperms.BungeePerms;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Action;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

public class TabPlayer extends ITabPlayer{

	public Server server;

	public TabPlayer(ProxiedPlayer p) {
		super(p);
		version = getPlayer().getPendingConnection().getVersion();
		ipAddress = p.getAddress().getAddress().getHostAddress();
		disabledHeaderFooter = Configs.disabledHeaderFooter.contains(getWorldName());
		disabledTablistNames = Configs.disabledTablistNames.contains(getWorldName());
		disabledNametag = Configs.disabledNametag.contains(getWorldName());
		disabledTablistObjective = Configs.disabledTablistObjective.contains(getWorldName());
		disabledBossbar = Configs.disabledBossbar.contains(getWorldName());
	}
	public String getGroupFromPermPlugin() {
		if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) {
			return LuckPerms.getApi().getUser(getPlayer().getUniqueId()).getPrimaryGroup();
		}
		if (ProxyServer.getInstance().getPluginManager().getPlugin("BungeePerms") != null) {
			return BungeePerms.getInstance().getPermissionsManager().getMainGroup(BungeePerms.getInstance().getPermissionsManager().getUser(getPlayer().getUniqueId())).getName();
		}
		return getPlayer().getGroups().toArray(new String[0])[0];
	}
	public String[] getGroupsFromPermPlugin() {
		return new String[] {getGroupFromPermPlugin()};
	}
	public ProxiedPlayer getPlayer() {
		return (ProxiedPlayer) player;
	}
	public String getName() {
		return getPlayer().getName();
	}
	public UUID getUniqueId() {
		return getPlayer().getUniqueId();
	}
	public String getWorldName() {
		if (server == null) server = getPlayer().getServer(); //no other effective way to initialize
		return server.getInfo().getName();
	}
	public boolean hasPermission(String permission) {
		return getPlayer().hasPermission(permission);
	}
	public long getPing() {
		return getPlayer().getPing();
	}
	public void sendPacket(Object nmsPacket) {
		getPlayer().unsafe().sendPacket((DefinedPacket) nmsPacket);
	}
	public String getNickname() {
		return getName();
	}
	public void onJoin() {
	}
	public String getMoney() {
		return "-";
	}
	public void setTeamVisible(boolean p0) {
	}
	public void restartArmorStands() {
	}
	public Integer getEntityId() {
		return 0;
	}
	public int getHealth() {
		return 0;
	}
	public void setPlayerListName() {
		Item playerInfoData = new Item();
		playerInfoData.setDisplayName((String) Shared.mainClass.createComponent(getName()));
		playerInfoData.setUsername(getName());
		playerInfoData.setUuid(getUniqueId());
		PlayerListItem packet = new PlayerListItem();
		packet.setAction(Action.UPDATE_DISPLAY_NAME);
		packet.setItems(new Item[] {playerInfoData});
		for (ITabPlayer all : Shared.getPlayers()) all.sendPacket(packet);
	}
	@SuppressWarnings("deprecation")
	public void sendMessage(String message) {
		if (message == null || message.length() == 0) return;
		getPlayer().sendMessage(message);
	}
	protected void loadChannel() {
		try {
			Field wrapperField = InitialHandler.class.getDeclaredField("ch");
			wrapperField.setAccessible(true);
			channel = ((ChannelWrapper) wrapperField.get(getPlayer().getPendingConnection())).getHandle();
		} catch (Exception e) {
			Shared.error("Failed to get channel of " + getName(), e);
		}
	}
}