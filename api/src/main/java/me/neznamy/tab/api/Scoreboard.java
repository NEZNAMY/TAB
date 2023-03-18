package me.neznamy.tab.api;

import lombok.NonNull;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.rgb.RGBUtils;

import java.util.Collection;

public interface Scoreboard {

    /**
     * Sets objective display slot of specified objective
     *
     * @param   slot
     *          Display slot
     * @param   objective
     *          Objective name
     */
    void setDisplaySlot(DisplaySlot slot, @NonNull String objective);

    /**
     * Sets scoreboard score
     * @param   objective
     *          Objective name
     * @param   player
     *          Affected player
     * @param   score
     *          New score value
     */
    void setScore(@NonNull String objective, @NonNull String player, int score);

    /**
     * Removes scoreboard score
     *
     * @param   objective
     *          Objective to remove from
     * @param   player
     *          Player to remove from sidebar
     */
    void removeScore(@NonNull String objective, @NonNull String player);

    void registerObjective(@NonNull String objectiveName, @NonNull String title, boolean hearts);

    void unregisterObjective(@NonNull String objectiveName);

    void updateObjective(@NonNull String objectiveName, @NonNull String title, boolean hearts);

    void registerTeam(@NonNull String name, String prefix, String suffix, String visibility,
                                String collision, Collection<String> players, int options);

    void unregisterTeam(@NonNull String name);

    void updateTeam(@NonNull String name, String prefix, String suffix, String visibility, String collision, int options);

    /**
     * Cuts given string to specified character length (or length-1 if last character is a color character)
     * and translates RGB to legacy colors. If string is not that long, the original string is returned.
     * RGB codes are converted into legacy, since cutting is only needed for &lt;1.13.
     * If {@code string} is {@code null}, empty string is returned.
     *
     * @param   string
     *          String to cut
     * @param   length
     *          Length to cut to
     * @return  string cut to {@code length} characters
     */
    default String cutTo(String string, int length) {
        if (string == null) return "";
        String legacyText = string;
        if (string.contains("#")) {
            //converting RGB to legacy colors
            legacyText = RGBUtils.getInstance().convertRGBtoLegacy(string);
        }
        if (legacyText.length() <= length) return legacyText;
        if (legacyText.charAt(length-1) == EnumChatFormat.COLOR_CHAR) {
            return legacyText.substring(0, length-1); //cutting one extra character to prevent prefix ending with "&"
        } else {
            return legacyText.substring(0, length);
        }
    }
}
