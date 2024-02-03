package me.neznamy.tab.shared.hook;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Class for hooking into floodgate to detect bedrock players and
 * adapt features for them for best experience.
 */
public class FloodgateHook {

    /** Instance of the class */
    @Getter private static final FloodgateHook instance = new FloodgateHook();

    /** Flag tracking if floodgate is installed or not */
    private final boolean installed = ReflectionUtils.classExists("org.geysermc.floodgate.api.FloodgateApi");

    /**
     * Returns {@code true} if this player is a bedrock player,
     * {@code false} if not.
     *
     * @param   uniqueId
     *          UUID of player
     * @param   name
     *          Player's name, used in debug message
     * @return  {@code true} if bedrock player, {@code false} if not
     */
    public boolean isFloodgatePlayer(@NotNull UUID uniqueId, @NotNull String name) {
        if (!installed) return false;
        if (FloodgateApi.getInstance() == null) {
            TAB.getInstance().debug("Floodgate is installed, but API returned null. Could not check player " + name);
            return false;
        }
        boolean bedrock = FloodgateApi.getInstance().isFloodgatePlayer(uniqueId);
        TAB.getInstance().debug("Floodgate returned bedrock status " + String.valueOf(bedrock).toUpperCase() + " for player " + name);
        char firstCharacter = name.charAt(0);
        boolean validFirstChar = (firstCharacter >= 'A' && firstCharacter <= 'Z') ||
                (firstCharacter >= 'a' && firstCharacter <= 'z') ||
                (firstCharacter >= '0' && firstCharacter <= '9') ||
                (firstCharacter == '_');
        if (!bedrock && !validFirstChar) {
            TAB.getInstance().getPlatform().logWarn(new SimpleComponent("Floodgate returned bedrock status FALSE " +
                    "for player " + name + ", however, this player appears to be a bedrock player. This means " +
                    "floodgate is not configured correctly, usually because it is also installed on proxy, but not " +
                    "linked properly. See proxy setup on floodgate wiki for more details. This will result in visual issues for the " +
                    "player, most notably scoreboard lines being out of order and more."));
        }

        return bedrock;
    }
}
