package me.neznamy.tab.shared.platform;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public abstract class PlatformScoreboard<T extends TabPlayer> {

    /** Player this scoreboard belongs to */
    protected final T player;

    /** Scoreboard teams player has registered */
    private final Set<String> registeredTeams = new HashSet<>();

    /** Scoreboard objectives player has registered */
    private final Set<String> registeredObjectives = new HashSet<>();

    public void setScore(@NonNull String objective, @NonNull String playerName, int score) {
        if (!registeredObjectives.contains(objective)) {
            error("Tried to update score (%s) without the existence of its requested objective '%s' to player ", playerName, objective);
            return;
        }
        setScore0(objective, playerName, score);
    }

    public void removeScore(@NonNull String objective, @NonNull String playerName) {
        if (!registeredObjectives.contains(objective)) {
            error("Tried to remove score (%s) without the existence of its requested objective '%s' to player ", playerName, objective);
            return;
        }
        removeScore0(objective, playerName);
    }

    public void registerObjective(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        if (!registeredObjectives.add(objectiveName)) {
            error("Tried to register duplicated objective %s to player ", objectiveName);
            return;
        }
        registerObjective0(objectiveName, cutTo(title, 32), hearts);
    }

    public void unregisterObjective(@NonNull String objectiveName) {
        if (!registeredObjectives.remove(objectiveName)) {
            error("Tried to unregister non-existing objective %s for player ", objectiveName);
            return;
        }
        unregisterObjective0(objectiveName);
    }

    public void updateObjective(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        if (!registeredObjectives.contains(objectiveName)) {
            error("Tried to modify non-existing objective %s for player ", objectiveName);
            return;
        }
        updateObjective0(objectiveName, cutTo(title, 32), hearts);
    }

    public void registerTeam(@NonNull String name, @NonNull String prefix, @NonNull String suffix, @NonNull String visibility,
                             @NonNull String collision, @NonNull Collection<String> players, int options) {
        if (!registeredTeams.add(name)) {
            error("Tried to register duplicated team %s to player ", name);
            return;
        }
        registerTeam0(name, cutTo(prefix, 16), cutTo(suffix, 16), visibility, collision, players, options);
    }

    public void unregisterTeam(@NonNull String name) {
        if (!registeredTeams.remove(name)) {
            error("Tried to unregister non-existing team %s for player ", name);
            return;
        }
        unregisterTeam0(name);
    }

    public void updateTeam(@NonNull String name, @NonNull String prefix, @NonNull String suffix, @NonNull String visibility,
                           @NonNull String collision, int options) {
        if (!registeredTeams.contains(name)) {
            error("Tried to modify non-existing team %s for player ", name);
            return;
        }
        updateTeam0(name, cutTo(prefix, 16), cutTo(suffix, 16), visibility, collision, options);
    }

    private void error(@NonNull String format, @NonNull Object... args) {
        TAB.getInstance().getErrorManager().printError(String.format(format, args) + player.getName());
    }

    /**
     * Clears maps of registered teams and objectives on server switch, as proxy sends Login packet
     */
    public void clearRegisteredObjectives() {
        registeredTeams.clear();
        registeredObjectives.clear();
    }

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
    private String cutTo(@Nullable String string, int length) {
        if (player.getVersion().getMinorVersion() >= 13) return string;
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

    public abstract void setDisplaySlot(@NonNull DisplaySlot slot, @NonNull String objective);

    public abstract void setScore0(@NonNull String objective, @NonNull String player, int score);

    public abstract void removeScore0(@NonNull String objective, @NonNull String player);

    public abstract void registerObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts);

    public abstract void unregisterObjective0(@NonNull String objectiveName);

    public abstract void updateObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts);

    public abstract void registerTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix, @NonNull String visibility,
                                                 @NonNull String collision, @NonNull Collection<String> players, int options);

    public abstract void unregisterTeam0(@NonNull String name);

    public abstract void updateTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix, @NonNull String visibility,
                                     @NonNull String collision, int options);

    public enum DisplaySlot { PLAYER_LIST, SIDEBAR, BELOW_NAME }
}
