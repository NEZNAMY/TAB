package me.neznamy.tab.platforms.sponge8;

import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.scoreboard.CollisionRules;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.Visibilities;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.scoreboard.criteria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;

import java.util.Collection;

public class SpongeScoreboard extends Scoreboard<SpongeTabPlayer> {

    /** Collision rule array for fast access */
    private static final org.spongepowered.api.scoreboard.CollisionRule[] collisionRules = new org.spongepowered.api.scoreboard.CollisionRule[]{
            CollisionRules.ALWAYS.get(), CollisionRules.NEVER.get(), CollisionRules.PUSH_OTHER_TEAMS.get(), CollisionRules.PUSH_OWN_TEAM.get()
    };

    /** Visibility array for fast access */
    private static final Visibility[] visibilities = new Visibility[] {
            Visibilities.ALWAYS.get(), Visibilities.NEVER.get(), Visibilities.HIDE_FOR_OTHER_TEAMS.get(), Visibilities.HIDE_FOR_OWN_TEAM.get()
    };

    /** DisplaySlot array for fast access */
    private static final org.spongepowered.api.scoreboard.displayslot.DisplaySlot[] displaySlots = new org.spongepowered.api.scoreboard.displayslot.DisplaySlot[] {
            DisplaySlots.LIST.get(), DisplaySlots.SIDEBAR.get(), DisplaySlots.BELOW_NAME.get()
    };

    /** Scoreboard of the player */
    @NotNull
    private final org.spongepowered.api.scoreboard.Scoreboard sb = org.spongepowered.api.scoreboard.Scoreboard.builder().build();
    
    public SpongeScoreboard(@NotNull SpongeTabPlayer player) {
        super(player);
        // Make sure each player is in different scoreboard for per-player view
        player.getPlayer().setScoreboard(sb);
    }

    @Override
    public void setDisplaySlot(@NotNull DisplaySlot slot, @NotNull String objective) {
        sb.objective(objective).ifPresent(o -> sb.updateDisplaySlot(o, displaySlots[slot.ordinal()]));
    }

    @Override
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, @NotNull HealthDisplay display) {
        sb.addObjective(Objective.builder()
                .name(objectiveName)
                .displayName(adventure(title))
                .objectiveDisplayMode(display == HealthDisplay.HEARTS ? ObjectiveDisplayModes.HEARTS : ObjectiveDisplayModes.INTEGER)
                .criterion(Criteria.DUMMY)
                .build());
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        sb.objective(objectiveName).ifPresent(sb::removeObjective);
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, @NotNull HealthDisplay display) {
        sb.objective(objectiveName).ifPresent(obj -> {
            obj.setDisplayName(adventure(title));
            obj.setDisplayMode(display == HealthDisplay.HEARTS ? ObjectiveDisplayModes.HEARTS.get() : ObjectiveDisplayModes.INTEGER.get());
        });
     }

    @Override
    public void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                              @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                              @NotNull Collection<String> players, int options) {
        Team team = Team.builder()
                .name(name)
                .displayName(adventure(name))
                .prefix(adventure(prefix))
                .suffix(adventure(suffix))
                .allowFriendlyFire((options & 0x01) != 0)
                .canSeeFriendlyInvisibles((options & 0x02) != 0)
                .collisionRule(collisionRules[collision.ordinal()])
                .nameTagVisibility(visibilities[visibility.ordinal()])
                .build();
        for (String member : players) {
            team.addMember(adventure(member));
        }
        sb.registerTeam(team);
    }

    @Override
    public void unregisterTeam0(@NotNull String name) {
        sb.team(name).ifPresent(Team::unregister);
    }

    @Override
    public void updateTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                            @NotNull NameVisibility visibility, @NotNull CollisionRule collision, int options) {
        sb.team(name).ifPresent(team -> {
            team.setDisplayName(adventure(name));
            team.setPrefix(adventure(prefix));
            team.setSuffix(adventure(suffix));
            team.setAllowFriendlyFire((options & 0x01) != 0);
            team.setCanSeeFriendlyInvisibles((options & 0x02) != 0);
            team.setCollisionRule(collisionRules[collision.ordinal()]);
            team.setNameTagVisibility(visibilities[visibility.ordinal()]);
        });
    }

    @Override
    public void setScore0(@NotNull String objective, @NotNull String playerName, int score) {
        sb.objective(objective).ifPresent(o -> o.findOrCreateScore(adventure(playerName)).setScore(score));
    }

    @Override
    public void removeScore0(@NotNull String objective, @NotNull String playerName) {
        sb.objective(objective).ifPresent(o -> o.removeScore(adventure(playerName)));
    }

    /**
     * Converts text to Adventure component.
     *
     * @param   text
     *          Text to convert
     * @return  Converted text
     */
    @NotNull
    private Component adventure(@NotNull String text) {
        return player.getPlatform().toComponent(IChatBaseComponent.optimizedComponent(text), player.getVersion());
    }
}
