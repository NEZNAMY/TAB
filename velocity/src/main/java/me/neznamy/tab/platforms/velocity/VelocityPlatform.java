package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.Player;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.proxy.ProxyPlatform;

import java.util.Locale;

/**
 * Velocity implementation of Platform
 */
public class VelocityPlatform extends ProxyPlatform {

    /**
     * Constructs new instance with default packet builder
     */
    public VelocityPlatform() {
        super(new VelocityPacketBuilder());
    }

    @Override
    public void loadFeatures() {
        TAB tab = TAB.getInstance();
        if (tab.getConfiguration().isPipelineInjection())
            tab.getFeatureManager().registerFeature(TabConstants.Feature.PIPELINE_INJECTION, new VelocityPipelineInjector());
        super.loadFeatures();
        for (Player p : Main.getInstance().getServer().getAllPlayers()) {
            TAB.getInstance().addPlayer(new VelocityTabPlayer(p));
        }
    }

    @Override
    public String getPluginVersion(String plugin) {
        return Main.getInstance().getServer().getPluginManager().getPlugin(plugin.toLowerCase(Locale.US))
                .flatMap(pluginContainer -> pluginContainer.getDescription().getVersion()).orElse(null);
    }
}