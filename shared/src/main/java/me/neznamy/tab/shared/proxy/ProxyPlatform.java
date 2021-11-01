package me.neznamy.tab.shared.proxy;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.NameTag;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.PluginMessageHandler;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.globalplayerlist.GlobalPlayerlist;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;

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
		Placeholder p = new PlayerPlaceholder(identifier, refresh, null) {
			
			@Override
			public Object get(TabPlayer p) {
				plm.requestPlaceholder(p, identifier);
				return getLastValues().get(p.getName());
			}
		};
		pl.registerPlaceholder(p);
	}
	
	@Override
	public void loadFeatures() {
		TAB tab = TAB.getInstance();
		new UniversalPlaceholderRegistry().registerPlaceholders(tab.getPlaceholderManager());
		if (tab.getConfiguration().getConfig().getBoolean("scoreboard-teams.enabled", true)) tab.getFeatureManager().registerFeature("nametag16", new NameTag());
		tab.loadUniversalFeatures();
		if (tab.getConfiguration().getConfig().getBoolean("bossbar.enabled", false)) tab.getFeatureManager().registerFeature("bossbar", new BossBarManagerImpl());
		if (tab.getConfiguration().getConfig().getBoolean("global-playerlist.enabled", false)) 	tab.getFeatureManager().registerFeature("globalplayerlist", new GlobalPlayerlist());
	}
}
