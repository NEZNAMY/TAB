package me.neznamy.tab.platforms.sponge8;

import lombok.NonNull;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.scoreboard.CollisionRules;
import org.spongepowered.api.scoreboard.Visibilities;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.scoreboard.criteria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;

/**
 * Scoreboard implementation for Sponge 8 using its API.
 */
public class SpongeScoreboard extends SafeScoreboard<SpongeTabPlayer> {

    /** Collision rule array for fast access */
    private static final org.spongepowered.api.scoreboard.CollisionRule[] collisionRules = {
            CollisionRules.ALWAYS.get(),
            CollisionRules.NEVER.get(),
            CollisionRules.PUSH_OTHER_TEAMS.get(),
            CollisionRules.PUSH_OWN_TEAM.get()
    };

    /** Visibility array for fast access */
    private static final Visibility[] visibilities = {
            Visibilities.ALWAYS.get(),
            Visibilities.NEVER.get(),
            Visibilities.HIDE_FOR_OTHER_TEAMS.get(),
            Visibilities.HIDE_FOR_OWN_TEAM.get()
    };

    /** DisplaySlot array for fast access */
    private static final org.spongepowered.api.scoreboard.displayslot.DisplaySlot[] displaySlots = {
            DisplaySlots.LIST.get(),
            DisplaySlots.SIDEBAR.get(),
            DisplaySlots.BELOW_NAME.get()
    };

    /** Health display array for fast access */
    private static final ObjectiveDisplayMode[] healthDisplays = {
            ObjectiveDisplayModes.INTEGER.get(),
            ObjectiveDisplayModes.HEARTS.get()
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
                .displayName(objective.getTitle().toAdventure(player.getVersion()))
                .objectiveDisplayMode(healthDisplays[objective.getHealthDisplay().ordinal()])
                .criterion(Criteria.DUMMY)
                .build();
        sb.addObjective(obj);
        sb.updateDisplaySlot(obj, displaySlots[objective.getDisplaySlot().ordinal()]);
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        sb.objective(objective.getName()).ifPresent(sb::removeObjective);
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        sb.objective(objective.getName()).ifPresent(obj -> {
            obj.setDisplayName(objective.getTitle().toAdventure(player.getVersion()));
            obj.setDisplayMode(healthDisplays[objective.getHealthDisplay().ordinal()]);
        });
    }

    @Override
    public void setScore(@NonNull Score score) {
        sb.objective(score.getObjective()).ifPresent(o -> SpongeMultiVersion.findOrCreateScore(o, score.getHolder()).setScore(score.getValue()));
    }

    @Override
    public void removeScore(@NonNull Score score) {
        sb.objective(score.getObjective()).ifPresent(o -> o.removeScore(SpongeMultiVersion.findOrCreateScore(o, score.getHolder())));
    }

    @Override
    public void registerTeam(@NonNull Team team) {
        org.spongepowered.api.scoreboard.Team spongeTeam = org.spongepowered.api.scoreboard.Team.builder()
                .name(team.getName())
                .displayName(Component.text(team.getName()))
                .prefix(team.getPrefix().toAdventure(player.getVersion()))
                .suffix(team.getSuffix().toAdventure(player.getVersion()))
                .color(NamedTextColor.NAMES.valueOr(team.getColor().name(), NamedTextColor.WHITE))
                .allowFriendlyFire((team.getOptions() & 0x01) != 0)
                .canSeeFriendlyInvisibles((team.getOptions() & 0x02) != 0)
                .collisionRule(collisionRules[team.getCollision().ordinal()])
                .nameTagVisibility(visibilities[team.getVisibility().ordinal()])
                .build();
        for (String member : team.getPlayers()) {
            spongeTeam.addMember(Component.text(member));
        }
        sb.registerTeam(spongeTeam);
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        sb.team(team.getName()).ifPresent(org.spongepowered.api.scoreboard.Team::unregister);
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        sb.team(team.getName()).ifPresent(spongeTeam -> {
            spongeTeam.setDisplayName(Component.text(team.getName()));
            spongeTeam.setPrefix(team.getPrefix().toAdventure(player.getVersion()));
            spongeTeam.setSuffix(team.getSuffix().toAdventure(player.getVersion()));
            spongeTeam.setColor(NamedTextColor.NAMES.valueOr(team.getColor().name(), NamedTextColor.WHITE));
            spongeTeam.setAllowFriendlyFire((team.getOptions() & 0x01) != 0);
            spongeTeam.setCanSeeFriendlyInvisibles((team.getOptions() & 0x02) != 0);
            spongeTeam.setCollisionRule(collisionRules[team.getCollision().ordinal()]);
            spongeTeam.setNameTagVisibility(visibilities[team.getVisibility().ordinal()]);
        });
    }
}
