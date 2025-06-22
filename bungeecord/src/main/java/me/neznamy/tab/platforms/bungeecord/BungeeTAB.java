package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.TAB;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.protocol.Either;
import net.md_5.bungee.protocol.packet.Team;

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
        try {
            return Team.class.getDeclaredField("nameTagVisibility").getType() == Either.class;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private void logIncompatibleVersionWarning() {
        getLogger().warning("§c====================================================================================================");
        getLogger().warning("§cThe plugin requires BungeeCord build #1980 (released on June 21st, 2025) and up (or an equivalent fork) to work.");
        getLogger().warning("§cIf you are using a fork that did not update to the new BungeeCord version yet, stay on TAB v5.2.2, which supports older builds.");
        getLogger().warning("§c====================================================================================================");
    }

    @Override
    public void onDisable() {
        if (TAB.getInstance() != null) TAB.getInstance().unload();
    }
}