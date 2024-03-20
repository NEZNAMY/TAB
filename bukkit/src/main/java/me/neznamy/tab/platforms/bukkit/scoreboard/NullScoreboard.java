package me.neznamy.tab.platforms.bukkit.scoreboard;

import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Dummy implementation if no scoreboard implementation is available.
 */
public class NullScoreboard extends Scoreboard<BukkitTabPlayer, Object> {

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public NullScoreboard(@NonNull BukkitTabPlayer player) {
        super(player);
    }

    @Override
    protected void setDisplaySlot0(int slot, @NonNull String objective) {
        // Do nothing
    }

    @Override
    protected void setScore0(@NonNull String objective, @NonNull String scoreHolder, int score,
                             @Nullable Object displayName, @Nullable Object numberFormat) {
        // Do nothing
    }

    @Override
    protected void removeScore0(@NonNull String objective, @NonNull String scoreHolder) {
        // Do nothing
    }

    @Override
    protected void registerObjective0(@NonNull String objectiveName, @NonNull String title, int display,
                                      @Nullable Object numberFormat) {
        // Do nothing
    }

    @Override
    protected void unregisterObjective0(@NonNull String objectiveName) {
        // Do nothing
    }

    @Override
    protected void updateObjective0(@NonNull String objectiveName, @NonNull String title, int display,
                                    @Nullable Object numberFormat) {
        // Do nothing
    }

    @Override
    protected void registerTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix,
                                 @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                                 @NonNull Collection<String> players, int options, @NonNull EnumChatFormat color) {
        // Do nothing
    }

    @Override
    protected void unregisterTeam0(@NonNull String name) {
        // Do nothing
    }

    @Override
    protected void updateTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix,
                               @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                               int options, @NonNull EnumChatFormat color) {
        // Do nothing
    }
}
