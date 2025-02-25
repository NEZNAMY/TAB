package me.neznamy.tab.platforms.bukkit.header;

import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * A class responsible for sending header/footer to players on request.
 */
public abstract class HeaderFooter {

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
    public abstract void set(@NotNull BukkitTabPlayer player, @NotNull TabComponent header, @NotNull TabComponent footer);
}