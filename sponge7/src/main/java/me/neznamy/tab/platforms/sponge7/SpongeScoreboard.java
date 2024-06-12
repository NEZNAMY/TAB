package me.neznamy.tab.platforms.sponge7;

import lombok.NonNull;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import org.spongepowered.api.scoreboard.CollisionRules;
import org.spongepowered.api.scoreboard.Visibilities;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;

/**
 * Scoreboard implementation for Sponge 7 using its API.
 */
public class SpongeScoreboard extends SafeScoreboard<SpongeTabPlayer> {

    /** Collision rule array for fast access */
    private static final org.spongepowered.api.scoreboard.CollisionRule[] collisionRules = {
            CollisionRules.ALWAYS,
            CollisionRules.NEVER,
            CollisionRules.PUSH_OTHER_TEAMS,
            CollisionRules.PUSH_OWN_TEAM
    };

    /** Visibility array for fast access */
    private static final Visibility[] visibilities = {
            Visibilities.ALWAYS,
            Visibilities.NEVER,
            Visibilities.HIDE_FOR_OTHER_TEAMS,
            Visibilities.HIDE_FOR_OWN_TEAM
    };

    /** DisplaySlot array for fast access */
    private static final org.spongepowered.api.scoreboard.displayslot.DisplaySlot[] displaySlots = {
            DisplaySlots.LIST,
            DisplaySlots.SIDEBAR,
            DisplaySlots.BELOW_NAME
    };

    /** Health display array for fast access */
    private static final ObjectiveDisplayMode[] healthDisplays = {
            ObjectiveDisplayModes.INTEGER,
            ObjectiveDisplayModes.HEARTS
    };

    /** Scoreboard of the player */
    private final org.spongepowered.api.scoreboard.Scoreboard sb = org.spongepowered.api.scoreboard.Scoreboard.builder().build();

    /**
     * Constructs new instance and puts player into new scoreboard.
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public SpongeScoreboard(@NonNull SpongeTabPlayer player) {
        super(player);

        // Make sure each player is in a different scoreboard for per-player view
        player.getPlayer().setScoreboard(sb);
    }

    @Override
    public void registerObjective(@NonNull Objective objective) {
        org.spongepowered.api.scoreboard.objective.Objective obj = org.spongepowered.api.scoreboard.objective.Objective.builder()
                .name(objective.getName())
                .displayName(Text.of(cutTo(objective.getTitle().toLegacyText(), Limitations.SCOREBOARD_TITLE_PRE_1_13)))
                .objectiveDisplayMode(healthDisplays[objective.getHealthDisplay().ordinal()])
                .criterion(Criteria.DUMMY)
                .build();
        sb.addObjective(obj);
        sb.updateDisplaySlot(obj, displaySlots[objective.getDisplaySlot().ordinal()]);
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        sb.getObjective(objective.getName()).ifPresent(sb::removeObjective);
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        sb.getObjective(objective.getName()).ifPresent(obj -> {
            obj.setDisplayName(objective.getTitle().convert(player.getVersion()));
            obj.setDisplayMode(healthDisplays[objective.getHealthDisplay().ordinal()]);
        });
    }

    @Override
    public void setScore(@NonNull Score score) {
        sb.getObjective(score.getObjective()).ifPresent(o -> o.getOrCreateScore(Text.of(score.getHolder())).setScore(score.getValue()));
    }

    @Override
    public void removeScore(@NonNull Score score) {
        sb.getObjective(score.getObjective()).ifPresent(o -> o.removeScore(Text.of(score.getHolder())));
    }

    @Override
    public void registerTeam(@NonNull Team team) {
        org.spongepowered.api.scoreboard.Team spongeTeam = org.spongepowered.api.scoreboard.Team.builder()
                .name(team.getName())
                .displayName(Text.of(team.getName()))
                .prefix(Text.of(cutTo(team.getPrefix().toLegacyText(), Limitations.SCOREBOARD_TITLE_PRE_1_13)))
                .suffix(Text.of(cutTo(team.getSuffix().toLegacyText(), Limitations.SCOREBOARD_TITLE_PRE_1_13)))
                .allowFriendlyFire((team.getOptions() & 0x01) != 0)
                .canSeeFriendlyInvisibles((team.getOptions() & 0x02) != 0)
                .collisionRule(collisionRules[team.getCollision().ordinal()])
                .nameTagVisibility(visibilities[team.getVisibility().ordinal()])
                .build();
        for (String member : team.getPlayers()) {
            spongeTeam.addMember(Text.of(member));
        }
        sb.registerTeam(spongeTeam);
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        sb.getTeam(team.getName()).ifPresent(org.spongepowered.api.scoreboard.Team::unregister);
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        sb.getTeam(team.getName()).ifPresent(spongeTeam -> {
            spongeTeam.setDisplayName(Text.of(team.getName()));
            spongeTeam.setPrefix(Text.of(cutTo(team.getPrefix().toLegacyText(), Limitations.SCOREBOARD_TITLE_PRE_1_13)));
            spongeTeam.setSuffix(Text.of(cutTo(team.getSuffix().toLegacyText(), Limitations.SCOREBOARD_TITLE_PRE_1_13)));
            spongeTeam.setAllowFriendlyFire((team.getOptions() & 0x01) != 0);
            spongeTeam.setCanSeeFriendlyInvisibles((team.getOptions() & 0x02) != 0);
            spongeTeam.setCollisionRule(collisionRules[team.getCollision().ordinal()]);
            spongeTeam.setNameTagVisibility(visibilities[team.getVisibility().ordinal()]);
        });
    }
}
