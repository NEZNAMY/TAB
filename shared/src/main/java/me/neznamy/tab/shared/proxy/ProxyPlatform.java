package me.neznamy.tab.shared.proxy;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.PluginMessageHandler;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;

public abstract class ProxyPlatform implements Platform {

	protected PluginMessageHandler plm;
	
	protected ProxyPlatform(PluginMessageHandler plm) {
		this.plm = plm;
	}
	
	@Override
	public void registerUnknownPlaceholder(String identifier) {
		TAB.getInstance().debug("Detected used PlaceholderAPI placeholder " + identifier);
		PlaceholderManagerImpl pl = TAB.getInstance().getPlaceholderManager();
		int refresh = pl.getDefaultRefresh();
		if (pl.getPlayerPlaceholderRefreshIntervals().containsKey(identifier)) refresh = pl.getPlayerPlaceholderRefreshIntervals().get(identifier);
		if (pl.getServerPlaceholderRefreshIntervals().containsKey(identifier)) refresh = pl.getServerPlaceholderRefreshIntervals().get(identifier);
		Placeholder p = new PlayerPlaceholder(identifier, TAB.getInstance().getErrorManager().fixPlaceholderInterval(identifier, refresh)){
			
			public Object get(TabPlayer p) {
				plm.requestPlaceholder(p, identifier);
				return getLastValues().get(p.getName());
			}
		};
		pl.registerPlaceholder(p);
	}
}
