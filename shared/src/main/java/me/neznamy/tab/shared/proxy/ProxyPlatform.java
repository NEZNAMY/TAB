package me.neznamy.tab.shared.proxy;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.PluginMessageHandler;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.globalplayerlist.GlobalPlayerList;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;

public abstract class ProxyPlatform implements Platform {

	protected final PluginMessageHandler plm;
	
	protected ProxyPlatform(PluginMessageHandler plm) {
		this.plm = plm;
	}
	
	@Override
	public void registerUnknownPlaceholder(String identifier) {
		PlaceholderManagerImpl pl = TAB.getInstance().getPlaceholderManager();
		Placeholder p;
		if (identifier.startsWith("%rel_")) {
			p = new RelationalPlaceholderImpl(identifier, pl.getRelationalRefresh(identifier), (viewer, target) -> ""); //bridge does not support relational placeholders yet
		} else {
			int refresh = pl.getPlayerPlaceholderRefreshIntervals().getOrDefault(identifier, pl.getServerPlaceholderRefreshIntervals().getOrDefault(identifier, pl.getDefaultRefresh()));
			p = new PlayerPlaceholderImpl(identifier, refresh, null) {

				@Override
				public String request(TabPlayer p) {
					plm.requestPlaceholder(p, identifier);
					return null;
				}
			};
		}
		pl.registerPlaceholder(p);
	}
	
	@Override
	public void loadFeatures() {
		TAB tab = TAB.getInstance();
		new UniversalPlaceholderRegistry().registerPlaceholders(tab.getPlaceholderManager());
		if (tab.getConfiguration().getConfig().getBoolean("scoreboard-teams.enabled", true))
			tab.getFeatureManager().registerFeature(TabConstants.Feature.NAME_TAGS, new NameTag());
		tab.loadUniversalFeatures();
		if (tab.getConfiguration().getConfig().getBoolean("bossbar.enabled", false))
			tab.getFeatureManager().registerFeature(TabConstants.Feature.BOSS_BAR, new BossBarManagerImpl());
		if (tab.getConfiguration().getConfig().getBoolean("global-playerlist.enabled", false))
			tab.getFeatureManager().registerFeature(TabConstants.Feature.GLOBAL_PLAYER_LIST, new GlobalPlayerList());
	}

	@Override
	public String getConfigName() {
		return "proxyconfig.yml";
	}

	public PluginMessageHandler getPluginMessageHandler() {
		return plm;
	}
}
