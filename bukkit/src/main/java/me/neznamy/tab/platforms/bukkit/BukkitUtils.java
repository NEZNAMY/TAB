package me.neznamy.tab.platforms.bukkit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
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
     * Converts component to legacy string using bukkit RGB format if supported by both server and client.
     * If not, closest legacy color is used instead.
     *
     * @param   component
     *          Component to convert
     * @param   rgbClient
     *          Whether client accepts RGB colors or not.
     * @return  Converted string using bukkit color format
     */
    @NotNull
    public static String toBukkitFormat(@NotNull IChatBaseComponent component, boolean rgbClient) {
        StringBuilder sb = new StringBuilder();
        if (component.getModifier().getColor() != null) {
            if (TAB.getInstance().getServerVersion().supportsRGB() && rgbClient) {
                String hexCode = component.getModifier().getColor().getHexCode();
                char c = EnumChatFormat.COLOR_CHAR;
                sb.append(c).append("x").append(c).append(hexCode.charAt(0)).append(c).append(hexCode.charAt(1))
                        .append(c).append(hexCode.charAt(2)).append(c).append(hexCode.charAt(3))
                        .append(c).append(hexCode.charAt(4)).append(c).append(hexCode.charAt(5));
            } else {
                sb.append(component.getModifier().getColor().getLegacyColor().getFormat());
            }
        }
        sb.append(component.getModifier().getMagicCodes());
        if (component.getText() != null) sb.append(component.getText());
        for (IChatBaseComponent extra : component.getExtra()) {
            sb.append(toBukkitFormat(extra, rgbClient));
        }
        return sb.toString();
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
        sb.append(EnumChatFormat.RED.getFormat());
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
        Bukkit.getConsoleSender().sendMessage(EnumChatFormat.RED.getFormat() + "[TAB] Please update the plugin to " +
                "a version with native support for your server version for optimal experience. This plugin version " +
                "was made for " + ProtocolVersion.V1_5.getFriendlyName() + " - " + ProtocolVersion.LATEST_KNOWN_VERSION.getFriendlyName() + ".");
    }
}
