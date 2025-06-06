package me.neznamy.tab.shared.platform.decorators;

import lombok.*;
import me.neznamy.tab.shared.TAB;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.chat.TextColor;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An abstract class for adding safety checks into Scoreboard-related functions
 * to prevent various issues:<p>
 * - Client crash when performing an invalid action (1.5 - 1.7)<p>
 * - Client crash on server switch before login packet is sent on BungeeCord (1.20.3+)<p>
 * - Disconnect with "Network Protocol Error" when performing an invalid action (1.20.5+)<p>
 * - Geyser console spam when performing an invalid action (Bedrock)
 *
 * @param   <T>
 *          Platform's TabPlayer class
 */
@RequiredArgsConstructor
@Setter
public abstract class SafeScoreboard<T extends TabPlayer> implements Scoreboard {

    /** Static to prevent spam when packet is sent to each player */
    private static String lastTeamOverrideMessage;

    /** Player this scoreboard belongs to */
    @Getter
    protected final T player;

    /** Map of blocked team adds, key is player and value is team name */
    private final Map<String, String> blockedTeamAdds = new HashMap<>();

    /** Map of allowed team adds, key is player and value is team name */
    private final Map<String, String> allowedTeamAdds = new HashMap<>();

    /** Flag tracking time between Login packet send and its processing */
    private boolean frozen;
    
    /** Registered objectives */
    private final Map<String, Objective> objectives = new ConcurrentHashMap<>();

    /** Registered teams */
    private final Map<String, Team> teams = new ConcurrentHashMap<>();

    @Override
    public synchronized void registerObjective(@NonNull String objectiveName, @NonNull TabComponent title,
                                        @NonNull HealthDisplay display, @Nullable TabComponent numberFormat) {
        Objective existing = objectives.get(objectiveName);
        if (existing != null) {
            error("Tried to register duplicated objective %s to player ", objectiveName);
            return;
        }
        Objective objective = new Objective(objectiveName, title, display, numberFormat, null);
        objectives.put(objectiveName, objective);
        if (frozen) return;
        registerObjective(objective);
    }

    @Override
    public synchronized void setDisplaySlot(@NonNull String objectiveName, @NonNull DisplaySlot displaySlot) {
        Objective objective = objectives.get(objectiveName);
        if (objective == null) {
            error("Tried to set display slot for non-existing objective %s to player ", objectiveName);
            return;
        }
        objective.setDisplaySlot(displaySlot);
        if (frozen) return;
        setDisplaySlot(objective);
    }

    @Override
    public synchronized void unregisterObjective(@NonNull String objectiveName) {
        Objective objective = objectives.remove(objectiveName);
        if (objective == null) {
            error("Tried to unregister non-existing objective %s for player ", objectiveName);
            return;
        }
        if (frozen) return;
        unregisterObjective(objective);
    }

    @Override
    public synchronized void updateObjective(@NonNull String objectiveName, @NonNull TabComponent title,
                                      @NonNull HealthDisplay display, @Nullable TabComponent numberFormat) {
        Objective objective = objectives.get(objectiveName);
        if (objective == null) {
            error("Tried to modify non-existing objective %s for player ", objectiveName);
            return;
        }
        objective.update(title, display, numberFormat);
        if (frozen) return;
        updateObjective(objective);
    }

    @Override
    public synchronized void setScore(@NonNull String objectiveName, @NonNull String scoreHolder, int value,
                               @Nullable TabComponent displayName, @Nullable TabComponent numberFormat) {
        Objective objective = objectives.get(objectiveName);
        if (objective == null) {
            error("Tried to update score (%s) without the existence of its requested objective '%s' to player ", scoreHolder, objectiveName);
            return;
        }
        Score score = objective.getScores().get(scoreHolder);
        if (score == null) {
            score = new Score(objective, scoreHolder, value, displayName, numberFormat);
            objective.getScores().put(scoreHolder, score);
        } else {
            score.update(value, displayName, numberFormat);
        }
        if (frozen) return;
        setScore(score);
    }

    @Override
    public synchronized void removeScore(@NonNull String objectiveName, @NonNull String scoreHolder) {
        Objective objective = objectives.get(objectiveName);
        if (objective == null) {
            error("Tried to remove score (%s) without the existence of its requested objective '%s' to player ", scoreHolder, objectiveName);
            return;
        }
        Score score = objective.getScores().remove(scoreHolder);
        if (score == null) return;
        if (frozen) return;
        removeScore(score);
    }

    @Override
    public synchronized void registerTeam(@NonNull String name, @NonNull TabComponent prefix, @NonNull TabComponent suffix,
                                   @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                                   @NonNull Collection<String> players, int options, @NonNull TextColor color) {
        Team existing = teams.get(name);
        if (existing != null) {
            error("Tried to register duplicated team %s with entry %s, while this team already exists with entry %s to player ",
                    name, players.toString(), existing.players.toString());
            return;
        }
        Team team = new Team(createTeam(name), name, prefix, suffix, visibility, collision, players, options, color);
        teams.put(name, team);
        if (frozen) return;
        registerTeam(team);
    }

    @Override
    public synchronized void unregisterTeam(@NonNull String teamName) {
        Team team = teams.remove(teamName);
        if (team == null) {
            error("Tried to unregister non-existing team %s for player ", teamName);
            return;
        }
        if (frozen) return;
        unregisterTeam(team);
    }

    @Override
    public synchronized void updateTeam(@NonNull String name, @NonNull TabComponent prefix, @NonNull TabComponent suffix,
                                 @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                                 int options, @NonNull TextColor color) {
        Team team = teams.get(name);
        if (team == null) {
            error("Tried to modify non-existing team %s for player ", name);
            return;
        }
        team.update(prefix, suffix, visibility, collision, options, color);
        if (frozen) return;
        updateTeam(team);
    }

    @Override
    public synchronized void updateTeam(@NonNull String name, @NonNull TabComponent prefix, @NonNull TabComponent suffix, @NonNull TextColor color) {
        Team team = teams.get(name);
        if (team == null) return;
        team.update(prefix, suffix, color);
        if (frozen) return;
        updateTeam(team);
    }

    @Override
    public synchronized void updateTeam(@NonNull String name, @NonNull CollisionRule collision) {
        Team team = teams.get(name);
        if (team == null) return;
        team.collision = collision;
        if (frozen) return;
        updateTeam(team);
    }

    @Override
    public synchronized void updateTeam(@NonNull String name, @NonNull NameVisibility visibility) {
        Team team = teams.get(name);
        if (team == null) return;
        team.visibility = visibility;
        if (frozen) return;
        updateTeam(team);
    }

    @Override
    public synchronized void renameTeam(@NonNull String oldName, @NonNull String newName) {
        Team team = teams.get(oldName);
        if (team == null) return;
        unregisterTeam(oldName);
        registerTeam(newName, team.prefix, team.suffix, team.visibility, team.collision, team.players, team.options, team.color);
    }

    @Override
    public synchronized void resend() {
        for (Objective objective : objectives.values()) {
            registerObjective(objective);
            if (objective.getDisplaySlot() != null) {
                setDisplaySlot(objective);
            }
            for (Score score : objective.getScores().values()) {
                setScore(score);
            }
        }
        for (Team team : teams.values()) {
            registerTeam(team);
        }
    }

    @Override
    public synchronized void clear() {
        for (String objective : objectives.keySet()) {
            unregisterObjective(objective);
        }
        for (String team : teams.keySet()) {
            unregisterTeam(team);
        }
    }

    /**
     * Safely unregisters a team if it exists. If not, nothing happens.
     *
     * @param   teamName
     *          Name of team to unregister
     */
    public synchronized void unregisterTeamSafe(@NonNull String teamName) {
        Team team = teams.remove(teamName);
        if (team == null || frozen) return;
        unregisterTeam(team);
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
        TAB.getInstance().getErrorManager().printError(String.format(format, args) + player.getName(), null);
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
    public static String cutTo(@Nullable String string, int length) {
        if (string == null) return "";
        if (string.length() <= length) return string;
        if (string.charAt(length-1) == '§') {
            return string.substring(0, length-1); //cutting one extra character to prevent prefix ending with "&"
        } else {
            return string.substring(0, length);
        }
    }

    /**
     * Processes packet send.
     *
     * @param   packet
     *          Packet sent by the server
     */
    public void onPacketSend(@NonNull Object packet) {
        // Implemented by platforms with pipeline injection
    }

    /**
     * Checks if team contains a player who should belong to a different team and if override attempt was detected,
     * sends a warning and removes player from the collection.
     *
     * @param   action
     *          Team packet action
     * @param   teamName
     *          Team name in the packet
     * @param   players
     *          Players in the packet
     * @return  Modified collection of players
     */
    @NotNull
    public Collection<String> onTeamPacket(int action, @NonNull String teamName, @NonNull Collection<String> players) {
        Collection<String> newList = new ArrayList<>();
        if (action == TeamAction.CREATE || action == TeamAction.ADD_PLAYER) {
            for (String entry : players) {
                Team expectedTeam = getExpectedTeam(entry);
                if (expectedTeam == null) {
                    blockedTeamAdds.remove(entry);
                    allowedTeamAdds.put(entry, teamName);
                    newList.add(entry);
                    continue;
                }
                if (teamName.equals(expectedTeam.getName())) {
                    newList.add(entry);
                    allowedTeamAdds.remove(entry);
                } else {
                    blockedTeamAdds.put(entry, teamName);
                    logTeamOverride(teamName, entry, expectedTeam);
                }
            }
        }
        if (action == TeamAction.REMOVE_PLAYER) {
            // TAB does not send remove player, making checks easier
            for (String entry : players) {
                Team expectedTeam = getExpectedTeam(entry);
                if (expectedTeam != null) {
                    allowedTeamAdds.remove(entry);
                    blockedTeamAdds.remove(entry);
                    continue;
                }
                if (allowedTeamAdds.containsKey(entry)) {
                    allowedTeamAdds.remove(entry);
                    newList.add(entry);
                    continue;
                }
                blockedTeamAdds.remove(entry);
            }
        }
        if (action == TeamAction.REMOVE) {
            allowedTeamAdds.entrySet().removeIf(entry -> entry.getValue().equals(teamName));
            blockedTeamAdds.entrySet().removeIf(entry -> entry.getValue().equals(teamName));
        }
        return newList;
    }

    @Nullable
    private Team getExpectedTeam(@NotNull String player) {
        for (Team team : teams.values()) {
            if (team.getPlayers().contains(player)) return team;
        }
        return null;
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
     *          Expected team
     */
    public static void logTeamOverride(@NonNull String team, @NonNull String player, @NonNull Team expectedTeam) {
        String message = "Blocked attempt to add player " + player + " into team " + team + " (expected team: " + expectedTeam.getName() + ")";
        //not logging the same message for every online player who received the packet
        if (!message.equals(lastTeamOverrideMessage)) {
            lastTeamOverrideMessage = message;
            TAB.getInstance().getErrorManager().printError(message, Collections.emptyList(), false, TAB.getInstance().getErrorManager().getAntiOverrideLog());
        }
    }

    /**
     * Registers an objective
     *
     * @param   objective
     *          Objective to register
     */
    public abstract void registerObjective(@NonNull Objective objective);

    /**
     * Sets display slot for an objective
     *
     * @param   objective
     *          Objective to set display slot for
     */
    public abstract void setDisplaySlot(@NonNull Objective objective);

    /**
     * Unregisters an objective
     *
     * @param   objective
     *          Objective to unregister
     */
    public abstract void unregisterObjective(@NonNull Objective objective);

    /**
     * Updates an objective
     *
     * @param   objective
     *          Objective to update
     */
    public abstract void updateObjective(@NonNull Objective objective);

    /**
     * Sets score value
     *
     * @param   score
     *          Score to set
     */
    public abstract void setScore(@NonNull Score score);

    /**
     * Removes score value
     *
     * @param   score
     *          Score to remove
     */
    public abstract void removeScore(@NonNull Score score);

    /**
     * Constructs platform's team object with given name
     *
     * @param   name
     *          Team name
     * @return  Platform's team object with given name
     */
    @NotNull
    public abstract Object createTeam(@NonNull String name);

    /**
     * Registers a team
     *
     * @param   team
     *          Team to register
     */
    public abstract void registerTeam(@NonNull Team team);

    /**
     * Unregisters a team
     *
     * @param   team
     *          Team to unregister
     */
    public abstract void unregisterTeam(@NonNull Team team);

    /**
     * Updates a team
     *
     * @param   team
     *          Team to update
     */
    public abstract void updateTeam(@NonNull Team team);

    /**
     * Structure holding objective data.
     */
    @Getter
    @Setter
    public static class Objective {

        @Nullable private DisplaySlot displaySlot;
        @NonNull private final String name;
        @NonNull private TabComponent title;
        @NonNull private HealthDisplay healthDisplay;
        @Nullable private TabComponent numberFormat;
        @NonNull private final Map<String, Score> scores = new HashMap<>();

        /** Platform's objective object (if it has one) for fast access */
        @Nullable private Object platformObjective;

        private Objective(@NonNull String name, @NonNull TabComponent title,
                         @NonNull HealthDisplay healthDisplay, @Nullable TabComponent numberFormat, @Nullable Object platformObjective) {
            this.name = name;
            this.title = title;
            this.healthDisplay = healthDisplay;
            this.numberFormat = numberFormat;
            this.platformObjective = platformObjective;
        }

        private void update(@NonNull TabComponent title, @NonNull HealthDisplay healthDisplay, @Nullable TabComponent numberFormat) {
            this.title = title;
            this.healthDisplay = healthDisplay;
            this.numberFormat = numberFormat;
        }
    }

    /**
     * Structure holding score data.
     */
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Score {

        @NonNull private final Objective objective;
        @NonNull private final String holder;
        private int value;
        @Nullable private TabComponent displayName;
        @Nullable private TabComponent numberFormat;

        private void update(int value, @Nullable TabComponent displayName, @Nullable TabComponent numberFormat) {
            this.value = value;
            this.displayName = displayName;
            this.numberFormat = numberFormat;
        }
    }

    /**
     * Structure holding team data.
     */
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Team {

        /** Platform's team object for fast access */
        @NotNull private Object platformTeam;
        @NonNull private final String name;
        @NonNull private TabComponent prefix;
        @NonNull private TabComponent suffix;
        @NonNull private NameVisibility visibility;
        @NonNull private CollisionRule collision;
        @NonNull private Collection<String> players;
        private int options;
        @NonNull private TextColor color;

        private void update(@NonNull TabComponent prefix, @NonNull TabComponent suffix, @NonNull NameVisibility visibility,
                            @NonNull CollisionRule collision, int options, @NonNull TextColor color) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.visibility = visibility;
            this.collision = collision;
            this.options = options;
            this.color = color;
        }

        private void update(@NonNull TabComponent prefix, @NonNull TabComponent suffix, @NonNull TextColor color) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.color = color;
        }
    }
}
