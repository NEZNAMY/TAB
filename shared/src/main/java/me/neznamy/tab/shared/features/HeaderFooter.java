package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.HeaderFooterManager;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.shared.PropertyUtils;
import me.neznamy.tab.shared.TAB;

/**
 * Feature handler for header and footer
 */
public class HeaderFooter extends TabFeature implements HeaderFooterManager {
	
	public HeaderFooter() {
		super("Header/Footer", TAB.getInstance().getConfiguration().getConfig().getStringList("header-footer.disable-in-servers"),
				TAB.getInstance().getConfiguration().getConfig().getStringList("header-footer.disable-in-worlds"));
		TAB.getInstance().debug(String.format("Loaded HeaderFooter feature with parameters disabledWorlds=%s, disabledServers=%s", Arrays.toString(disabledWorlds), Arrays.toString(disabledServers)));
	}
	
	@Override
	public void load() {
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			onJoin(p);
		}
	}
	
	@Override
	public void unload() {
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			if (disabledPlayers.contains(p) || p.getVersion().getMinorVersion() < 8) continue;
			p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter("",""), this);
		}
	}
	
	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		if (isDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld())) {
			disabledPlayers.add(connectedPlayer);
			return;
		}
		refresh(connectedPlayer, true);
	}
	
	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		boolean disabledBefore = disabledPlayers.contains(p);
		boolean disabledNow = false;
		if (isDisabled(p.getServer(), p.getWorld())) {
			disabledNow = true;
			disabledPlayers.add(p);
		} else {
			disabledPlayers.remove(p);
		}
		if (p.getVersion().getMinorVersion() < 8) return;
		if (disabledNow) {
			if (!disabledBefore) p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter("", ""), this);
		} else {
			if (disabledBefore) {
				refresh(p, true);
				return;
			}
			boolean refresh = false;
			if (p.setProperty(this, PropertyUtils.HEADER, getProperty(p, PropertyUtils.HEADER))) {
				refresh = true;
			}
			if (p.setProperty(this, PropertyUtils.FOOTER, getProperty(p, PropertyUtils.FOOTER))) {
				refresh = true;
			}
			if (refresh) {
				p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(p.getProperty(PropertyUtils.HEADER).get(), p.getProperty(PropertyUtils.FOOTER).get()), this);
			}
		}
	}

	@Override
	public void refresh(TabPlayer p, boolean force) {
		if (force) {
			p.setProperty(this, PropertyUtils.HEADER, getProperty(p, PropertyUtils.HEADER));
			p.setProperty(this, PropertyUtils.FOOTER, getProperty(p, PropertyUtils.FOOTER));
		}
		if (disabledPlayers.contains(p) || p.getVersion().getMinorVersion() < 8) return;
		p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(p.getProperty(PropertyUtils.HEADER).updateAndGet(), p.getProperty(PropertyUtils.FOOTER).updateAndGet()), this);
	}

	private String getProperty(TabPlayer p, String property) {
		String append = getFromConfig(p, property + "append");
		if (append.length() > 0) append = "\n" + EnumChatFormat.COLOR_CHAR + "r" + append;
		return getFromConfig(p, property) + append;
	}
	
	private String getFromConfig(TabPlayer p, String property) {
		String[] value = TAB.getInstance().getConfiguration().getUsers().getProperty(p.getName(), property, p.getServer(), p.getWorld());
		if (value.length > 0) {
			return value[0];
		}
		value = TAB.getInstance().getConfiguration().getUsers().getProperty(p.getUniqueId().toString(), property, p.getServer(), p.getWorld());
		if (value.length > 0) {
			return value[0];
		}
		value = TAB.getInstance().getConfiguration().getGroups().getProperty(p.getGroup(), property, p.getServer(), p.getWorld());
		if (value.length > 0) {
			return value[0];
		}
		List<String> lines = TAB.getInstance().getConfiguration().getConfig().getStringList("header-footer.per-server." + p.getServer() + "." + property);
		if (lines == null) {
			lines = TAB.getInstance().getConfiguration().getConfig().getStringList("header-footer.per-world." + p.getWorld() + "." + property);
		}
		if (lines == null) {
			 lines = TAB.getInstance().getConfiguration().getConfig().getStringList("header-footer." + property);
		}
		if (lines == null) lines = new ArrayList<>();
		return String.join("\n" + EnumChatFormat.COLOR_CHAR + "r", lines);
	}

	@Override
	public void setHeader(TabPlayer player, String header) {
		player.getProperty(PropertyUtils.HEADER).setTemporaryValue(header);
		player.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(player.getProperty(PropertyUtils.HEADER).updateAndGet(), player.getProperty(PropertyUtils.FOOTER).updateAndGet()), this);
	}

	@Override
	public void setFooter(TabPlayer player, String footer) {
		player.getProperty(PropertyUtils.FOOTER).setTemporaryValue(footer);
		player.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(player.getProperty(PropertyUtils.HEADER).updateAndGet(), player.getProperty(PropertyUtils.FOOTER).updateAndGet()), this);
	}

	@Override
	public void setHeaderAndFooter(TabPlayer player, String header, String footer) {
		player.getProperty(PropertyUtils.HEADER).setTemporaryValue(header);
		player.getProperty(PropertyUtils.FOOTER).setTemporaryValue(footer);
		player.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(player.getProperty(PropertyUtils.HEADER).updateAndGet(), player.getProperty(PropertyUtils.FOOTER).updateAndGet()), this);
	}

	@Override
	public void resetHeader(TabPlayer player) {
		player.getProperty(PropertyUtils.HEADER).setTemporaryValue(null);
		player.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(player.getProperty(PropertyUtils.HEADER).updateAndGet(), player.getProperty(PropertyUtils.FOOTER).updateAndGet()), this);
	}

	@Override
	public void resetFooter(TabPlayer player) {
		player.getProperty(PropertyUtils.FOOTER).setTemporaryValue(null);
		player.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(player.getProperty(PropertyUtils.HEADER).updateAndGet(), player.getProperty(PropertyUtils.FOOTER).updateAndGet()), this);
	}

	@Override
	public void resetHeaderAndFooter(TabPlayer player) {
		player.getProperty(PropertyUtils.HEADER).setTemporaryValue(null);
		player.getProperty(PropertyUtils.FOOTER).setTemporaryValue(null);
		player.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(player.getProperty(PropertyUtils.HEADER).updateAndGet(), player.getProperty(PropertyUtils.FOOTER).updateAndGet()), this);
	}
}