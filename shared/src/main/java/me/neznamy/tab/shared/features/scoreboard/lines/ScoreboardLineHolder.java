package me.neznamy.tab.shared.features.scoreboard.lines;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.scoreboard.Line;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardPlayerData;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * A class representing a line in the scoreboard.
 * It holds all information about a line and is able to swap between implementations
 * if text is changed via API.
 */
@Getter
public class ScoreboardLineHolder extends RefreshableFeature implements Line, CustomThreaded {

    /** The scoreboard this line belongs to */
    @NotNull
    private final ScoreboardImpl parent;

    /** The configured text to display in raw form (including || separators) */
    @NotNull
    private String text;

    @NotNull
    private String leftText;

    @NotNull
    private String numberFormat;

    @NotNull
    private String score;

    /** Number of this line (1-15+) */
    private final int lineNumber;

    @NotNull
    private final String forcedPlayerNameStart;

    /** The team name for this line */
    @NotNull
    private final String teamName;

    /** The handle for the scoreboard line based on current text */
    @NotNull
    private ScoreboardLine handle;

    /**
     * Constructs a new instance with given parameters.
     *
     * @param   parent
     *          the scoreboard this line belongs to
     * @param   text
     *          the configured text to display in raw form (including || separators)
     * @param   lineNumber
     *          number of this line (1-15+)
     */
    public ScoreboardLineHolder(@NonNull ScoreboardImpl parent, @NonNull String text, int lineNumber) {
        if (lineNumber > 99) throw new IllegalStateException("Internal code does not support more than 99 lines per scoreboard.");
        this.parent = parent;
        this.text = text;
        this.lineNumber = lineNumber;
        forcedPlayerNameStart = String.format("§%d§%d§r", (lineNumber / 10) % 10, lineNumber % 10);
        teamName = "TAB-Sidebar-" + lineNumber;
        splitText();
        handle = createHandle();
    }

    @Override
    @NotNull
    public String getText() {
        return text;
    }

    @Override
    public void setText(@NonNull String text) {
        if (this.text.equals(text)) return;
        for (TabPlayer player : parent.getPlayers()) {
            handle.unregister(player);
        }
        this.text = text;
        splitText();
        handle = createHandle();
        for (TabPlayer player : parent.getPlayers()) {
            player.scoreboardData.lineProperties.put(this, new ScoreboardPlayerData.LineProperties(
                    new Property(this, player, leftText),
                    forcedPlayerNameStart,
                    new Property(this, player, numberFormat),
                    new Property(this, player, score)
            ));
            handle.register(player);
        }
    }

    @Override
    @NotNull
    public ThreadExecutor getCustomThread() {
        return parent.getCustomThread();
    }

    @Override
    @NotNull
    public String getRefreshDisplayName() {
        return "Updating Scoreboard lines";
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        handle.refresh(refreshed);
    }

    @Override
    @NotNull
    public String getFeatureName() {
        return parent.getFeatureName();
    }

    /**
     * Registers this line to the player
     *
     * @param   player
     *          player to register line to
     */
    public void register(@NotNull TabPlayer player) {
        player.scoreboardData.lineProperties.put(this, new ScoreboardPlayerData.LineProperties(
                new Property(this, player, leftText),
                forcedPlayerNameStart,
                new Property(this, player, numberFormat),
                new Property(this, player, score)
        ));
        player.getScoreboard().registerTeam(
                teamName,
                TabComponent.empty(),
                TabComponent.empty(),
                Scoreboard.NameVisibility.NEVER,
                Scoreboard.CollisionRule.NEVER,
                Collections.singletonList(forcedPlayerNameStart),
                0,
                TabTextColor.RESET.getLegacyColor()
        );
        handle.register(player);
    }

    /**
     * Updates the team prefix and suffix for the player
     *
     * @param   player
     *          player to update team for
     * @param   prefix
     *          new prefix to set
     * @param   suffix
     *          new suffix to set
     */
    public void updateTeam(@NotNull TabPlayer player, @NotNull String prefix, @NotNull String suffix) {
        player.getScoreboard().updateTeam(
                teamName,
                parent.getManager().getCache().get(prefix),
                parent.getManager().getCache().get(suffix),
                TabTextColor.RESET.getLegacyColor()
        );
    }

    /**
     * Sets the score for player. Created as a separate method to avoid duplication.
     *
     * @param   player
     *          player to set score for
     * @param   scoreHolder
     *          score holder to set score for
     */
    public void setScore(@NotNull TabPlayer player, @NotNull String scoreHolder) {
        int score;
        if (!this.score.isEmpty()) {
            try {
                score = Integer.parseInt(player.scoreboardData.lineProperties.get(this).scoreProperty.get());
            } catch (NumberFormatException e) {
                score = -1;
            }
        } else {
            score = parent.getLines().size() + 1 - lineNumber;
        }
        player.getScoreboard().setScore(
                ScoreboardManagerImpl.OBJECTIVE_NAME,
                scoreHolder,
                score,
                null,
                parent.getManager().getNumberFormatCache().get(player.scoreboardData.lineProperties.get(this).numberFormatProperty.get())
        );
    }

    /**
     * Unregisters this line to the player
     *
     * @param   player
     *          player to unregister line to
     */
    public void unregister(@NotNull TabPlayer player) {
        player.getScoreboard().unregisterTeam(teamName);
        handle.unregister(player);
    }

    private void splitText() {
        leftText = "";
        numberFormat = "";
        score = "";

        String realText = text;
        if (realText.startsWith("Long|")) {
            realText = realText.substring(5);
        }
        String[] split = realText.split("\\|\\|", -1);
        if (split.length > 0) leftText = split[0];
        if (split.length > 1) numberFormat = split[1];
        if (split.length > 2) score = split[2];
    }

    @NotNull
    private ScoreboardLine createHandle() {
        if (leftText.isEmpty() && numberFormat.isEmpty()) {
            return new EmptyLine(this);
        }
        if (text.startsWith("Long|")) {
            return new LongLine(this);
        }
        if (text.contains("%") || text.contains("<")) {
            return new NoFlickerLine(this);
        }
        return new LongLine(this);
    }
}
