package me.neznamy.tab.shared;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.Scoreboard;
import me.neznamy.tab.api.TabPlayer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public abstract class TabScoreboard implements Scoreboard {

    /** Player this scoreboard belongs to */
    protected final TabPlayer player;

    /** Scoreboard teams player has registered */
    private final Set<String> registeredTeams = new HashSet<>();

    /** Scoreboard objectives player has registered */
    private final Set<String> registeredObjectives = new HashSet<>();

    @Override
    public void setScore(@NonNull String objective, @NonNull String player, int score) {
        if (!registeredObjectives.contains(objective)) {
            TAB.getInstance().getErrorManager().printError("Tried to update score (" + player +
                    ") without the existence of its requested objective '" + objective + "' to player " + this.player.getName());
            return;
        }
        setScore0(objective, player, score);
    }

    @Override
    public void removeScore(@NonNull String objective, @NonNull String player) {
        if (!registeredObjectives.contains(objective)) {
            TAB.getInstance().getErrorManager().printError("Tried to update score (" + player +
                    ") without the existence of its requested objective '" + objective + "' to player " + this.player.getName());
            return;
        }
        removeScore0(objective, player);
    }

    @Override
    public void registerObjective(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        if (!registeredObjectives.add(objectiveName)) {
            TAB.getInstance().getErrorManager().printError("Tried to register duplicated objective " + objectiveName + " to player " + this.player.getName());
            return;
        }
        registerObjective0(objectiveName, title, hearts);
    }

    @Override
    public void unregisterObjective(@NonNull String objectiveName) {
        if (!registeredObjectives.remove(objectiveName)) {
            TAB.getInstance().getErrorManager().printError("Tried to unregister non-existing objective " + objectiveName + " for player " + this.player.getName());
            return;
        }
        unregisterObjective0(objectiveName);
    }

    @Override
    public void updateObjective(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        if (!registeredObjectives.contains(objectiveName)) {
            TAB.getInstance().getErrorManager().printError("Tried to modify non-existing objective " + objectiveName + " for player " + this.player.getName());
            return;
        }
        updateObjective0(objectiveName, title, hearts);
    }

    @Override
    public void registerTeam(@NonNull String name, String prefix, String suffix, String visibility, String collision, Collection<String> players, int options) {
        if (!registeredTeams.add(name)) {
            TAB.getInstance().getErrorManager().printError("Tried to register duplicated team " + name + " to player " + this.player.getName());
            return;
        }
        registerTeam0(name, prefix, suffix, visibility, collision, players, options);
    }

    @Override
    public void unregisterTeam(@NonNull String name) {
        if (!registeredTeams.remove(name)) {
            TAB.getInstance().getErrorManager().printError("Tried to unregister non-existing team " + name + " for player " + this.player.getName());
            return;
        }
        unregisterTeam0(name);
    }

    @Override
    public void updateTeam(@NonNull String name, String prefix, String suffix, String visibility, String collision, int options) {
        if (!registeredTeams.contains(name)) {
            TAB.getInstance().getErrorManager().printError("Tried to modify non-existing team " + name + " for player " + this.player.getName());
            return;
        }
        updateTeam0(name, prefix, suffix, visibility, collision, options);
    }

    @Override
    public void clearRegisteredObjectives() {
        registeredTeams.clear();
        registeredObjectives.clear();
    }

    public abstract void setScore0(@NonNull String objective, @NonNull String player, int score);

    public abstract void removeScore0(@NonNull String objective, @NonNull String player);

    public abstract void registerObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts);

    public abstract void unregisterObjective0(@NonNull String objectiveName);

    public abstract void updateObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts);

    public abstract void registerTeam0(@NonNull String name, String prefix, String suffix, String visibility,
                                                 String collision, Collection<String> players, int options);

    public abstract void unregisterTeam0(@NonNull String name);

    public abstract void updateTeam0(@NonNull String name, String prefix, String suffix, String visibility, String collision, int options);

}
