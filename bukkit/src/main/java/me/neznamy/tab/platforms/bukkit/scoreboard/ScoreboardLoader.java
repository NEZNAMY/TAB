package me.neznamy.tab.platforms.bukkit.scoreboard;

import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.platforms.bukkit.scoreboard.packet.PacketScoreboard;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Loader finding the best available Scoreboard implementation.
 */
public class ScoreboardLoader {

    /** Instance function */
    @Getter
    @NotNull
    private static Function<BukkitTabPlayer, Scoreboard<BukkitTabPlayer>> instance = NullScoreboard::new;

    /**
     * Finds the best available instance for current server software.
     */
    public static void findInstance() {
        if (PacketScoreboard.isAvailable()) {
            instance = PacketScoreboard::new;
        } else if (PaperScoreboard.isAvailable()) {
            instance = PaperScoreboard::new;
            BukkitUtils.compatibilityError(PacketScoreboard.getException(), "Scoreboards", "Paper API",
                    "Compatibility with other plugins being reduced",
                    "1.20.3+ visuals not working due to lack of API"); // hopefully only temporarily
        } else if (BukkitScoreboard.isAvailable()) {
            instance = BukkitScoreboard::new;
            BukkitUtils.compatibilityError(PacketScoreboard.getException(), "Scoreboards", "Bukkit API",
                    "Compatibility with other plugins being reduced",
                    "1.20.3+ visuals not working due to lack of API"); // hopefully only temporarily
        } else {
            BukkitUtils.compatibilityError(PacketScoreboard.getException(), "Scoreboards", null,
                    "Scoreboard feature will not work",
                    "Belowname feature will not work",
                    "Player objective feature will not work",
                    "Scoreboard teams feature will not work (nametags & sorting)");
        }
    }
}
