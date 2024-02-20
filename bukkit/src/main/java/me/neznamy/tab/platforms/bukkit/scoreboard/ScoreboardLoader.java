package me.neznamy.tab.platforms.bukkit.scoreboard;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.scoreboard.packet.PacketScoreboard;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.List;
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
        } else {
            Exception e = PacketScoreboard.getException();
            if (PaperScoreboard.isAvailable()) {
                instance = PaperScoreboard::new;
                List<String> missingFeatures = Lists.newArrayList("Compatibility with other plugins being reduced");
                if (BukkitReflection.is1_20_3Plus() && !PaperScoreboard.isNumberFormatAPI()) {
                    missingFeatures.add("1.20.3+ visuals not working due to lack of API (added in late 1.20.4 builds)");
                }
                BukkitUtils.compatibilityError(e, "Scoreboards", "Paper API", missingFeatures.toArray(new String[0]));
            } else if (BukkitScoreboard.isAvailable()) {
                instance = BukkitScoreboard::new;
                List<String> missingFeatures = Lists.newArrayList(
                        "Compatibility with other plugins being reduced",
                        "Features receiving new artificial character limits"
                );
                if (BukkitReflection.is1_20_3Plus()) {
                    missingFeatures.add("1.20.3+ visuals not working due to lack of API"); // soontm?
                }
                BukkitUtils.compatibilityError(e, "Scoreboards", "Bukkit API", missingFeatures.toArray(new String[0]));
            } else {
                BukkitUtils.compatibilityError(e, "Scoreboards", null,
                        "Scoreboard feature will not work",
                        "Belowname feature will not work",
                        "Player objective feature will not work",
                        "Scoreboard teams feature will not work (nametags & sorting)");
            }
        }
    }
}
