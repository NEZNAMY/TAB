package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Main class for BungeeCord.
 */
public class BungeeTAB extends Plugin {

    @Override
    public void onEnable() {
        if (ReflectionUtils.classExists("net.md_5.bungee.protocol.packet.Team$NameTagVisibility")) {
            TAB.create(new BungeePlatform(this));
        } else {
            getLogger().warning("§cThe plugin requires BungeeCord build #1899 " +
                    "(released on February 1st, 2025) and up (or an equivalent fork) to work. If you are using a fork that did not" +
                    " update to the new BungeeCord version yet, stay on TAB v5.0.5, which supports older builds.");
        }
    }

    @Override
    public void onDisable() {
        if (TAB.getInstance() != null) TAB.getInstance().unload();
    }
}