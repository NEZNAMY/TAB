package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Main class for BungeeCord.
 */
public class BungeeTAB extends Plugin {

    @Override
    public void onEnable() {
        if (!ReflectionUtils.classExists("net.md_5.bungee.protocol.Either")) {
            getLogger().warning(EnumChatFormat.color("&cThe plugin requires BungeeCord build #1767 " +
                    "(released on November 6th, 2023) and up (or an equivalent fork) to work."));
            return;
        }
        TAB.create(new BungeePlatform(this));
    }

    @Override
    public void onDisable() {
        if (TAB.getInstance() != null) TAB.getInstance().unload();
    }
}