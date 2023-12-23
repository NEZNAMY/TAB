package me.neznamy.tab.platforms.bukkit.scoreboard;

import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.scoreboard.packet.PacketScoreboard;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Loader finding the best available Scoreboard implementation.
 */
public class ScoreboardLoader {

    /** Instance function */
    @Getter
    private static Function<BukkitTabPlayer, Scoreboard<BukkitTabPlayer>> instance;

    /**
     * Finds the best available instance for current server software.
     */
    public static void findInstance() {
        if (PacketScoreboard.isAvailable()) {
            instance = PacketScoreboard::new;
        } else if (PaperScoreboard.isAvailable()) {
            instance = PaperScoreboard::new;
            sendMessage("Paper");
        } else if (BukkitScoreboard.isAvailable()) {
            instance = BukkitScoreboard::new;
            sendMessage("Bukkit");
        } else {
            instance = NullScoreboard::new;
            Bukkit.getConsoleSender().sendMessage(EnumChatFormat.RED.getFormat() + "[TAB] Failed to initialize NMS fields for " +
                    "Scoreboards due to a compatibility error. No fallback solution was found either. This will " +
                    "result in the features not working. " +
                    "Please update the plugin a to version with native support for your server version to unlock the features.");
        }
    }

    private static void sendMessage(@NotNull String implementationName) {
        Bukkit.getConsoleSender().sendMessage(EnumChatFormat.RED.getFormat() + "[TAB] Failed to initialize NMS fields for " +
                "Scoreboards due to a compatibility error. Using fallback solution using " + implementationName + " API. " +
                "This may result in features not working properly, mainly compatibility with other plugins. " +
                "Please update the plugin to a version with native support for your server version for optimal experience.");
    }
}
