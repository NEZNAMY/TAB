package me.neznamy.tab.shared.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PluginMessageHandler;

public abstract class ProxyTabPlayer extends ITabPlayer {

	private final PluginMessageHandler plm;
	private final Map<String, String> attributes = new HashMap<>();
	
	protected ProxyTabPlayer(PluginMessageHandler plm, Object player, UUID uniqueId, String name, String server, String world) {
		super(player, uniqueId, name, server, world);
		this.plm = plm;
	}
	
	@Override
	public boolean isVanished() {
		return Boolean.parseBoolean(getAttribute("vanished", false));
	}
	
	@Override
	public boolean isDisguised() {
		return Boolean.parseBoolean(getAttribute("disguised", false));
	}
	
	@Override
	public boolean hasInvisibilityPotion() {
		return Boolean.parseBoolean(getAttribute("invisible", false));
	}

	public String getAttribute(String name, Object def) {
		plm.requestAttribute(this, name);
		return attributes.getOrDefault(name, def.toString());
	}
	
	@Override
	public boolean hasPermission(String permission) {
		if (TAB.getInstance().getConfiguration().isBukkitPermissions()) {
			String merge = "hasPermission:" + permission;
			plm.requestAttribute(this, merge);
			return Boolean.parseBoolean(attributes.getOrDefault(merge, "false"));
		}
		return hasPermission0(permission);
	}
	
	public void setAttribute(String attribute, String value) {
		attributes.put(attribute, value);
	}
	
	public PluginMessageHandler getPluginMessageHandler() {
		return plm;
	}
	
	public abstract boolean hasPermission0(String permission);
}