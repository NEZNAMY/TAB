package me.neznamy.tab.shared.features.scoreboard.lines;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardPlayerData;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Line of text that uses only team prefix and suffix to display text.
 * This allows for changes without having to remove a score and add a new one, possibly
 * causing a flicker on update.
 * Limitations:
 *   1.12-: Up to 32 characters depending on color / magic codes
 *   1.13+: Unlimited
 */
@RequiredArgsConstructor
public class NoFlickerLine implements ScoreboardLine {

    /** Holder of this line */
    @NotNull
    private final ScoreboardLineHolder holder;

    @Override
    public void register(@NonNull TabPlayer player) {
        ScoreboardPlayerData.LineProperties properties = player.scoreboardData.lineProperties.get(holder);
        if (properties.textProperty.get().isEmpty() && properties.numberFormatProperty.get().isEmpty()) {
            return;
        }
        String[] prefixSuffix = split(player, properties.textProperty.get());
        holder.updateTeam(player, prefixSuffix[0], prefixSuffix[1]);
        holder.setScore(player, holder.getForcedPlayerNameStart());
    }

    @Override
    public void refresh(@NotNull TabPlayer player) {
        if (player.scoreboardData.activeScoreboard != holder.getParent()) return; // Player has different scoreboard displayed
        ScoreboardPlayerData.LineProperties properties = player.scoreboardData.lineProperties.get(holder);
        boolean emptyBefore = properties.textProperty.get().isEmpty() && properties.numberFormatProperty.get().isEmpty();
        if (properties.textProperty.update()) {
            String[] prefixSuffix = split(player, properties.textProperty.get());
            holder.updateTeam(player, prefixSuffix[0], prefixSuffix[1]);
        }
        boolean numberFormatUpdated = properties.numberFormatProperty.update();
        boolean scoreUpdated = properties.scoreProperty.update();
        if (properties.textProperty.get().isEmpty() && properties.numberFormatProperty.get().isEmpty()) {
            player.getScoreboard().removeScore(ScoreboardManagerImpl.OBJECTIVE_NAME, holder.getForcedPlayerNameStart());
        } else if (emptyBefore || numberFormatUpdated || scoreUpdated) {
            holder.setScore(player, holder.getForcedPlayerNameStart());
        }
    }

    @Override
    public void unregister(@NotNull TabPlayer player) {
        player.getScoreboard().removeScore(ScoreboardManagerImpl.OBJECTIVE_NAME, holder.getForcedPlayerNameStart());
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
    @NotNull
    private String[] split(@NonNull TabPlayer p, @NonNull String text) {
        if (p.getVersion().getNetworkId() < ProtocolVersion.V1_16.getNetworkId()) {
            text = holder.getParent().getManager().getCache().get(text).toLegacyText(); //converting RGB to legacy here to avoid splitting in the middle of RGB code
        }
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
            suffix.insert(0, getLastColors(holder.getParent().getManager().getCache().get(prefixString).toLegacyText()));
            return new String[] {prefixString, suffix.toString()};
        } else {
            return new String[] {text, ""};
        }
    }
}
