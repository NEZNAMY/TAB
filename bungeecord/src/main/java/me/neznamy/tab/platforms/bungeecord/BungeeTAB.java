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
        if (isCompatible()) {
            TAB.create(new BungeePlatform(this));
        } else {
            logIncompatibleVersionWarning();
        }
    }

    private boolean isCompatible() {
        return ReflectionUtils.classExists("net.md_5.bungee.api.chat.ObjectComponent");
    }

    private void logIncompatibleVersionWarning() {
        int buildNumber = 2000;
        String releaseDate = "September 30th, 2025";
        String oldTabVersion = "5.2.5";
        getLogger().warning("§c====================================================================================================");
        getLogger().warning(String.format("§cThe plugin requires BungeeCord build #%d (released on %s) and up (or an equivalent fork) to work.", buildNumber, releaseDate));
        getLogger().warning(String.format("§cIf you are using a fork that did not update to the new BungeeCord version yet, stay on TAB v%s, which supports older builds.", oldTabVersion));
        getLogger().warning("§c====================================================================================================");
    }

    @Override
    public void onDisable() {
        if (TAB.getInstance() != null) TAB.getInstance().unload();
    }
}