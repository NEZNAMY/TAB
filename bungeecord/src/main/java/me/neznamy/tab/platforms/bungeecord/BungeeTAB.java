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
        String requiredClass = "net.md_5.bungee.protocol.packet.ScoreboardScoreReset";
        if (!ReflectionUtils.classExists(requiredClass)) {
            getLogger().warning(EnumChatFormat.color("&cThe plugin requires BungeeCord build #1774 " +
                    "(released on November 25th, 2023) and up (or an equivalent fork) to work. Compatibility check " +
                    "failed, because required class " + requiredClass + " was not found."));
            return;
        }
        TAB.create(new BungeePlatform(this));
    }

    @Override
    public void onDisable() {
        if (TAB.getInstance() != null) TAB.getInstance().unload();
    }
}