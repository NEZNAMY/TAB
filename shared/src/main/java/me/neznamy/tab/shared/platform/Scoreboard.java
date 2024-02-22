package me.neznamy.tab.shared.platform;

import lombok.AllArgsConstructor;
import lombok.NonNull;
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

/**
 * Scoreboard class for sending scoreboard-related packets.
 * @param   <T>
 *          Platform's TabPlayer class
 */
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

    /**
     * Sets display slot of an objective.
     *
     * @param   slot
     *          Objective slot: 0 = playerlist, 1 = sidebar, 2 = belowname
     * @param   objective
     *          Objective name
     */
    public final void setDisplaySlot(int slot, @NonNull String objective) {
        if (frozen) return;
        setDisplaySlot0(slot, objective);
    }

    /**
     * Sets score of a holder to specified value.
     *
     * @param   objective
     *          Objective to set score on
     * @param   scoreHolder
     *          Name of score holder
     * @param   score
     *          Numeric score value
     * @param   displayName
     *          Display name of score holder (1.20.3+)
     * @param   numberFormat
     *          Number format of score value (1.20.3+)
     */
    public final void setScore(@NonNull String objective, @NonNull String scoreHolder, int score,
                               @Nullable TabComponent displayName, @Nullable TabComponent numberFormat) {
        if (frozen) return;
        if (!registeredObjectives.contains(objective)) {
            error("Tried to update score (%s) without the existence of its requested objective '%s' to player ", scoreHolder, objective);
            return;
        }
        setScore0(objective, scoreHolder, score, displayName, numberFormat);
    }

    /**
     * Removes score from specified objective.
     *
     * @param   objective
     *          Objective to remove score from
     * @param   scoreHolder
     *          Name of score holder to remove score of
     */
    public final void removeScore(@NonNull String objective, @NonNull String scoreHolder) {
        if (frozen) return;
        if (!registeredObjectives.contains(objective)) {
            error("Tried to remove score (%s) without the existence of its requested objective '%s' to player ", scoreHolder, objective);
            return;
        }
        removeScore0(objective, scoreHolder);
    }

    /**
     * Registers new scoreboard objective.
     *
     * @param   objectiveName
     *          Objective name
     * @param   title
     *          Objective title
     * @param   display
     *          Display type: 0 = integer, 1 = hearts
     * @param   numberFormat
     *          Default number format for all scores in this objective (1.20.3+)
     */
    public final void registerObjective(@NonNull String objectiveName, @NonNull String title, int display,
                                  @Nullable TabComponent numberFormat) {
        if (frozen) return;
        if (!registeredObjectives.add(objectiveName)) {
            error("Tried to register duplicated objective %s to player ", objectiveName);
            return;
        }
        registerObjective0(objectiveName, cutTo(title, Limitations.SCOREBOARD_TITLE_PRE_1_13), display, numberFormat);
    }

    /**
     * Unregisters scoreboard objective.
     *
     * @param   objectiveName
     *          Objective name
     */
    public final void unregisterObjective(@NonNull String objectiveName) {
        if (frozen) return;
        if (!registeredObjectives.remove(objectiveName)) {
            error("Tried to unregister non-existing objective %s for player ", objectiveName);
            return;
        }
        unregisterObjective0(objectiveName);
    }

    /**
     * Updates objective properties.
     *
     * @param   objectiveName
     *          Objective name
     * @param   title
     *          New objective title
     * @param   display
     *          New objective display type: 0 = integer, 1 = hearts
     * @param   numberFormat
     *          New default number format for all scores
     */
    public final void updateObjective(@NonNull String objectiveName, @NonNull String title, int display,
                                @Nullable TabComponent numberFormat) {
        if (frozen) return;
        if (!registeredObjectives.contains(objectiveName)) {
            error("Tried to modify non-existing objective %s for player ", objectiveName);
            return;
        }
        updateObjective0(objectiveName, cutTo(title, Limitations.SCOREBOARD_TITLE_PRE_1_13), display, numberFormat);
    }

    /**
     * Registers new team into the scoreboard.
     *
     * @param   name
     *          Team name
     * @param   prefix
     *          Team prefix
     * @param   suffix
     *          Team suffix
     * @param   visibility
     *          Team nametag visibility
     * @param   collision
     *          Team collision rule
     * @param   players
     *          Players to add to the team
     * @param   options
     *          Team options:
     *              0x01 - Allow friendly fire
     *              0x02 - Can see friendly invisibles
     * @param   color
     *          Team color (name color and prefix/suffix color start)
     */
    public final void registerTeam(@NonNull String name, @NonNull String prefix, @NonNull String suffix,
                                   @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                                   @NonNull Collection<String> players, int options, @NonNull EnumChatFormat color) {
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

    /**
     * Unregisters team from the scoreboard.
     *
     * @param   name
     *          Team name
     */
    public final void unregisterTeam(@NonNull String name) {
        if (frozen) return;
        if (!registeredTeams.remove(name)) {
            error("Tried to unregister non-existing team %s for player ", name);
            return;
        }
        unregisterTeam0(name);
    }

    /**
     * Updates team properties.
     *
     * @param   name
     *          Team name
     * @param   prefix
     *          New team prefix
     * @param   suffix
     *          New team suffix
     * @param   visibility
     *          New team nametag visibility
     * @param   collision
     *          New team collision rule
     * @param   options
     *          New team options:
     *              0x01 - Allow friendly fire
     *              0x02 - Can see friendly invisibles
     * @param   color
     *          New team color (name color and prefix/suffix color start)
     */
    public final void updateTeam(@NonNull String name, @NonNull String prefix, @NonNull String suffix,
                                 @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                                 int options, @NonNull EnumChatFormat color) {
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

    /**
     * Prints a debug message if attempted to perform an invalid operation.
     *
     * @param   format
     *          Message format
     * @param   args
     *          Format arguments
     */
    private void error(@NonNull String format, @NonNull Object... args) {
        TAB.getInstance().debug(String.format(format, args) + player.getName());
    }

    /**
     * Marks for freeze. While frozen, no packets will be sent.
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
    public boolean isTeamPacket(@NonNull Object packet) {
        return false;
    }

    /**
     * Removes all real players from team if packet does not come from TAB and reports this to override log.
     *
     * @param   packet
     *          Packet to process
     */
    public void onTeamPacket(@NonNull Object packet) {
        // Empty by default, overridden by Bukkit, BungeeCord and Fabric
    }

    /**
     * Returns {@code true} if packet is a display objective packet, {@code false} if not.
     *
     * @param   packet
     *          Minecraft packet to check
     * @return  {@code true} if is a display objective packet, {@code false} if not.
     */
    public boolean isDisplayObjective(@NonNull Object packet) {
        return false;
    }

    /**
     * Processes display objective packet.
     *
     * @param   packet
     *          Packet to process
     */
    public void onDisplayObjective(@NonNull Object packet) {
        // Empty by default, overridden by Bukkit, BungeeCord and Fabric
    }

    /**
     * Returns {@code true} if packet is an objective packet, {@code false} if not.
     *
     * @param   packet
     *          Minecraft packet to check
     * @return  {@code true} if is an objective packet, {@code false} if not.
     */
    public boolean isObjective(@NonNull Object packet) {
        return false;
    }

    /**
     * Processes objective packet.
     *
     * @param   packet
     *          Packet to process
     */
    public void onObjective(@NonNull Object packet) {
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
    public static TabPlayer getPlayer(@NonNull String name) {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (p.getNickname().equals(name))
                return p; // Nicked name
        }
        return TAB.getInstance().getPlayer(name); // Try original name
    }

    /**
     * Logs a message into anti-override log when blocking attempt to add
     * a player into a team.
     *
     * @param   team
     *          Team name from another source
     * @param   player
     *          Player who was about to be added into the team
     * @param   expectedTeam
     *          Expected name of the team
     */
    public static void logTeamOverride(@NonNull String team, @NonNull String player, @NonNull String expectedTeam) {
        String message = "Blocked attempt to add player " + player + " into team " + team + " (expected team: " + expectedTeam + ")";
        //not logging the same message for every online player who received the packet
        if (!message.equals(lastTeamOverrideMessage)) {
            lastTeamOverrideMessage = message;
            TAB.getInstance().getErrorManager().printError(message, Collections.emptyList(), false, TAB.getInstance().getErrorManager().getAntiOverrideLog());
        }
    }

    protected abstract void setDisplaySlot0(int slot, @NonNull String objective);

    protected abstract void setScore0(@NonNull String objective, @NonNull String scoreHolder, int score,
                                   @Nullable TabComponent displayName, @Nullable TabComponent numberFormat);

    protected abstract void removeScore0(@NonNull String objective, @NonNull String scoreHolder);

    protected abstract void registerObjective0(@NonNull String objectiveName, @NonNull String title,
                                            int display, @Nullable TabComponent numberFormat);

    protected abstract void unregisterObjective0(@NonNull String objectiveName);

    protected abstract void updateObjective0(@NonNull String objectiveName, @NonNull String title,
                                          int display, @Nullable TabComponent numberFormat);

    protected abstract void registerTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix,
                                          @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                                          @NonNull Collection<String> players, int options, @NonNull EnumChatFormat color);

    protected abstract void unregisterTeam0(@NonNull String name);

    protected abstract void updateTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix,
                                        @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                                        int options, @NonNull EnumChatFormat color);

    /**
     * Team collision rule enum.
     */
    @AllArgsConstructor
    public enum CollisionRule {

        /** Always pushes all players */
        ALWAYS("always"),

        /** Never pushes anyone */
        NEVER("never"),

        /** Only pushes players from other teams */
        PUSH_OTHER_TEAMS("pushOtherTeams"),

        /** Only pushes players from own team */
        PUSH_OWN_TEAM("pushOwnTeam");

        /** Map of code name to enum constant */
        private static final Map<String, CollisionRule> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(collisionRule -> collisionRule.string, collisionRule -> collisionRule));

        /** Code name of this constant */
        private final String string;

        @Override
        public String toString() {
            return string;
        }

        /**
         * Returns enum constant from code name. If invalid, {@link #ALWAYS}
         * is returned.
         *
         * @param   name
         *          Code name of the collision rule
         * @return  Enum constant from given code name
         */
        @NotNull
        public static CollisionRule getByName(@NotNull String name) {
            return BY_NAME.getOrDefault(name, ALWAYS);
        }
    }

    /**
     * Nametag visibility enum.
     */
    @AllArgsConstructor
    public enum NameVisibility {

        /** Name can be seen by everyone */
        ALWAYS("always"),

        /** Name cannot be seen by anyone */
        NEVER("never"),

        /** Name is hidden from other teams */
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),

        /** Name is hidden from own team */
        HIDE_FOR_OWN_TEAM("hideForOwnTeam");

        /** Map of code name to enum constant */
        private static final Map<String, NameVisibility> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(visibility -> visibility.string, visibility -> visibility));

        /** Code name of this constant */
        private final String string;

        @Override
        public String toString() {
            return string;
        }

        /**
         * Returns enum constant from code name. If invalid, {@link #ALWAYS}
         * is returned.
         *
         * @param   name
         *          Code name of the collision rule
         * @return  Enum constant from given code name
         */
        public static NameVisibility getByName(String name) {
            return BY_NAME.getOrDefault(name, ALWAYS);
        }
    }

    /**
     * Class containing scoreboard objective action constants.
     */
    public static class ObjectiveAction {

        /** Register objective action */
        public static final int REGISTER = 0;

        /** Unregister objective action */
        public static final int UNREGISTER = 1;

        /** Update objective action */
        public static final int UPDATE = 2;
    }

    /**
     * Class containing scoreboard objective health display constants.
     */
    public static class HealthDisplay {

        /** INTEGER display type */
        public static final int INTEGER = 0;

        /** HEARTS display type (1.8+) */
        public static final int HEARTS = 1;
    }

    /**
     * Class containing scoreboard display slot constants.
     */
    public static class DisplaySlot {

        /** Playerlist slot in tablist aligned to the right */
        public static final int PLAYER_LIST = 0;

        /** Sidebar slot on the right */
        public static final int SIDEBAR = 1;

        /** Belowname slot below player nametags */
        public static final int BELOW_NAME = 2;
    }

    /**
     * Class containing scoreboard score action constants.
     */
    public static class ScoreAction {

        /** Sets score (adds if not present) */
        public static final int CHANGE = 0;

        /** Removes score */
        public static final int REMOVE = 1;
    }

    /**
     * Class containing scoreboard team action constants.
     */
    @SuppressWarnings("unused")
    public static class TeamAction {

        /** Creates team */
        public static final int CREATE = 0;

        /** Removes team */
        public static final int REMOVE = 1;

        /** Updates team properties */
        public static final int UPDATE = 2;

        /** Adds player into the team */
        public static final int ADD_PLAYER = 3;

        /** Removes player from the team */
        public static final int REMOVE_PLAYER = 4;
    }
}
