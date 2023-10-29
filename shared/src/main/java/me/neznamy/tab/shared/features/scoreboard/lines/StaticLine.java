package me.neznamy.tab.shared.features.scoreboard.lines;

import lombok.NonNull;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;

/**
 * A line with static text (no placeholders)
 * Limitations:
 *   1.5.x - 1.7.x: 42 characters
 *   1.8.x - 1.12.x: 66 characters
 *   1.13+: unlimited
 */
public class StaticLine extends ScoreboardLine {

    //values for 1.7 clients
    protected String prefix17;
    protected String name17;
    protected String suffix17;

    //values for 1.8-1.12 clients
    protected String prefix;
    protected String name;
    protected String suffix;

    public StaticLine(@NonNull ScoreboardImpl parent, int lineNumber, @NonNull String text) {
        super(parent, lineNumber);
        this.text = EnumChatFormat.color(text);
        setValues(this.text);
    }

    private void setValues(@NonNull String text) {
        super.text = text;
        String legacy = RGBUtils.getInstance().convertRGBtoLegacy(this.text);
        //1.8+
        String[] v18 = splitText(getPlayerName(lineNumber), legacy, Limitations.SCOREBOARD_SCORE_LENGTH_1_8);
        prefix = v18[0];
        name = v18[1];
        suffix = v18[2];
        //1.7-
        String[] v17 = splitText(getPlayerName(lineNumber), legacy, Limitations.SCOREBOARD_SCORE_LENGTH_1_7);
        prefix17 = v17[0];
        name17 = v17[1];
        suffix17 = v17[2];
    }

    @Override
    public String getPlayerName(@NonNull TabPlayer viewer) {
        if (viewer.getVersion().getMinorVersion() >= 13) {
            return playerName;
        } else if (viewer.getVersion().getMinorVersion() >= 8) {
            return name;
        } else {
            return name17;
        }
    }

    @Override
    public void register(@NonNull TabPlayer p) {
        if (p.getVersion().getMinorVersion() >= 13) {
            addLine(p, playerName, text, "");
        } else if (p.getVersion().getMinorVersion() >= 8) {
            addLine(p, name, prefix, suffix);
        } else {
            addLine(p, name17, prefix17, suffix17);
        }
    }

    @Override
    public void unregister(@NonNull TabPlayer p) {
        if (text.length() > 0) {
            removeLine(p, getPlayerName(p));
        }
    }

    @Override
    public void setText(@NonNull String text) {
        for (TabPlayer p : parent.getPlayers()) {
            unregister(p);
        }
        setValues(text);
        for (TabPlayer p : parent.getPlayers()) {
            register(p);
        }
    }
}