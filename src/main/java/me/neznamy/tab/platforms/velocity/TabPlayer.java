package me.neznamy.tab.platforms.velocity;

import java.util.UUID;

import com.google.common.base.Charsets;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.placeholders.Placeholders;
import net.kyori.adventure.text.TextComponent;

public class TabPlayer extends ITabPlayer{

	private Player player;

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
	@Override
	public boolean hasPermission(String permission) {
		return player.hasPermission(permission);
	}
	@Override
	public long getPing() {
		return player.getPing();
	}
	@Override
	public void sendPacket(Object nmsPacket) {
		if (nmsPacket != null) ((ConnectedPlayer)player).getConnection().write(nmsPacket);
	}
	@Override
	public void sendMessage(String message) {
		if (message == null || message.length() == 0) return;
		player.sendMessage(TextComponent.of(Placeholders.color(message)));
	}
	@Override
	public void sendRawMessage(String message) {
		if (message == null || message.length() == 0) return;
		player.sendMessage(TextComponent.of(message));
	}
	@Override
	public Object getSkin() {
		return player.getGameProfile().getProperties();
	}
	@Override
	public Player getVelocityEntity() {
		return player;
	}
}