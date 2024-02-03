package me.neznamy.tab.platforms.bukkit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Class containing static util methods used on Bukkit.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BukkitUtils {

    /** Whether compatibility exceptions should be printed or not, enabling when adding support for new versions */
    private static final boolean PRINT_EXCEPTIONS = false;

    private static boolean compatibilityIssue;

    /**
     * Returns online players from Bukkit API. This requires reflection, as return type changed in 1.8,
     * and we want to avoid errors.
     *
     * @return  Online players from Bukkit API.
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    @NotNull
    public static Player[] getOnlinePlayers() {
        Object players = Bukkit.class.getMethod("getOnlinePlayers").invoke(null);
        if (players instanceof Player[]) {
            //1.7-
            return (Player[]) players;
        } else {
            //1.8+
            return ((Collection<Player>)players).toArray(new Player[0]);
        }
    }

    /**
     * Prints a console warn that some compatibility issue was found.
     *
     * @param   exception
     *          Exception thrown when initializing fields, if applicable
     * @param   failedCheck
     *          Check that failed
     * @param   fallback
     *          Fallback implementation, if available
     * @param   missingFeatures
     *          Features that will be broken because of the incompatibility
     */
    public static void compatibilityError(@NotNull Exception exception, @NotNull String failedCheck,
                                          @Nullable String fallback, @NotNull String... missingFeatures) {
        StringBuilder sb = new StringBuilder();
        sb.append(EnumChatFormat.RED);
        sb.append("[TAB] Failed to initialize minecraft fields for ");
        sb.append(failedCheck);
        sb.append(" due to a compatibility error. ");
        if (fallback != null) {
            sb.append("Using fallback solution using ").append(fallback).append(". ");
        } else {
            sb.append("No fallback solution was found. ");
        }
        sb.append("This will result in: ");
        for (int i=0; i<missingFeatures.length; i++) {
            sb.append("\n").append("#").append(i + 1).append(": ").append(missingFeatures[i]);
        }
        Bukkit.getConsoleSender().sendMessage(sb.toString());
        if (PRINT_EXCEPTIONS) exception.printStackTrace();
        compatibilityIssue = true;
    }

    /**
     * Sends a message asking user to update the plugin if some compatibility issue was found.
     */
    public static void sendCompatibilityMessage() {
        if (!compatibilityIssue) return;
        Bukkit.getConsoleSender().sendMessage(EnumChatFormat.RED + "[TAB] Please update the plugin to " +
                "a version with native support for your server version for optimal experience. This plugin version " +
                "was made for " + ProtocolVersion.V1_5.getFriendlyName() + " - " + ProtocolVersion.LATEST_KNOWN_VERSION.getFriendlyName() + ".");
    }
}
