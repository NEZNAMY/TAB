package me.neznamy.tab.shared;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.Scoreboard;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.rgb.RGBUtils;

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
        if (player.getVersion().getMinorVersion() < 13) title = cutTo(title, 32);
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
        if (player.getVersion().getMinorVersion() < 13) title = cutTo(title, 32);
        updateObjective0(objectiveName, title, hearts);
    }

    @Override
    public void registerTeam(@NonNull String name, String prefix, String suffix, String visibility, String collision, Collection<String> players, int options) {
        if (!registeredTeams.add(name)) {
            TAB.getInstance().getErrorManager().printError("Tried to register duplicated team " + name + " to player " + this.player.getName());
            return;
        }
        if (player.getVersion().getMinorVersion() < 13) prefix = cutTo(prefix, 16);
        if (player.getVersion().getMinorVersion() < 13) suffix = cutTo(suffix, 16);
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
        if (player.getVersion().getMinorVersion() < 13) prefix = cutTo(prefix, 16);
        if (player.getVersion().getMinorVersion() < 13) suffix = cutTo(suffix, 16);
        updateTeam0(name, prefix, suffix, visibility, collision, options);
    }

    /**
     * Clears maps of registered teams and objectives when Login packet is sent
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
    private String cutTo(String string, int length) {
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
