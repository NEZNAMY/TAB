package me.neznamy.tab.shared.platform;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class Scoreboard<T extends TabPlayer> {

    /** Player this scoreboard belongs to */
    protected final T player;

    /** Scoreboard teams player has registered */
    private final Set<String> registeredTeams = new HashSet<>();

    /** Scoreboard objectives player has registered */
    private final Set<String> registeredObjectives = new HashSet<>();

    public void setScore(@NotNull String objective, @NotNull String playerName, int score) {
        if (!registeredObjectives.contains(objective)) {
            error("Tried to update score (%s) without the existence of its requested objective '%s' to player ", playerName, objective);
            return;
        }
        setScore0(objective, playerName, score);
    }

    public void removeScore(@NotNull String objective, @NotNull String playerName) {
        if (!registeredObjectives.contains(objective)) {
            error("Tried to remove score (%s) without the existence of its requested objective '%s' to player ", playerName, objective);
            return;
        }
        removeScore0(objective, playerName);
    }

    public void registerObjective(@NotNull String objectiveName, @NotNull String title, @NotNull HealthDisplay display) {
        if (!registeredObjectives.add(objectiveName)) {
            error("Tried to register duplicated objective %s to player ", objectiveName);
            return;
        }
        registerObjective0(objectiveName, cutTo(title, 32), display);
    }

    public void unregisterObjective(@NotNull String objectiveName) {
        if (!registeredObjectives.remove(objectiveName)) {
            error("Tried to unregister non-existing objective %s for player ", objectiveName);
            return;
        }
        unregisterObjective0(objectiveName);
    }

    public void updateObjective(@NotNull String objectiveName, @NotNull String title, @NotNull HealthDisplay display) {
        if (!registeredObjectives.contains(objectiveName)) {
            error("Tried to modify non-existing objective %s for player ", objectiveName);
            return;
        }
        updateObjective0(objectiveName, cutTo(title, 32), display);
    }

    public void registerTeam(@NotNull String name, @NotNull String prefix, @NotNull String suffix, @NotNull NameVisibility visibility,
                             @NotNull CollisionRule collision, @NotNull Collection<String> players, int options) {
        if (!registeredTeams.add(name)) {
            error("Tried to register duplicated team %s to player ", name);
            return;
        }
        registerTeam0(name, cutTo(prefix, 16), cutTo(suffix, 16), visibility, collision, players, options);
    }

    public void unregisterTeam(@NotNull String name) {
        if (!registeredTeams.remove(name)) {
            error("Tried to unregister non-existing team %s for player ", name);
            return;
        }
        unregisterTeam0(name);
    }

    public void updateTeam(@NotNull String name, @NotNull String prefix, @NotNull String suffix, @NotNull NameVisibility visibility,
                           @NotNull CollisionRule collision, int options) {
        if (!registeredTeams.contains(name)) {
            error("Tried to modify non-existing team %s for player ", name);
            return;
        }
        updateTeam0(name, cutTo(prefix, 16), cutTo(suffix, 16), visibility, collision, options);
    }

    private void error(@NotNull String format, @NotNull Object... args) {
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

    public abstract void setDisplaySlot(@NotNull DisplaySlot slot, @NotNull String objective);

    public abstract void setScore0(@NotNull String objective, @NotNull String player, int score);

    public abstract void removeScore0(@NotNull String objective, @NotNull String player);

    public abstract void registerObjective0(@NotNull String objectiveName, @NotNull String title, @NotNull HealthDisplay display);

    public abstract void unregisterObjective0(@NotNull String objectiveName);

    public abstract void updateObjective0(@NotNull String objectiveName, @NotNull String title, @NotNull HealthDisplay display);

    public abstract void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix, @NotNull NameVisibility visibility,
                                                 @NotNull CollisionRule collision, @NotNull Collection<String> players, int options);

    public abstract void unregisterTeam0(@NotNull String name);

    public abstract void updateTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix, @NotNull NameVisibility visibility,
                                     @NotNull CollisionRule collision, int options);

    public enum DisplaySlot { PLAYER_LIST, SIDEBAR, BELOW_NAME }

    @AllArgsConstructor
    public enum CollisionRule {

        ALWAYS("always"),
        NEVER("never"),
        PUSH_OTHER_TEAMS("pushOtherTeams"),
        PUSH_OWN_TEAM("pushOwnTeam");

        private static final Map<String, CollisionRule> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(collisionRule -> collisionRule.string, collisionRule -> collisionRule));
        private final String string;

        @Override
        public String toString() {
            return string;
        }

        public static CollisionRule getByName(String name) {
            return BY_NAME.getOrDefault(name, ALWAYS);
        }
    }

    @AllArgsConstructor
    public enum NameVisibility {

        ALWAYS("always"),
        NEVER("never"),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam");

        private static final Map<String, NameVisibility> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(visibility -> visibility.string, visibility -> visibility));
        private final String string;

        @Override
        public String toString() {
            return string;
        }

        public static NameVisibility getByName(String name) {
            return BY_NAME.getOrDefault(name, ALWAYS);
        }
    }
    
    public enum HealthDisplay {
        
        INTEGER, HEARTS
    }
}
