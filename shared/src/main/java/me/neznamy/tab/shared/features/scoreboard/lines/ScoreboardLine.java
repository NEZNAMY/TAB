package me.neznamy.tab.shared.features.scoreboard.lines;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.api.scoreboard.Line;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.scoreboard.ScoreRefresher;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract class representing a line of scoreboard
 */
@Getter
public abstract class ScoreboardLine extends RefreshableFeature implements Line, CustomThreaded {

    //ID of this line
    protected final int lineNumber;

    //text to display
    protected String text;
    protected String numberFormat;
    
    //scoreboard this line belongs to
    protected final ScoreboardImpl parent;
    
    //scoreboard team name of player in this line
    protected final String teamName;

    /** Forced player name start to make lines unique & sort them by names */
    protected final String forcedPlayerNameStart;

    private final ScoreRefresher scoreRefresher;

    private final Set<TabPlayer> shownPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
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
        teamName = "TAB-Sidebar-" + lineNumber;
        if (lineNumber > 99) throw new IllegalStateException("Internal code does not support more than 99 lines per scoreboard.");
        forcedPlayerNameStart = String.format("§%d§%d§r", (lineNumber / 10) % 10, lineNumber % 10);
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
     * Returns player name of this line for specified viewer.
     *
     * @param   viewer
     *          Player to get line name for
     * @return  Name of this line for specified viewer
     */
    @NotNull
    public String getPlayerName(@NonNull TabPlayer viewer) {
        return forcedPlayerNameStart;
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
        if (string.charAt(splitIndex-1) == '§') splitIndex--;
        return new String[] {string.substring(0, splitIndex), string.substring(splitIndex)};
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
                parent.getManager().getCache().get(prefix),
                parent.getManager().getCache().get(suffix),
                Scoreboard.NameVisibility.NEVER,
                Scoreboard.CollisionRule.NEVER,
                Collections.singletonList(fakePlayer),
                0,
                TabTextColor.RESET.getLegacyColor()
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
        if (parent.getManager().getConfiguration().isUseNumbers() || p.getVersion().getMinorVersion() < 8) {
            return parent.getLines().size() + 1 - lineNumber;
        } else {
            return parent.getManager().getConfiguration().getStaticNumber();
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
            other = playerNameStart + getLastColors(prefixValue) + other;
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

    /**
     * Updates prefix/suffix of the fake player.
     *
     * @param   player
     *          Player to send the update to
     * @param   prefix
     *          Prefix to use
     * @param   suffix
     *          Suffix to use
     */
    protected void updateTeam(@NotNull TabPlayer player, @NotNull String prefix, @NotNull String suffix) {
        player.getScoreboard().updateTeam(
                teamName,
                parent.getManager().getCache().get(prefix),
                parent.getManager().getCache().get(suffix),
                TabTextColor.RESET.getLegacyColor()
        );
    }

    /**
     * Silently removes players from the list of players this line is shown to.
     *
     * @param   player
     *          Player to remove
     */
    public void removePlayerSilently(@NonNull TabPlayer player) {
        shownPlayers.remove(player);
    }

    @Override
    @NotNull
    public ThreadExecutor getCustomThread() {
        return parent.getCustomThread();
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return parent.getFeatureName();
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating Scoreboard lines";
    }

    /**
     * Returns last color codes used in provided text.
     *
     * @param   input
     *          text to get last colors from
     * @return  last colors used in provided text or empty string if nothing was found
     */
    @NotNull
    protected String getLastColors(@NotNull String input) {
        StringBuilder result = new StringBuilder();
        int length = input.length();
        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if ((section == '§' || section == '&') && (index < length - 1)) {
                char c = input.charAt(index + 1);
                if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(String.valueOf(c))) {
                    result.insert(0, '§');
                    result.insert(1, c);
                    if ("0123456789AaBbCcDdEeFfRr".contains(String.valueOf(c))) {
                        break;
                    }
                }
            }
        }
        return result.toString();
    }
}