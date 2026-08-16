package me.neznamy.tab.shared.features.scoreboard.lines;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation for empty lines. They are treated as a special case to make them actually
 * appear, as refresh logic hides lines that evaluated to an empty string, which we do not want
 * if a line is configured to be empty (spacer line).
 */
@RequiredArgsConstructor
public class EmptyLine implements ScoreboardLine {

    /** Holder of this line */
    @NotNull
    private final ScoreboardLineHolder holder;

    @Override
    public void register(@NonNull TabPlayer player) {
        holder.setScore(player, holder.getForcedPlayerNameStart());
    }

    @Override
    public void refresh(@NotNull TabPlayer player) {
        player.scoreboardData.lineProperties.get(holder).scoreProperty.update();
        holder.setScore(player, holder.getForcedPlayerNameStart());
    }

    @Override
    public void unregister(@NotNull TabPlayer player) {
        player.getScoreboard().removeScore(ScoreboardManagerImpl.OBJECTIVE_NAME, holder.getForcedPlayerNameStart());
    }
}
