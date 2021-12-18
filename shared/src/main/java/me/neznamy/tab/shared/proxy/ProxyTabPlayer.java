package me.neznamy.tab.shared.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PluginMessageHandler;

public abstract class ProxyTabPlayer extends ITabPlayer {

	private Map<String, String> attributes = new HashMap<>();
	private final Map<String, Long> cooldowns = new HashMap<>();
	
	protected ProxyTabPlayer(Object player, UUID uniqueId, String name, String server) {
		super(player, uniqueId, name, server, "N/A");
		getPluginMessageHandler().requestAttribute(this, "world");
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
		if (!cooldowns.containsKey(name) || System.currentTimeMillis() - cooldowns.get(name) > 1000){
			cooldowns.put(name, System.currentTimeMillis());
			getPluginMessageHandler().requestAttribute(this, name);
		}
		return getAttributes().getOrDefault(name, def.toString());
	}
	
	@Override
	public boolean hasPermission(String permission) {
		if (TAB.getInstance().getConfiguration().isBukkitPermissions()) {
			String merge = "hasPermission:" + permission;
			getPluginMessageHandler().requestAttribute(this, merge);
			return Boolean.parseBoolean(getAttributes().getOrDefault(merge, "false"));
		}
		return hasPermission0(permission);
	}
	
	public void setAttribute(String attribute, String value) {
		getAttributes().put(attribute, value);
	}
	
	public PluginMessageHandler getPluginMessageHandler() {
		return ((ProxyPlatform)TAB.getInstance().getPlatform()).getPluginMessageHandler();
	}

	private Map<String, String> getAttributes() {
		if (attributes == null) attributes = new HashMap<>(); //called by superclass before initialized in constructor
		return attributes;
	}
	
	public abstract boolean hasPermission0(String permission);
}