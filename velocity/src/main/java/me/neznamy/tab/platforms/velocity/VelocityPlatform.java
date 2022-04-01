package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.Player;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.proxy.ProxyPlatform;

import java.util.Locale;

/**
 * Velocity implementation of Platform
 */
public class VelocityPlatform extends ProxyPlatform {

    /**
     * Constructs new instance
     */
    public VelocityPlatform() {
        super(new PacketBuilder());
    }

    @Override
    public void loadFeatures() {
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