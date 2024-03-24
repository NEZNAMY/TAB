package me.neznamy.tab.shared.features.scoreboard.lines;

import lombok.NonNull;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Line using all 3 values - prefix, name and suffix. Line may flicker when placeholder changes value.
 * Limitations:
 *   1.5.x - 1.7.x: up to 42 characters
 *   1.8.x - 1.12.x: up to 66 characters
 *   1.13+: unlimited
 */
public class LongLine extends ScoreboardLine {

    private final String nameProperty = Property.randomName();

    /**
     * Constructs new instance with given parameters
     *
     * @param   parent
     *          scoreboard this line belongs to
     * @param   lineNumber
     *          ID of this line
     * @param   text
     *          line text
     */
    public LongLine(@NonNull ScoreboardImpl parent, int lineNumber, @NonNull String text) {
        super(parent, lineNumber, text);
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (!parent.getPlayers().contains(refreshed)) return; //player has different scoreboard displayed
        if (refreshed.getProperty(textProperty).update()) {
            if (refreshed.getVersion().getMinorVersion() >= 13) {
                updateTeam(refreshed, refreshed.getProperty(textProperty).get(), "");
            } else {
                removeLine(refreshed, refreshed.getProperty(nameProperty).get());
                String[] values = splitText(
                        getPlayerName(lineNumber),
                        RGBUtils.getInstance().convertRGBtoLegacy(refreshed.getProperty(textProperty).get()),
                        refreshed.getVersion().getMinorVersion() >= 8 ? Limitations.SCOREBOARD_SCORE_LENGTH_1_8 : Limitations.SCOREBOARD_SCORE_LENGTH_1_7
                );
                addLine(refreshed, values[1], values[0], values[2]);
                refreshed.setProperty(this, nameProperty, values[1]);
            }
        }
    }

    @Override
    public void register(@NonNull TabPlayer p) {
        p.setProperty(this, textProperty, text);
        getScoreRefresher().registerProperties(p);
        String value = p.getProperty(textProperty).get();
        if (p.getVersion().getMinorVersion() >= 13) {
            addLine(p, playerName, value, "");
            p.setProperty(this, nameProperty, playerName);
        } else {
            String[] values = splitText(
                    playerName,
                    RGBUtils.getInstance().convertRGBtoLegacy(value),
                    p.getVersion().getMinorVersion() >= 8 ? Limitations.SCOREBOARD_SCORE_LENGTH_1_8 : Limitations.SCOREBOARD_SCORE_LENGTH_1_7
            );
            addLine(p, values[1], values[0], values[2]);
            p.setProperty(this, nameProperty, values[1]);
        }
    }

    @Override
    public void unregister(@NonNull TabPlayer p) {
        if (parent.getPlayers().contains(p)) {
            removeLine(p, p.getProperty(nameProperty).get());
        }
    }

    @Override
    public void setText(@NonNull String text) {
        initializeText(text);
        for (TabPlayer p : parent.getPlayers()) {
            p.setProperty(this, textProperty, text);
            refresh(p, true);
        }
    }

    @Override
    public String getPlayerName(@NonNull TabPlayer viewer) {
        return viewer.getProperty(nameProperty).get();
    }
}