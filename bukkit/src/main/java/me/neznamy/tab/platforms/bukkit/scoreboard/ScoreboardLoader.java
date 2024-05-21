package me.neznamy.tab.platforms.bukkit.scoreboard;

import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.platforms.bukkit.scoreboard.packet.PacketScoreboard;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.impl.DummyScoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Loader finding the best available Scoreboard implementation.
 */
public class ScoreboardLoader {

    /** Instance function */
    @Getter
    @NotNull
    private static Function<BukkitTabPlayer, Scoreboard> instance = DummyScoreboard::new;

    /**
     * Tries to load Scoreboard packets.
     */
    public static void tryLoad() {
        if (PacketScoreboard.isAvailable()) {
            instance = PacketScoreboard::new;
        } else {
            BukkitUtils.compatibilityError(PacketScoreboard.getException(), "Scoreboards", null,
                    "Scoreboard feature will not work",
                    "Belowname feature will not work",
                    "Playerlist objective feature will not work",
                    "Scoreboard teams feature will not work (nametags & sorting)");
        }
    }
}
