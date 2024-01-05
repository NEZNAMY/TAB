package me.neznamy.tab.shared.features.scoreboard.lines;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.api.scoreboard.Line;
import me.neznamy.tab.shared.features.scoreboard.ScoreRefresher;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Abstract class representing a line of scoreboard
 */
public abstract class ScoreboardLine extends TabFeature implements Line {

    @Getter private final String featureName = "Scoreboard";

    //ID of this line
    protected final int lineNumber;
    
    //text to display
    @Getter protected String text;
    @Getter protected String numberFormat;
    
    //scoreboard this line belongs to
    @Getter protected final ScoreboardImpl parent;
    
    //scoreboard team name of player in this line
    @Getter protected final String teamName;
    
    //forced player name start to make lines unique & sort them by names
    @Getter protected final String playerName;

    @Getter private final ScoreRefresher scoreRefresher;

    private final Set<TabPlayer> shownPlayers = Collections.newSetFromMap(new WeakHashMap<>());
    
    /**
     * Constructs new instance with given parameters
     *
     * @param   parent
     *          scoreboard this line belongs to
     * @param   lineNumber
     *          ID of this line
     */
    protected ScoreboardLine(@NonNull ScoreboardImpl parent, int lineNumber, String text) {
        initializeText(text);
        this.parent = parent;
        this.lineNumber = lineNumber;
        teamName = "TAB-SB-TM-" + lineNumber;
        playerName = getPlayerName(lineNumber);
        scoreRefresher = new ScoreRefresher(this, numberFormat);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.scoreboardScore(parent.getName(), lineNumber), scoreRefresher);
    }
    
    /**
     * Registers this line to the player
     *
     * @param   p
     *          player to register line to
     */
    public abstract void register(@NonNull TabPlayer p);
    
    /**
     * Unregisters this line to the player
     *
     * @param   p
     *          player to unregister line to
     */
    public abstract void unregister(@NonNull TabPlayer p);

    /**
     * Returns forced name start of this line to specified viewer
     *
     * @param   viewer
     *          Player to get forced name start for
     * @return  forced name start of this line to specified viewer
     */
    public String getPlayerName(@NonNull TabPlayer viewer) {
        return playerName;
    }

    /**
     * Splits the text into 2 with given max length of first string
     *
     * @param   string
     *          string to split
     * @param   firstElementMaxLength
     *          max length of first string
     * @return  array of 2 strings where second one might be empty
     */
    protected String[] split(@NonNull String string, int firstElementMaxLength) {
        if (string.length() <= firstElementMaxLength) return new String[] {string, ""};
        int splitIndex = firstElementMaxLength;
        if (string.charAt(splitIndex-1) == EnumChatFormat.COLOR_CHAR) splitIndex--;
        return new String[] {string.substring(0, splitIndex), string.substring(splitIndex)};
    }

    /**
     * Builds forced name start based on line number
     *
     * @param   lineNumber
     *          ID of line
     * @return  forced name start
     */
    protected String getPlayerName(int lineNumber) {
        String id = String.valueOf(lineNumber);
        if (id.length() == 1) id = "0" + id;
        return EnumChatFormat.COLOR_STRING + id.charAt(0) + EnumChatFormat.COLOR_STRING + id.charAt(1) + EnumChatFormat.COLOR_STRING + "r";
    }
    
    /**
     * Sends this line to player
     *
     * @param   p
     *          player to send line to
     * @param   fakePlayer
     *          player name
     * @param   prefix
     *          prefix
     * @param   suffix
     *          suffix
     */
    protected void addLine(@NonNull TabPlayer p, @NonNull String fakePlayer, @NonNull String prefix, @NonNull String suffix) {
        p.getScoreboard().setScore(
                ScoreboardManagerImpl.OBJECTIVE_NAME,
                fakePlayer,
                getNumber(p),
                null, // Makes no sense for TAB
                scoreRefresher.getNumberFormat(p)
        );
        p.getScoreboard().registerTeam(
                teamName,
                prefix,
                suffix,
                Scoreboard.NameVisibility.NEVER,
                Scoreboard.CollisionRule.NEVER,
                Collections.singletonList(fakePlayer),
                0,
                EnumChatFormat.RESET
        );
        shownPlayers.add(p);
    }
    
    /**
     * Removes this line from player
     *
     * @param   p
     *          player to remove line from
     * @param   fakePlayer
     *          player name
     */
    protected void removeLine(@NonNull TabPlayer p, @NonNull String fakePlayer) {
        p.getScoreboard().removeScore(ScoreboardManagerImpl.OBJECTIVE_NAME, fakePlayer);
        p.getScoreboard().unregisterTeam(teamName);
        shownPlayers.remove(p);
    }

    /**
     * Returns number that should be displayed as score for specified player
     *
     * @param   p
     *          player to get number for
     * @return  number displayed
     */
    public int getNumber(@NonNull TabPlayer p) {
        if (parent.getManager().isUsingNumbers() || p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) {
            return parent.getLines().size() + 1 - lineNumber;
        } else {
            return parent.getManager().getStaticNumber();
        }
    }

    /**
     * Splits entered text into 3 parts - prefix, name and suffix respecting all limits.
     * Returns the values as an array of 3 elements.
     *
     * @param   playerNameStart
     *          forced start of name field (used to secure unique names and line order)
     * @param   text
     *          text to display
     * @param   maxNameLength
     *          maximum length of name field
     * @return  Split text as an array of 3 elements
     */
    protected String[] splitText(@NonNull String playerNameStart, @NonNull String text, int maxNameLength) {
        String prefixValue;
        String nameValue;
        String suffixValue;
        if (text.length() <= (maxNameLength - playerNameStart.length())) {
            prefixValue = "";
            nameValue = playerNameStart + text;
            suffixValue = "";
        } else {
            String[] prefixOther = split(text, Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13);
            prefixValue = prefixOther[0];
            String other = prefixOther[1];
            if (!playerNameStart.isEmpty()) {
                other = playerNameStart + EnumChatFormat.getLastColors(prefixValue) + other;
            }
            String[] nameSuffix = split(other, maxNameLength);
            nameValue = nameSuffix[0];
            suffixValue = nameSuffix[1];
        }
        return new String[]{prefixValue, nameValue, suffixValue};
    }

    /**
     * Returns {@code true} if this line is visible to specified player,
     * {@code false} if not.
     *
     * @param   player
     *          Player to check
     * @return  {@code true} if shown, {@code false} if not
     */
    public boolean isShownTo(@NonNull TabPlayer player) {
        return shownPlayers.contains(player);
    }

    /**
     * Splits text using {@code "||"} string, where first part is text to display and
     * second part is number format (optional)
     *
     * @param   text
     *          Inputted text to categorize
     */
    protected void initializeText(@NotNull String text) {
        String[] split = text.split("\\|\\|");
        this.text = split[0];
        numberFormat = split.length >= 2 ? split[1] : "";
    }
}