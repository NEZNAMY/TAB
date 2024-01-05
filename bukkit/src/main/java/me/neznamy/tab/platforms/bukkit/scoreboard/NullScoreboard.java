package me.neznamy.tab.platforms.bukkit.scoreboard;

import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Dummy implementation if no scoreboard implementation is available.
 */
public class NullScoreboard extends Scoreboard<BukkitTabPlayer> {

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public NullScoreboard(@NotNull BukkitTabPlayer player) {
        super(player);
    }

    @Override
    protected void setDisplaySlot0(int slot, @NotNull String objective) {
        // Do nothing
    }

    @Override
    protected void setScore0(@NotNull String objective, @NotNull String scoreHolder, int score,
                             @Nullable IChatBaseComponent displayName, @Nullable IChatBaseComponent numberFormat) {
        // Do nothing
    }

    @Override
    protected void removeScore0(@NotNull String objective, @NotNull String scoreHolder) {
        // Do nothing
    }

    @Override
    protected void registerObjective0(@NotNull String objectiveName, @NotNull String title, int display,
                                      @Nullable IChatBaseComponent numberFormat) {
        // Do nothing
    }

    @Override
    protected void unregisterObjective0(@NotNull String objectiveName) {
        // Do nothing
    }

    @Override
    protected void updateObjective0(@NotNull String objectiveName, @NotNull String title, int display,
                                    @Nullable IChatBaseComponent numberFormat) {
        // Do nothing
    }

    @Override
    protected void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                                 @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                                 @NotNull Collection<String> players, int options, @NotNull EnumChatFormat color) {
        // Do nothing
    }

    @Override
    protected void unregisterTeam0(@NotNull String name) {
        // Do nothing
    }

    @Override
    protected void updateTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                               @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                               int options, @NotNull EnumChatFormat color) {
        // Do nothing
    }
}
