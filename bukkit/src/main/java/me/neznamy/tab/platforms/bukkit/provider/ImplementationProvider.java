package me.neznamy.tab.platforms.bukkit.provider;

import io.netty.channel.Channel;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabListEntryTracker;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface to provide platform-specific implementations of various features in various Minecraft versions.
 */
public interface ImplementationProvider {

    /**
     * Creates a new instance of scoreboard for the given player.
     *
     * @param   player
     *          Player to create scoreboard for
     * @return  Newly created scoreboard
     */
    @NotNull
    Scoreboard newScoreboard(@NotNull BukkitTabPlayer player);

    /**
     * Creates a new instance of tab list for the given player.
     *
     * @param   player
     *          Player to create tab list for
     * @return  Newly created tab list
     */
    @NotNull
    TabList newTabList(@NotNull BukkitTabPlayer player);

    /**
     * Returns component converter to be used for converting TAB components to NMS components.
     *
     * @return  Component converter
     */
    @NotNull
    ComponentConverter<?> getComponentConverter();

    /**
     * Gets player's channel. If not available, returns {@code null}.
     *
     * @param   player
     *          Player to get channel of
     * @return  Player's channel or {@code null} if not supported
     */
    @Nullable
    Channel getChannel(@NotNull Player player);

    /**
     * Creates a new instance of tab list entry tracker.
     *
     * @return  Newly created tab list entry tracker
     */
    @NotNull
    TabListEntryTracker newTabListEntryTracker();

    /**
     * Returns ping of the given player.
     *
     * @param   player
     *          Player to get ping of
     * @return  Player's ping
     */
    int getPing(@NotNull BukkitTabPlayer player);
}
