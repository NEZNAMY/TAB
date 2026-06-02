package me.neznamy.tab.platforms.bungeecord;

import com.google.common.base.Function;
import io.netty.buffer.ByteBuf;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.protocol.DefinedPacket;

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
        return ReflectionUtils.methodExists(DefinedPacket.class, "readOptional", Function.class, ByteBuf.class);
    }

    private void logIncompatibleVersionWarning() {
        int buildNumber = 2068;
        String releaseDate = "May 9th, 2026";
        String oldTabVersion = "6.0.2";
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