package me.neznamy.tab.shared.hook;

import com.viaversion.viaversion.api.Via;
import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.SimpleComponent;
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

    /**
     * Prints warn into console when ViaVersion is installed on BungeeCord.
     * Surprisingly, the issue with wrong protocol version is not present on Velocity.
     */
    public void printProxyWarn() {
        if (!installed) return;
        TAB.getInstance().getPlatform().logWarn(new SimpleComponent("Detected plugin ViaVersion, which when installed on BungeeCord acts like a" +
                " client-sided protocol hack, making it impossible for TAB to properly detect player version and causing issues " +
                "if client and server versions don't match, which include, but are not limited to:"));
        TAB.getInstance().getPlatform().logWarn(new SimpleComponent("#1 - NameTag prefix/suffix being cut to 16 characters even for 1.13+ players"));
        TAB.getInstance().getPlatform().logWarn(new SimpleComponent("#2 - Scoreboard lines being cut to 26 characters even for 1.13+ players"));
        TAB.getInstance().getPlatform().logWarn(new SimpleComponent("#3 - Scoreboard lines being cut to 14 characters for <1.13"));
        TAB.getInstance().getPlatform().logWarn(new SimpleComponent("#4 - Scoreboard lines might be out of order when using all 0s"));
        TAB.getInstance().getPlatform().logWarn(new SimpleComponent("#5 - RGB colors will display as legacy colors even for 1.16+ players"));
        TAB.getInstance().getPlatform().logWarn(new SimpleComponent("#6 - Layout entries will be in random order for 1.19.3+ players"));
        TAB.getInstance().getPlatform().logWarn(new SimpleComponent("Please install ViaVersion on all backend servers instead."));
    }
}
