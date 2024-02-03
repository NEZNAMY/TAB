package me.neznamy.tab.shared.platform;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class Scoreboard<T extends TabPlayer> {

    /** Static to prevent spam when packet is sent to each player */
    private static String lastTeamOverrideMessage;

    /** Player this scoreboard belongs to */
    protected final T player;

    /** Scoreboard teams player has registered */
    private final Set<String> registeredTeams = new HashSet<>();

    /** Scoreboard objectives player has registered */
    private final Set<String> registeredObjectives = new HashSet<>();

    /** Flag tracking time between Login packet send and its processing */
    private boolean frozen;

    public final void setDisplaySlot(int slot, @NotNull String objective) {
        if (frozen) return;
        setDisplaySlot0(slot, objective);
    }

    public final void setScore(@NotNull String objective, @NotNull String scoreHolder, int score,
                               @Nullable TabComponent displayName, @Nullable TabComponent numberFormat) {
        if (frozen) return;
        if (!registeredObjectives.contains(objective)) {
            error("Tried to update score (%s) without the existence of its requested objective '%s' to player ", scoreHolder, objective);
            return;
        }
        setScore0(objective, scoreHolder, score, displayName, numberFormat);
    }

    public final void removeScore(@NotNull String objective, @NotNull String scoreHolder) {
        if (frozen) return;
        if (!registeredObjectives.contains(objective)) {
            error("Tried to remove score (%s) without the existence of its requested objective '%s' to player ", scoreHolder, objective);
            return;
        }
        removeScore0(objective, scoreHolder);
    }

    public final void registerObjective(@NotNull String objectiveName, @NotNull String title, int display,
                                  @Nullable TabComponent numberFormat) {
        if (frozen) return;
        if (!registeredObjectives.add(objectiveName)) {
            error("Tried to register duplicated objective %s to player ", objectiveName);
            return;
        }
        registerObjective0(objectiveName, cutTo(title, Limitations.SCOREBOARD_TITLE_PRE_1_13), display, numberFormat);
    }

    public final void unregisterObjective(@NotNull String objectiveName) {
        if (frozen) return;
        if (!registeredObjectives.remove(objectiveName)) {
            error("Tried to unregister non-existing objective %s for player ", objectiveName);
            return;
        }
        unregisterObjective0(objectiveName);
    }

    public final void updateObjective(@NotNull String objectiveName, @NotNull String title, int display,
                                @Nullable TabComponent numberFormat) {
        if (frozen) return;
        if (!registeredObjectives.contains(objectiveName)) {
            error("Tried to modify non-existing objective %s for player ", objectiveName);
            return;
        }
        updateObjective0(objectiveName, cutTo(title, Limitations.SCOREBOARD_TITLE_PRE_1_13), display, numberFormat);
    }

    public final void registerTeam(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                                   @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                                   @NotNull Collection<String> players, int options, @NotNull EnumChatFormat color) {
        if (frozen) return;
        if (!registeredTeams.add(name)) {
            error("Tried to register duplicated team %s to player ", name);
            return;
        }
        registerTeam0(
                name,
                cutTo(prefix, Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13),
                cutTo(suffix, Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13),
                visibility,
                collision,
                players,
                options,
                color
        );
    }

    public final void unregisterTeam(@NotNull String name) {
        if (frozen) return;
        if (!registeredTeams.remove(name)) {
            error("Tried to unregister non-existing team %s for player ", name);
            return;
        }
        unregisterTeam0(name);
    }

    public final void updateTeam(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                                 @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                                 int options, @NotNull EnumChatFormat color) {
        if (frozen) return;
        if (!registeredTeams.contains(name)) {
            error("Tried to modify non-existing team %s for player ", name);
            return;
        }
        updateTeam0(
                name,
                cutTo(prefix, Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13),
                cutTo(suffix, Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13),
                visibility,
                collision,
                options,
                color
        );
    }

    private void error(@NotNull String format, @NotNull Object... args) {
        TAB.getInstance().debug(String.format(format, args) + player.getName());
    }

    /**
     * Marks for freeze.
     */
    public void freeze() {
        frozen = true;
    }

    /**
     * Clears frozen flag and clears maps of registered teams and objectives.
     */
    public void unfreeze() {
        registeredTeams.clear();
        registeredObjectives.clear();
        frozen = false;
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

    /**
     * Returns {@code true} if packet is a team packet, {@code false} if not.
     *
     * @param   packet
     *          Minecraft packet to check
     * @return  {@code true} if is a team packet, {@code false} if not.
     */
    public boolean isTeamPacket(@NotNull Object packet) {
        return false;
    }

    /**
     * Removes all real players from team if packet does not come from TAB and reports this to override log.
     *
     * @param   packet
     *          Packet to process
     */
    public void onTeamPacket(@NotNull Object packet) {
        // Empty by default, overridden by Bukkit, BungeeCord and Fabric
    }

    /**
     * Returns {@code true} if packet is a display objective packet, {@code false} if not.
     *
     * @param   packet
     *          Minecraft packet to check
     * @return  {@code true} if is a display objective packet, {@code false} if not.
     */
    public boolean isDisplayObjective(@NotNull Object packet) {
        return false;
    }

    /**
     * Processes display objective packet.
     *
     * @param   packet
     *          Packet to process
     */
    public void onDisplayObjective(@NotNull Object packet) {
        // Empty by default, overridden by Bukkit, BungeeCord and Fabric
    }

    /**
     * Returns {@code true} if packet is an objective packet, {@code false} if not.
     *
     * @param   packet
     *          Minecraft packet to check
     * @return  {@code true} if is an objective packet, {@code false} if not.
     */
    public boolean isObjective(@NotNull Object packet) {
        return false;
    }

    /**
     * Processes objective packet.
     *
     * @param   packet
     *          Packet to process
     */
    public void onObjective(@NotNull Object packet) {
        // Empty by default, overridden by Bukkit, BungeeCord and Fabric
    }

    /**
     * Returns player by given nickname.
     *
     * @param   name
     *          Nickname of player
     * @return  Player from given nickname
     */
    @Nullable
    public static TabPlayer getPlayer(@NotNull String name) {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (p.getNickname().equals(name))
                return p; // Nicked name
        }
        return TAB.getInstance().getPlayer(name); // Try original name
    }

    public static void logTeamOverride(@NotNull String team, @NotNull String player, @NotNull String expectedTeam) {
        String message = "Blocked attempt to add player " + player + " into team " + team + " (expected team: " + expectedTeam + ")";
        //not logging the same message for every online player who received the packet
        if (!message.equals(lastTeamOverrideMessage)) {
            lastTeamOverrideMessage = message;
            TAB.getInstance().getErrorManager().printError(message, Collections.emptyList(), false, TAB.getInstance().getErrorManager().getAntiOverrideLog());
        }
    }

    protected abstract void setDisplaySlot0(int slot, @NotNull String objective);

    protected abstract void setScore0(@NotNull String objective, @NotNull String scoreHolder, int score,
                                   @Nullable TabComponent displayName, @Nullable TabComponent numberFormat);

    protected abstract void removeScore0(@NotNull String objective, @NotNull String scoreHolder);

    protected abstract void registerObjective0(@NotNull String objectiveName, @NotNull String title,
                                            int display, @Nullable TabComponent numberFormat);

    protected abstract void unregisterObjective0(@NotNull String objectiveName);

    protected abstract void updateObjective0(@NotNull String objectiveName, @NotNull String title,
                                          int display, @Nullable TabComponent numberFormat);

    protected abstract void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                                          @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                                          @NotNull Collection<String> players, int options, @NotNull EnumChatFormat color);

    protected abstract void unregisterTeam0(@NotNull String name);

    protected abstract void updateTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                                        @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                                        int options, @NotNull EnumChatFormat color);

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

    public static class ObjectiveAction {

        public static final int REGISTER = 0;
        public static final int UNREGISTER = 1;
        public static final int UPDATE = 2;
    }

    public static class HealthDisplay {

        public static final int INTEGER = 0;
        public static final int HEARTS = 1;
    }

    public static class DisplaySlot {

        public static final int PLAYER_LIST = 0;
        public static final int SIDEBAR = 1;
        public static final int BELOW_NAME = 2;
    }

    public static class ScoreAction {

        public static final int CHANGE = 0;
        public static final int REMOVE = 1;
    }

    @SuppressWarnings("unused")
    public static class TeamAction {

        public static final int CREATE = 0;
        public static final int REMOVE = 1;
        public static final int UPDATE = 2;
        public static final int ADD_PLAYER = 3;
        public static final int REMOVE_PLAYER = 4;
    }
}
