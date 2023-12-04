package me.neznamy.tab.platforms.bukkit.header;

import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.PacketSender;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import org.bukkit.Bukkit;
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
        } else if (PacketHeaderFooter.isAvailable() && PacketSender.isAvailable()) {
            instance = new PacketHeaderFooter();
        } else if (BukkitHeaderFooter.isAvailable()) {
            instance = new BukkitHeaderFooter();
            Bukkit.getConsoleSender().sendMessage(EnumChatFormat.RED.getFormat() + "[TAB] Failed to initialize NMS fields for " +
                    "sending Header/Footer due to a compatibility error. Using fallback solution using Bukkit API. " +
                    "This will drastically drop performance, as well as miss support for fonts (added in 1.16). " +
                    "Please update the plugin to version with proper support for your server version.");
        } else {
            Bukkit.getConsoleSender().sendMessage(EnumChatFormat.RED.getFormat() + "[TAB] Failed to initialize NMS fields for " +
                    "sending Header/Footer due to a compatibility error. No fallback solution was found either. This will " +
                    "result in the feature not working. " +
                    "Please update the plugin to version with proper support for your server version.");
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
