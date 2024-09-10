package me.neznamy.tab.shared.platform;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Scoreboard class for sending scoreboard objectives and teams.
 */
public interface Scoreboard {

    /**
     * Registers new scoreboard objective.
     *
     * @param   displaySlot
     *          Display slot
     * @param   objectiveName
     *          Objective name
     * @param   title
     *          Objective title
     * @param   display
     *          Display type
     * @param   numberFormat
     *          Default number format for all scores in this objective (1.20.3+)
     */
    void registerObjective(@NonNull DisplaySlot displaySlot, @NonNull String objectiveName, @NonNull TabComponent title,
                           @NonNull HealthDisplay display, @Nullable TabComponent numberFormat);

    /**
     * Unregisters scoreboard objective.
     *
     * @param   objectiveName
     *          Objective name
     */
    void unregisterObjective(@NonNull String objectiveName);

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
    void updateObjective(@NonNull String objectiveName, @NonNull TabComponent title, HealthDisplay display, @Nullable TabComponent numberFormat);
    
    /**
     * Sets score of a holder to specified value.
     *
     * @param   objectiveName
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
    void setScore(@NonNull String objectiveName, @NonNull String scoreHolder, int score,
                  @Nullable TabComponent displayName, @Nullable TabComponent numberFormat);

    /**
     * Removes score from specified objective.
     *
     * @param   objectiveName
     *          Objective to remove score from
     * @param   scoreHolder
     *          Name of score holder to remove score of
     */
    void removeScore(@NonNull String objectiveName, @NonNull String scoreHolder);
    
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
    void registerTeam(@NonNull String name, @NonNull TabComponent prefix, @NonNull TabComponent suffix,
                      @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                      @NonNull Collection<String> players, int options, @NonNull EnumChatFormat color);

    /**
     * Unregisters team from the scoreboard.
     *
     * @param   teamName
     *          Team name
     */
    void unregisterTeam(@NonNull String teamName);

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
    void updateTeam(@NonNull String name, @NonNull TabComponent prefix, @NonNull TabComponent suffix,
                    @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                    int options, @NonNull EnumChatFormat color);

    /**
     * Updates team prefix, suffix and color.
     *
     * @param   name
     *          Team name
     * @param   prefix
     *          New team prefix
     * @param   suffix
     *          New team suffix
     * @param   color
     *          New team color (name color and prefix/suffix color start)
     */
    void updateTeam(@NonNull String name, @NonNull TabComponent prefix, @NonNull TabComponent suffix, @NonNull EnumChatFormat color);

    /**
     * Updates collision rule.
     *
     * @param   name
     *          Team name
     * @param   collision
     *          New collision rule
     */
    void updateTeam(@NonNull String name, @NonNull CollisionRule collision);

    /**
     * Updates visibility rule.
     *
     * @param   name
     *          Team name
     * @param   visibility
     *          New visibility rule
     */
    void updateTeam(@NonNull String name, @NonNull NameVisibility visibility);

    /**
     * Renames a team.
     *
     * @param   oldName
     *          Current team name
     * @param   newName
     *          New team name
     */
    void renameTeam(@NonNull String oldName, @NonNull String newName);

    /**
     * Resends all objectives and teams.
     */
    void resend();

    /**
     * Clears the entire scoreboard by unregistering all objectives and teams.
     */
    void clear();

    /**
     * Team collision rule enum.
     */
    @AllArgsConstructor
    enum CollisionRule {

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
    enum NameVisibility {

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
    class ObjectiveAction {

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
    enum HealthDisplay {

        /** INTEGER display type */
        INTEGER,

        /** HEARTS display type (1.8+) */
        HEARTS
    }

    /**
     * Class containing scoreboard display slot constants.
     */
    enum DisplaySlot {

        /** Playerlist slot in tablist aligned to the right */
        PLAYER_LIST,

        /** Sidebar slot on the right */
        SIDEBAR,

        /** Belowname slot below player nametags */
        BELOW_NAME
    }

    /**
     * Class containing scoreboard score action constants.
     */
    class ScoreAction {

        /** Sets score (adds if not present) */
        public static final int CHANGE = 0;

        /** Removes score */
        public static final int REMOVE = 1;
    }

    /**
     * Class containing scoreboard team action constants.
     */
    @SuppressWarnings("unused")
    class TeamAction {

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
