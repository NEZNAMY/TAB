package me.neznamy.tab.platforms.sponge8;

import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.scoreboard.*;
import org.spongepowered.api.scoreboard.criteria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;

import java.util.Collection;

public class SpongeScoreboard extends Scoreboard<SpongeTabPlayer> {

    public SpongeScoreboard(SpongeTabPlayer player) {
        super(player);
        // Make sure each player is in different scoreboard for per-player view
        player.getPlayer().setScoreboard(org.spongepowered.api.scoreboard.Scoreboard.builder().build());
    }

    @Override
    public void setDisplaySlot(@NotNull DisplaySlot slot, @NotNull String objective) {
        player.getPlayer().scoreboard().objective(objective).ifPresent(
                o -> player.getPlayer().scoreboard().updateDisplaySlot(o, convertDisplaySlot(slot)));
    }

    private org.spongepowered.api.scoreboard.displayslot.DisplaySlot convertDisplaySlot(DisplaySlot slot) {
        switch (slot) {
            case PLAYER_LIST: return DisplaySlots.LIST.get();
            case SIDEBAR: return DisplaySlots.SIDEBAR.get();
            default: return DisplaySlots.BELOW_NAME.get();
        }
    }

    @Override
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, boolean hearts) {
        Objective objective = Objective.builder()
                .name(objectiveName)
                .displayName(IChatBaseComponent.optimizedComponent(title).toAdventureComponent())
                .objectiveDisplayMode(hearts ? ObjectiveDisplayModes.HEARTS : ObjectiveDisplayModes.INTEGER)
                .criterion(Criteria.DUMMY)
                .build();
        player.getPlayer().scoreboard().addObjective(objective);
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        player.getPlayer().scoreboard().objective(objectiveName).ifPresent(o ->
                player.getPlayer().scoreboard().removeObjective(o));
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, boolean hearts) {
        Objective obj = player.getPlayer().scoreboard().objective(objectiveName).orElseThrow(IllegalStateException::new);
        obj.setDisplayName(IChatBaseComponent.optimizedComponent(title).toAdventureComponent());
        obj.setDisplayMode(hearts ? ObjectiveDisplayModes.HEARTS.get() : ObjectiveDisplayModes.INTEGER.get());
    }

    @Override
    public void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix, @NotNull NameVisibility visibility, @NotNull CollisionRule collision, @NotNull Collection<String> players, int options) {
        Team team = Team.builder()
                .name(name)
                .displayName(IChatBaseComponent.optimizedComponent(name).toAdventureComponent())
                .prefix(IChatBaseComponent.optimizedComponent(prefix).toAdventureComponent())
                .suffix(IChatBaseComponent.optimizedComponent(suffix).toAdventureComponent())
                .allowFriendlyFire((options & 0x01) != 0)
                .canSeeFriendlyInvisibles((options & 0x02) != 0)
                .collisionRule(convertCollisionRule(collision))
                .nameTagVisibility(convertVisibility(visibility))
                .build();
        for (String member : players) {
            team.addMember(IChatBaseComponent.optimizedComponent(member).toAdventureComponent());
        }
        player.getPlayer().scoreboard().registerTeam(team);
    }

    @Override
    public void unregisterTeam0(@NotNull String name) {
        player.getPlayer().scoreboard().team(name).ifPresent(Team::unregister);
    }

    @Override
    public void updateTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix, @NotNull NameVisibility visibility, @NotNull CollisionRule collision, int options) {
        Team team = player.getPlayer().scoreboard().team(name).orElse(null);
        if (team == null) return;
        team.setDisplayName(IChatBaseComponent.optimizedComponent(name).toAdventureComponent());
        team.setPrefix(IChatBaseComponent.optimizedComponent(prefix).toAdventureComponent());
        team.setSuffix(IChatBaseComponent.optimizedComponent(suffix).toAdventureComponent());
        team.setAllowFriendlyFire((options & 0x01) != 0);
        team.setCanSeeFriendlyInvisibles((options & 0x02) != 0);
        team.setCollisionRule(convertCollisionRule(collision));
        team.setNameTagVisibility(convertVisibility(visibility));
    }

    private org.spongepowered.api.scoreboard.CollisionRule convertCollisionRule(CollisionRule rule) {
        switch (rule) {
            case ALWAYS: return CollisionRules.ALWAYS.get();
            case NEVER: return CollisionRules.NEVER.get();
            case PUSH_OTHER_TEAMS: return CollisionRules.PUSH_OTHER_TEAMS.get();
            case PUSH_OWN_TEAM: return CollisionRules.PUSH_OWN_TEAM.get();
            default: throw new IllegalArgumentException();
        }
    }

    private Visibility convertVisibility(NameVisibility visibility) {
        switch (visibility) {
            case ALWAYS: return Visibilities.ALWAYS.get();
            case NEVER: return Visibilities.NEVER.get();
            case HIDE_FOR_OTHER_TEAMS: return Visibilities.HIDE_FOR_OTHER_TEAMS.get();
            case HIDE_FOR_OWN_TEAM: return Visibilities.HIDE_FOR_OWN_TEAM.get();
            default: throw new IllegalArgumentException();
        }
    }

    @Override
    public void setScore0(@NotNull String objective, @NotNull String playerName, int score) {
        player.getPlayer().scoreboard().objective(objective).ifPresent(o -> o.findOrCreateScore(
                IChatBaseComponent.optimizedComponent(playerName).toAdventureComponent()).setScore(score));
    }

    @Override
    public void removeScore0(@NotNull String objective, @NotNull String playerName) {
        player.getPlayer().scoreboard().objective(objective).ifPresent(o -> o.removeScore(
                IChatBaseComponent.optimizedComponent(playerName).toAdventureComponent()));
    }
}
