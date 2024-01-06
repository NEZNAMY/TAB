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
        if (PaperHeaderFooter.isAvailable()) {
            instance = new PaperHeaderFooter();
        } else if (PacketHeaderFooter.isAvailable()) {
            instance = new PacketHeaderFooter();
        } else if (BukkitHeaderFooter.isAvailable()) {
            instance = new BukkitHeaderFooter();
            BukkitUtils.compatibilityError("sending Header/Footer", "Bukkit API",
                    "Header/Footer having drastically increased CPU usage",
                    "Header/Footer not supporting fonts (1.16+)");
        } else {
            BukkitUtils.compatibilityError("sending Header/Footer", null,
                    "Header/Footer feature not working");
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
