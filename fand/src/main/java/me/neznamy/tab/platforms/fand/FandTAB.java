package me.neznamy.tab.platforms.fand;

import io.fand.api.plugin.Plugin;
import io.fand.api.plugin.PluginContext;
import me.neznamy.tab.shared.TAB;

/** Fand plugin entry point. */
public final class FandTAB implements Plugin {

    @Override
    public void onEnable(PluginContext context) {
        TAB.create(new FandPlatform(context));
    }

    @Override
    public void onDisable(PluginContext context) {
        if (TAB.getInstance() != null) {
            TAB.getInstance().unload();
        }
    }
}
