package me.neznamy.tab.shared.features.scoreboard.lines;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;

/**
 * Line using all 3 values - prefix, name and suffix. Line may flicker when placeholder changes value.
 */
public class LongLine extends ScoreboardLine implements Refreshable {

    @Getter private final String featureName = "Scoreboard";
    @Getter private final String refreshDisplayName = "Updating Scoreboard lines";
    private final String textProperty;
    private final String nameProperty;

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
        super(parent, lineNumber);
        this.text = text;
        nameProperty = TabConstants.Property.scoreboardName(parent.getName(), lineNumber);
        textProperty = parent.getName() + "-" + teamName;
    }

    @Override
    public void refresh(@NonNull TabPlayer refreshed, boolean force) {
        if (!parent.getPlayers().contains(refreshed)) return; //player has different scoreboard displayed
        if (refreshed.getProperty(textProperty).update()) {
            if (refreshed.getVersion().getMinorVersion() >= 13) {
                refreshed.getScoreboard().updateTeam(teamName, refreshed.getProperty(textProperty).get(),
                        "", Scoreboard.NameVisibility.ALWAYS, Scoreboard.CollisionRule.ALWAYS, 0);
            } else {
                removeLine(refreshed, refreshed.getProperty(nameProperty).get());
                String[] values = splitText(getPlayerName(lineNumber), RGBUtils.getInstance().convertRGBtoLegacy(refreshed.getProperty(textProperty).get()), refreshed.getVersion().getMinorVersion() >= 8 ? 40 : 16);
                addLine(refreshed, values[1], values[0], values[2]);
                refreshed.setProperty(this, nameProperty, values[1]);
            }
        }
    }

    @Override
    public void register(@NonNull TabPlayer p) {
        p.setProperty(this, textProperty, text);
        String value = p.getProperty(textProperty).get();
        if (p.getVersion().getMinorVersion() >= 13) {
            addLine(p, playerName, value, "");
            p.setProperty(this, nameProperty, playerName);
        } else {
            String[] values = splitText(playerName, RGBUtils.getInstance().convertRGBtoLegacy(value), p.getVersion().getMinorVersion() >= 8 ? 40 : 16);
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
        this.text = text;
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