package me.neznamy.tab.platforms.bukkit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.ProtocolVersion;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class containing static util methods used on Bukkit.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BukkitUtils {

    /** Whether compatibility exceptions should be printed or not, enabling when adding support for new versions */
    public static final boolean PRINT_EXCEPTIONS = false;

    private static boolean compatibilityIssue;

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
        sb.append("§c[TAB] Failed to initialize minecraft fields for ");
        sb.append(failedCheck);
        sb.append(" due to a compatibility error. ");
        if (fallback != null) {
            sb.append("Using fallback solution using ").append(fallback).append(". ");
        } else {
            sb.append("No fallback solution was found. ");
        }
        if (missingFeatures.length > 0) sb.append("This will result in: ");
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
        Bukkit.getConsoleSender().sendMessage("§c[TAB] Please use " +
                "a plugin version with full support for your server version for optimal experience. This plugin version " +
                "has full support for 1.8.8, 1.12.2 and 1.16.5 - " + ProtocolVersion.LATEST_KNOWN_VERSION.getFriendlyName() + ".");
    }
}
