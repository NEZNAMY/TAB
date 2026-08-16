package me.neznamy.tab.shared.features.scoreboard.lines;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardPlayerData;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * Line using all 3 values - prefix, name and suffix. Line may flicker when placeholder changes value.
 * Limitations:
 *   1.5.x - 1.7.x: up to 42 characters
 *   1.8.x - 1.12.x: up to 66 characters
 *   1.13+: unlimited
 */
@RequiredArgsConstructor
public class LongLine implements ScoreboardLine {

    /** Holder of this line */
    @NotNull
    private final ScoreboardLineHolder holder;

    @Override
    public void register(@NotNull TabPlayer player) {
        ScoreboardPlayerData.LineProperties properties = player.scoreboardData.lineProperties.get(holder);
        if (properties.textProperty.get().isEmpty() && properties.numberFormatProperty.get().isEmpty()) {
            return;
        }
        if (player.getVersion().getMinorVersion() >= 13 && !TAB.getInstance().getConfiguration().getConfig().isPacketEventsCompensation()) {
            holder.updateTeam(player, properties.textProperty.get(), "");
            holder.setScore(player, holder.getForcedPlayerNameStart());
        } else {
            String[] values = splitText(
                    holder.getForcedPlayerNameStart(),
                    holder.getParent().getManager().getCache().get(properties.textProperty.get()).toLegacyText(),
                    player.getVersion().getNetworkId() >= ProtocolVersion.V1_8.getNetworkId() ? Limitations.SCOREBOARD_SCORE_LENGTH_1_8 : Limitations.SCOREBOARD_SCORE_LENGTH_1_7
            );
            properties.scoreName = values[1];
            updateTeam(player, values);
            holder.setScore(player, values[1]);
        }
    }

    @Override
    public void refresh(@NotNull TabPlayer player) {
        if (player.scoreboardData.activeScoreboard != holder.getParent()) return; // Player has different scoreboard displayed
        ScoreboardPlayerData.LineProperties properties = player.scoreboardData.lineProperties.get(holder);
        boolean textUpdated = properties.textProperty.update();
        boolean numberFormatUpdated = properties.numberFormatProperty.update();
        boolean scoreUpdated = properties.scoreProperty.update();
        if (properties.textProperty.get().isEmpty() && properties.numberFormatProperty.get().isEmpty()) {
            player.getScoreboard().removeScore(ScoreboardManagerImpl.OBJECTIVE_NAME, properties.scoreName);
        } else if (textUpdated || numberFormatUpdated || scoreUpdated) {
            if (player.getVersion().getMinorVersion() >= 13 && !TAB.getInstance().getConfiguration().getConfig().isPacketEventsCompensation()) {
                holder.updateTeam(player, properties.textProperty.get(), "");
                holder.setScore(player, holder.getForcedPlayerNameStart());
            } else {
                player.getScoreboard().removeScore(ScoreboardManagerImpl.OBJECTIVE_NAME, properties.scoreName);
                String[] values = splitText(
                        holder.getForcedPlayerNameStart(),
                        holder.getParent().getManager().getCache().get(properties.textProperty.get()).toLegacyText(),
                        player.getVersion().getNetworkId() >= ProtocolVersion.V1_8.getNetworkId() ? Limitations.SCOREBOARD_SCORE_LENGTH_1_8 : Limitations.SCOREBOARD_SCORE_LENGTH_1_7
                );
                updateTeam(player, values);
                holder.setScore(player, values[1]);
                properties.scoreName = values[1];
            }
        }
    }

    private void updateTeam(@NotNull TabPlayer player, @NotNull String[] values) {
        player.getScoreboard().unregisterTeam(holder.getTeamName());
        player.getScoreboard().registerTeam(
                holder.getTeamName(),
                holder.getParent().getManager().getCache().get(values[0]),
                holder.getParent().getManager().getCache().get(values[2]),
                Scoreboard.NameVisibility.NEVER,
                Scoreboard.CollisionRule.NEVER,
                Collections.singletonList(values[1]),
                0,
                TabTextColor.RESET.getLegacyColor()
        );
    }

    @Override
    public void unregister(@NotNull TabPlayer player) {
        player.getScoreboard().removeScore(
                ScoreboardManagerImpl.OBJECTIVE_NAME,
                player.scoreboardData.lineProperties.get(holder).scoreName
        );
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
    @NotNull
    private String[] splitText(@NonNull String playerNameStart, @NonNull String text, int maxNameLength) {
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
     * Splits the text into 2 with given max length of first string
     *
     * @param   string
     *          string to split
     * @param   firstElementMaxLength
     *          max length of first string
     * @return  array of 2 strings where second one might be empty
     */
    private String[] split(@NonNull String string, int firstElementMaxLength) {
        if (string.length() <= firstElementMaxLength) return new String[] {string, ""};
        int splitIndex = firstElementMaxLength;
        if (string.charAt(splitIndex-1) == '§') splitIndex--;
        return new String[] {string.substring(0, splitIndex), string.substring(splitIndex)};
    }
}
