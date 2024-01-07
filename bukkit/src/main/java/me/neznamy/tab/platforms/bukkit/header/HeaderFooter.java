package me.neznamy.tab.platforms.bukkit.header;

import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class responsible for sending header/footer to players on request.
 */
public abstract class HeaderFooter {

    /** Instance of the class, null if no implementation is available */
    @Getter
    @Nullable
    private static HeaderFooter instance;

    /**
     * Finds the best available instance for current server software.
     */
    public static void findInstance() {
        instance = findInstance0();
    }

    @Nullable
    private static HeaderFooter findInstance0() {
        if (PaperHeaderFooter.isAvailable()) return new PaperHeaderFooter();
        try {
            return new PacketHeaderFooter();
        } catch (Exception e) {
            if (BukkitHeaderFooter.isAvailable()) {
                BukkitUtils.compatibilityError(e, "sending Header/Footer", "Bukkit API",
                        "Header/Footer having drastically increased CPU usage",
                        "Header/Footer not supporting fonts (1.16+)");
                return new BukkitHeaderFooter();
            } else {
                BukkitUtils.compatibilityError(e, "sending Header/Footer", null,
                        "Header/Footer feature not working");
                return null;
            }
        }
    }

    /**
     * Sends header/footer to player.
     *
     * @param   player
     *          Player to send header/footer to.
     * @param   header
     *          Header to use.
     * @param   footer
     *          Footer to use.
     */
    public abstract void set(@NotNull BukkitTabPlayer player, @NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer);
}
