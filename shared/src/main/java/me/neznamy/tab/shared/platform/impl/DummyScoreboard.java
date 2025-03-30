package me.neznamy.tab.shared.platform.impl;

import lombok.NonNull;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Dummy implementation if no scoreboard implementation is available.
 */
public class DummyScoreboard extends SafeScoreboard<TabPlayer> {

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public DummyScoreboard(@NonNull TabPlayer player) {
        super(player);
    }

    @Override
    public void registerObjective(@NonNull Objective objective) {
        // Do nothing
    }

    @Override
    public void setDisplaySlot(@NonNull Objective objective) {
        // Do nothing
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        // Do nothing
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        // Do nothing
    }

    @Override
    public void setScore(@NonNull Score score) {
        // Do nothing
    }

    @Override
    public void removeScore(@NonNull Score score) {
        // Do nothing
    }

    @Override
    @NotNull
    public Object createTeam(@NonNull String name) {
        return new Object();
    }

    @Override
    public void registerTeam(@NonNull Team team) {
        // Do nothing
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        // Do nothing
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        // Do nothing
    }
}
