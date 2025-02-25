package me.neznamy.tab.platforms.bukkit.header;

import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * An empty implementation that does not do anything.
 */
public class DummyHeaderFooter extends HeaderFooter {

    @Override
    public void set(@NotNull BukkitTabPlayer player, @NotNull TabComponent header, @NotNull TabComponent footer) {
        // Do nothing
    }
}