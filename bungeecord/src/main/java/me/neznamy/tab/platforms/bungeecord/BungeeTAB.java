package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.TAB;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Main class for BungeeCord.
 */
public class BungeeTAB extends Plugin {

    @Override
    public void onEnable() {
        TAB.create(new BungeePlatform(this));
    }

    @Override
    public void onDisable() {
        TAB.getInstance().unload();
    }
}