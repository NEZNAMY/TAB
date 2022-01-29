package me.neznamy.tab.shared.proxy;

import com.google.common.collect.Lists;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PluginMessageHandler;
import me.neznamy.tab.shared.permission.VaultBridge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class ProxyTabPlayer extends ITabPlayer {

	private boolean vanished;
	private boolean disguised;
	private boolean invisible;
	private final Map<String, Boolean> permissions = new HashMap<>();
	
	protected ProxyTabPlayer(Object player, UUID uniqueId, String name, String server, int protocolVersion) {
		super(player, uniqueId, name, server, "N/A", protocolVersion);
		List<Object> args = Lists.newArrayList("PlayerJoin", TAB.getInstance().getGroupManager().getPlugin() instanceof VaultBridge,
				TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PET_FIX));
		ProxyPlatform platform = (ProxyPlatform) TAB.getInstance().getPlatform();
		Map<String, Integer> placeholders = platform.getBridgePlaceholders();
		args.add(placeholders.size());
		for (Map.Entry<String, Integer> entry : placeholders.entrySet()) {
			args.add(entry.getKey());
			args.add(entry.getValue());
		}
		getPluginMessageHandler().sendMessage(this, args.toArray());
	}
	
	@Override
	public boolean isVanished() {
		return vanished;
	}

	public void setVanished(boolean vanished) {
		this.vanished = vanished;
	}
	
	@Override
	public boolean isDisguised() {
		return disguised;
	}

	public void setDisguised(boolean disguised) {
		this.disguised = disguised;
	}
	
	@Override
	public boolean hasInvisibilityPotion() {
		return invisible;
	}

	public void setInvisible(boolean invisible) {
		this.invisible = invisible;
	}

	@Override
	public boolean hasPermission(String permission) {
		if (TAB.getInstance().getConfiguration().isBukkitPermissions()) {
			getPluginMessageHandler().sendMessage(this, "Permission", permission);
			return permissions.getOrDefault(permission, false);
		}
		return hasPermission0(permission);
	}

	public void setHasPermission(String permission, boolean value) {
		permissions.put(permission, value);
	}

	public PluginMessageHandler getPluginMessageHandler() {
		return ((ProxyPlatform)TAB.getInstance().getPlatform()).getPluginMessageHandler();
	}
	
	public abstract boolean hasPermission0(String permission);
}