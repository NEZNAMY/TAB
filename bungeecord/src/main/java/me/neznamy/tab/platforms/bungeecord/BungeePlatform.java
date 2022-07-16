package me.neznamy.tab.platforms.bungeecord;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * BungeeCord implementation of Platform
 */
public class BungeePlatform extends ProxyPlatform {

    /**
     * Constructs new instance
     */
    public BungeePlatform() {
        super(new BungeePacketBuilder());
    }

    @Override
    public void loadFeatures() {
        TAB tab = TAB.getInstance();
        if (tab.getConfiguration().isPipelineInjection())
            tab.getFeatureManager().registerFeature(TabConstants.Feature.PIPELINE_INJECTION, new BungeePipelineInjector());
        tab.getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.DISPLAY_NAME, 500, p -> ((ProxiedPlayer) p.getPlayer()).getDisplayName());
        super.loadFeatures();
        if (ProxyServer.getInstance().getPluginManager().getPlugin(TabConstants.Plugin.REDIS_BUNGEE) != null) {
            if (RedisBungeeAPI.getRedisBungeeApi() != null) {
                tab.getFeatureManager().registerFeature(TabConstants.Feature.REDIS_BUNGEE, new RedisBungeeSupport());
            } else {
                TAB.getInstance().getErrorManager().criticalError("RedisBungee plugin was detected, but it returned null API instance. Disabling hook.", null);
            }
        }
        for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
            tab.addPlayer(new BungeeTabPlayer(p));
        }
    }

    @Override
    public String getPluginVersion(String plugin) {
        Plugin pl = ProxyServer.getInstance().getPluginManager().getPlugin(plugin);
        return pl == null ? null : pl.getDescription().getVersion();
    }
}