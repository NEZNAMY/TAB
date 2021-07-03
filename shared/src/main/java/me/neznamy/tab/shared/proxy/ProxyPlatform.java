package me.neznamy.tab.shared.proxy;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.features.PluginMessageHandler;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;

public abstract class ProxyPlatform implements Platform {

	protected PluginMessageHandler plm;
	
	protected ProxyPlatform(PluginMessageHandler plm) {
		this.plm = plm;
	}
	
	@Override
	public void registerUnknownPlaceholder(String identifier) {
		if (identifier.startsWith("%rel_")) return;
		if (identifier.contains("_")) {
			String expansion = identifier.split("_")[0].replace("%", "").toLowerCase();
			if (expansion.equals("some")) return;
			TAB.getInstance().debug("Detected used PlaceholderAPI placeholder " + identifier);
			PlaceholderManager pl = TAB.getInstance().getPlaceholderManager();
			int refresh = pl.getDefaultRefresh();
			if (pl.getPlayerPlaceholderRefreshIntervals().containsKey(identifier)) refresh = pl.getPlayerPlaceholderRefreshIntervals().get(identifier);
			if (pl.getServerPlaceholderRefreshIntervals().containsKey(identifier)) refresh = pl.getServerPlaceholderRefreshIntervals().get(identifier);
			TAB.getInstance().getPlaceholderManager().registerPlaceholder(new PlayerPlaceholder(identifier, TAB.getInstance().getErrorManager().fixPlaceholderInterval(identifier, refresh)){
				public String get(TabPlayer p) {
					plm.requestPlaceholder(p, identifier);
					return getLastValues().get(p.getName());
				}
			});
		}
	}
}
