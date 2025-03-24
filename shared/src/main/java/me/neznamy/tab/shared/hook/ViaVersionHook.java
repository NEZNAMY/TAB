package me.neznamy.tab.shared.hook;

import com.viaversion.viaversion.api.Via;
import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Class for hooking into ViaVersion to get protocol version of players
 * to adapt visuals for best experience respecting limits of individual versions.
 */
public class ViaVersionHook {

    /** Instance of the class */
    @Getter
    private static final ViaVersionHook instance = new ViaVersionHook();

    /** Flag tracking if ViaVersion is installed or not */
    @Getter
    private final boolean installed = ReflectionUtils.classExists("com.viaversion.viaversion.api.Via");

    /**
     * Gets player's network version using ViaVersion API
     *
     * @param   player
     *          Player's UUID
     * @param   playerName
     *          Player's name for debug messages
     * @param   serverVersion
     *          Server version to return if Via is not installed or something went wrong
     * @return  Player's network version
     */
    public int getPlayerVersion(@NotNull UUID player, @NotNull String playerName, int serverVersion) {
        if (!installed) return serverVersion;
        int version;
        try {
            version = Via.getAPI().getPlayerVersion(player);
        } catch (IllegalArgumentException e) {
            // java.lang.IllegalArgumentException: ViaVersion has not loaded the platform yet
            // Most likely another plugin shading Via API, just ignore it
            return serverVersion;
        }
        if (version == -1) {
            // Player got instantly disconnected with a packet error
            return serverVersion;
        }
        TAB.getInstance().debug("ViaVersion returned protocol version " + version + " for " + playerName);
        return version;
    }
}
