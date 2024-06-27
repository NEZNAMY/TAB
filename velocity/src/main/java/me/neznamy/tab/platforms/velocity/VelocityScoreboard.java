package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.TextHolder;
import com.velocitypowered.api.scoreboard.*;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import org.jetbrains.annotations.NotNull;

/**
 * Scoreboard implementation using VelocityScoreboardAPI plugin.
 */
public class VelocityScoreboard extends SafeScoreboard<VelocityTabPlayer> {

    private static final TeamColor[] colors = TeamColor.values();
    private static final com.velocitypowered.api.scoreboard.NameVisibility[] visibilities = com.velocitypowered.api.scoreboard.NameVisibility.values();
    private static final com.velocitypowered.api.scoreboard.CollisionRule[] collisions = com.velocitypowered.api.scoreboard.CollisionRule.values();
    private final ProxyScoreboard scoreboard;

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player to send scoreboard to
     */
    public VelocityScoreboard(@NotNull VelocityTabPlayer player) {
        super(player);
        scoreboard = ScoreboardManager.getInstance().getProxyScoreboard(player.getPlayer());
    }

    @Override
    public void registerObjective(@NonNull Objective objective) {
        try {
            ProxyObjective.Builder builder = scoreboard.objectiveBuilder(objective.getName())
                    .displaySlot(com.velocitypowered.api.scoreboard.DisplaySlot.valueOf(objective.getDisplaySlot().name()))
                    .healthDisplay(com.velocitypowered.api.scoreboard.HealthDisplay.valueOf(objective.getHealthDisplay().name()))
                    .title(TextHolder.of(objective.getTitle().toLegacyText(), objective.getTitle().toAdventure(player.getVersion())))
                    .numberFormat(objective.getNumberFormat() == null ? null : NumberFormat.fixed(objective.getNumberFormat().toAdventure(player.getVersion())));
            scoreboard.registerObjective(builder);
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().printError("Failed to register objective " + objective.getName() + " for player " + player.getName(), e);
        }
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        try {
            scoreboard.unregisterObjective(objective.getName());
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().printError("Failed to unregister objective " + objective.getName() + " for player " + player.getName(), e);
        }
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        try {
            ProxyObjective obj = scoreboard.getObjective(objective.getName());
            obj.setHealthDisplay(com.velocitypowered.api.scoreboard.HealthDisplay.valueOf(objective.getHealthDisplay().name()));
            obj.setTitle(TextHolder.of(objective.getTitle().toLegacyText(), objective.getTitle().toAdventure(player.getVersion())));
            obj.setNumberFormat(objective.getNumberFormat() == null ? null : NumberFormat.fixed(objective.getNumberFormat().toAdventure(player.getVersion())));
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().printError("Failed to update objective " + objective.getName() + " for player " + player.getName(), e);
        }
    }

    @Override
    public void setScore(@NonNull Score score) {
        try {
            scoreboard.getObjective(score.getObjective()).setScore(score.getHolder(), b -> b
                    .score(score.getValue())
                    .displayName(score.getDisplayName() == null ? null : score.getDisplayName().toAdventure(player.getVersion()))
                    .numberFormat(score.getNumberFormat() == null ? null : NumberFormat.fixed(score.getNumberFormat().toAdventure(player.getVersion())))
            );
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().printError("Failed to set score " + score.getHolder() + " for player " + player.getName(), e);
        }
    }

    @Override
    public void removeScore(@NonNull Score score) {
        try {
            scoreboard.getObjective(score.getObjective()).removeScore(score.getHolder());
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().printError("Failed to remove score " + score.getHolder() + " for player " + player.getName(), e);
        }
    }

    @Override
    @NotNull
    public Object createTeam(@NonNull String name) {
        return new Object(); // This API does not work that way
    }

    @Override
    public void registerTeam(@NonNull Team team) {
        ProxyTeam previous = scoreboard.getTeam(team.getName());
        if (previous != null) {
            TAB.getInstance().getErrorManager().printError("Team " + previous.getName() + " already existed when registering for player " + player.getName() + ", unregistering", null);
            scoreboard.unregisterTeam(previous.getName());
        }
        team.setPlatformTeam(scoreboard.registerTeam(scoreboard.teamBuilder(team.getName())
                .prefix(TextHolder.of(team.getPrefix().toLegacyText(), team.getPrefix().toAdventure(player.getVersion())))
                .suffix(TextHolder.of(team.getSuffix().toLegacyText(), team.getSuffix().toAdventure(player.getVersion())))
                .nameVisibility(visibilities[team.getVisibility().ordinal()])
                .collisionRule(collisions[team.getCollision().ordinal()])
                .allowFriendlyFire((team.getOptions() & 0x01) > 0)
                .canSeeFriendlyInvisibles((team.getOptions() & 0x02) > 0)
                .color(colors[team.getColor().ordinal()])
                .entries(team.getPlayers())
        ));
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        if (scoreboard.getTeam(team.getName()) != null) {
            scoreboard.unregisterTeam(team.getName());
        } else {
            TAB.getInstance().getErrorManager().printError("Team " + team.getName() + " did not exist when unregistering for player " + player.getName(), null);
        }
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        ((ProxyTeam)team.getPlatformTeam()).updateProperties(b -> b
                .prefix(TextHolder.of(team.getPrefix().toLegacyText(), team.getPrefix().toAdventure(player.getVersion())))
                .suffix(TextHolder.of(team.getSuffix().toLegacyText(), team.getSuffix().toAdventure(player.getVersion())))
                .nameVisibility(visibilities[team.getVisibility().ordinal()])
                .collisionRule(collisions[team.getCollision().ordinal()])
                .color(colors[team.getColor().ordinal()])
                .allowFriendlyFire((team.getOptions() & 0x01) > 0)
                .canSeeFriendlyInvisibles((team.getOptions() & 0x02) > 0)
        );
    }
}
