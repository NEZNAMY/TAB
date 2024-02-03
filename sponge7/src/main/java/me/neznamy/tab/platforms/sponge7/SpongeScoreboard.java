package me.neznamy.tab.platforms.sponge7;

import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.scoreboard.CollisionRules;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.Visibilities;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;

import java.util.Collection;

/**
 * Scoreboard implementation for Sponge 7 using its API.
 */
public class SpongeScoreboard extends Scoreboard<SpongeTabPlayer> {

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
    @NotNull
    private final org.spongepowered.api.scoreboard.Scoreboard sb = org.spongepowered.api.scoreboard.Scoreboard.builder().build();

    /**
     * Constructs new instance and puts player into new scoreboard.
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public SpongeScoreboard(@NotNull SpongeTabPlayer player) {
        super(player);

        // Make sure each player is in a different scoreboard for per-player view
        player.getPlayer().setScoreboard(sb);
    }

    @Override
    public void setDisplaySlot0(int slot, @NotNull String objective) {
        sb.getObjective(objective).ifPresent(o -> sb.updateDisplaySlot(o, displaySlots[slot]));
    }

    @Override
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, int display,
                                   @Nullable TabComponent numberFormat) {
        sb.addObjective(Objective.builder()
                .name(objectiveName)
                .displayName(Text.of(title))
                .objectiveDisplayMode(healthDisplays[display])
                .criterion(Criteria.DUMMY)
                .build()
        );
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        sb.getObjective(objectiveName).ifPresent(sb::removeObjective);
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, int display,
                                 @Nullable TabComponent numberFormat) {
        sb.getObjective(objectiveName).ifPresent(obj -> {
            obj.setDisplayName(Text.of(title));
            obj.setDisplayMode(healthDisplays[display]);
        });
    }

    @Override
    public void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                              @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                              @NotNull Collection<String> players, int options, @NotNull EnumChatFormat color) {
        Team team = Team.builder()
                .name(name)
                .displayName(Text.of(name))
                .prefix(Text.of(prefix))
                .suffix(Text.of(suffix))
                .allowFriendlyFire((options & 0x01) != 0)
                .canSeeFriendlyInvisibles((options & 0x02) != 0)
                .collisionRule(collisionRules[collision.ordinal()])
                .nameTagVisibility(visibilities[visibility.ordinal()])
                .build();
        for (String member : players) {
            team.addMember(Text.of(member));
        }
        sb.registerTeam(team);
    }

    @Override
    public void unregisterTeam0(@NotNull String name) {
        sb.getTeam(name).ifPresent(Team::unregister);
    }

    @Override
    public void updateTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                            @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                            int options, @NotNull EnumChatFormat color) {
        sb.getTeam(name).ifPresent(team -> {
            team.setDisplayName(Text.of(name));
            team.setPrefix(Text.of(prefix));
            team.setSuffix(Text.of(suffix));
            team.setAllowFriendlyFire((options & 0x01) != 0);
            team.setCanSeeFriendlyInvisibles((options & 0x02) != 0);
            team.setCollisionRule(collisionRules[collision.ordinal()]);
            team.setNameTagVisibility(visibilities[visibility.ordinal()]);
        });
    }

    @Override
    public void setScore0(@NotNull String objective, @NotNull String scoreHolder, int score,
                          @Nullable TabComponent displayName, @Nullable TabComponent numberFormat) {
        sb.getObjective(objective).ifPresent(o -> o.getOrCreateScore(Text.of(scoreHolder)).setScore(score));
    }

    @Override
    public void removeScore0(@NotNull String objective, @NotNull String scoreHolder) {
        sb.getObjective(objective).ifPresent(o -> o.removeScore(Text.of(scoreHolder)));
    }
}
