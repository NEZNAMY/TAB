package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.bossbar.bungee.BungeeBossBarAPI;
import me.neznamy.bossbar.shared.BossBarAPI;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Main class for BungeeCord.
 */
public class BungeeTAB extends Plugin {

    @Override
    public void onEnable() {
        if (!ReflectionUtils.classExists("net.md_5.bungee.protocol.packet.Team$NameTagVisibility")) {
            getLogger().warning("§c====================================================================================================");
            getLogger().warning("§cThe plugin requires BungeeCord build #1899 " +
                    "(released on February 1st, 2025) and up (or an equivalent fork) to work. If you are using a fork that did not" +
                    " update to the new BungeeCord version yet, stay on TAB v5.0.5, which supports older builds.");
            getLogger().warning("§c====================================================================================================");
            return;
        }
        BossBarAPI.setInstance(new BungeeBossBarAPI(this));
        TAB.create(new BungeePlatform(this));
    }

    @Override
    public void onDisable() {
        if (TAB.getInstance() != null) TAB.getInstance().unload();
    }
}