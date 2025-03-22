package me.neznamy.tab.shared.features.scoreboard.lines;

import lombok.NonNull;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Line of text with placeholder support
 * Limitations:
 *   1.5.x - 1.12.x: 28 - 32 characters (depending on used magic codes)
 *   1.13+: unlimited
 */
public class StableDynamicLine extends ScoreboardLine {

    private final String[] EMPTY_ARRAY = new String[0];

    /**
     * Constructs new instance with given parameters
     *
     * @param   parent
     *          scoreboard this line belongs to
     * @param   lineNumber
     *          ID of this line
     * @param   text
     *          text to display
     */
    public StableDynamicLine(@NonNull ScoreboardImpl parent, int lineNumber, @NonNull String text) {
        super(parent, lineNumber, text);
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (refreshed.scoreboardData.activeScoreboard != parent) return; //player has different scoreboard displayed
        String[] prefixSuffix = replaceText(refreshed, force, false);
        if (prefixSuffix.length == 0) return;
        updateTeam(refreshed, prefixSuffix[0], prefixSuffix[1]);
    }

    @Override
    public void register(@NonNull TabPlayer p) {
        p.scoreboardData.lineProperties.put(this, new Property(this, p, text));
        getScoreRefresher().registerProperties(p);
        String[] prefixSuffix = replaceText(p, true, true);
        if (prefixSuffix.length == 0) return;
        addLine(p, forcedPlayerNameStart, prefixSuffix[0], prefixSuffix[1]);
    }

    @Override
    public void unregister(@NonNull TabPlayer p) {
        if (p.scoreboardData.activeScoreboard == parent && !p.scoreboardData.lineProperties.get(this).get().isEmpty()) {
            removeLine(p, forcedPlayerNameStart);
        }
    }

    /**
     * Applies all placeholders and splits the result into prefix/suffix based on client version
     * or hides the line entirely if result is empty (and shows back once it's not)
     *
     * @param   p
     *          player to replace text for
     * @param   force
     *          if action should be done despite update seemingly not needed
     * @param   suppressToggle
     *          if line should NOT be removed despite being empty
     * @return  array of 2 elements for prefix/suffix
     */
    private String[] replaceText(TabPlayer p, boolean force, boolean suppressToggle) {
        Property scoreProperty = p.scoreboardData.lineProperties.get(this);
        if (scoreProperty == null) return EMPTY_ARRAY; //not actually loaded yet (force refresh called from placeholder manager register method)
        boolean emptyBefore = scoreProperty.get().isEmpty();
        if (!scoreProperty.update() && !force) return EMPTY_ARRAY;
        String replaced = scoreProperty.get();
        if (!p.getVersion().supportsRGB()) {
            replaced = parent.getManager().getCache().get(replaced).toLegacyText(); //converting RGB to legacy here to avoid splitting in the middle of RGB code
        }
        String[] split = split(p, replaced);
        if (!replaced.isEmpty()) {
            if (emptyBefore) {
                //was "", now it is not
                addLine(p, forcedPlayerNameStart, split[0], split[1]);
                parent.recalculateScores(p);
                return EMPTY_ARRAY;
            } else {
                return split;
            }
        } else {
            if (!suppressToggle) {
                //new string is "", but before it was not
                removeLine(p, forcedPlayerNameStart);
                parent.recalculateScores(p);
            }
            return EMPTY_ARRAY;
        }
    }

    /**
     * Splits text into 2 values (prefix/suffix) based on client version and text itself
     *
     * @param   p
     *          player to split text for
     * @param   text
     *          text to split
     * @return  array of 2 elements for prefix and suffix
     */
    private String[] split(@NonNull TabPlayer p, @NonNull String text) {
        if (p.getVersion().getMinorVersion() >= 13 && !TAB.getInstance().getConfiguration().getConfig().isPacketEventsCompensation()) return new String[] {text, ""};
        int charLimit = Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13;
        if (text.length() > charLimit) {
            StringBuilder prefix = new StringBuilder(text);
            StringBuilder suffix = new StringBuilder(text);
            prefix.setLength(charLimit);
            suffix.delete(0, charLimit);
            if (prefix.charAt(charLimit-1) == '§') {
                prefix.setLength(prefix.length()-1);
                suffix.insert(0, '§');
            }
            String prefixString = prefix.toString();
            suffix.insert(0, getLastColors(parent.getManager().getCache().get(prefixString).toLegacyText()));
            return new String[] {prefixString, suffix.toString()};
        } else {
            return new String[] {text, ""};
        }
    }

    @Override
    public void setText(@NonNull String text) {
        ensureActive();
        initializeText(text);
        for (TabPlayer p : parent.getPlayers()) {
            p.scoreboardData.lineProperties.get(this).changeRawValue(text);
            String[] prefixSuffix = replaceText(p, true, true);
            if (prefixSuffix.length == 0) {
                if (text.isEmpty()) {
                    prefixSuffix = new String[]{"", ""};
                } else {
                    continue;
                }
            }
            updateTeam(p, prefixSuffix[0], prefixSuffix[1]);
        }
    }
}