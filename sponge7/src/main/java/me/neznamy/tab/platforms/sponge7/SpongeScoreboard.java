package me.neznamy.tab.platforms.sponge7;

import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.scoreboard.*;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;

import java.util.Collection;

public class SpongeScoreboard extends Scoreboard<SpongeTabPlayer> {

    public SpongeScoreboard(SpongeTabPlayer player) {
        super(player);
        // Make sure each player is in different scoreboard for per-player view
        player.getPlayer().setScoreboard(org.spongepowered.api.scoreboard.Scoreboard.builder().build());
    }

    @Override
    public void setDisplaySlot(@NotNull DisplaySlot slot, @NotNull String objective) {
        player.getPlayer().getScoreboard().getObjective(objective).ifPresent(
                o -> player.getPlayer().getScoreboard().updateDisplaySlot(o, convertDisplaySlot(slot)));
    }

    private org.spongepowered.api.scoreboard.displayslot.DisplaySlot convertDisplaySlot(DisplaySlot slot) {
        switch (slot) {
            case PLAYER_LIST: return DisplaySlots.LIST;
            case SIDEBAR: return DisplaySlots.SIDEBAR;
            default: return DisplaySlots.BELOW_NAME;
        }
    }

    @Override
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, boolean hearts) {
        Objective objective = Objective.builder()
                .name(objectiveName)
                .displayName(Text.of(title))
                .objectiveDisplayMode(hearts ? ObjectiveDisplayModes.HEARTS : ObjectiveDisplayModes.INTEGER)
                .criterion(Criteria.DUMMY)
                .build();
        player.getPlayer().getScoreboard().addObjective(objective);
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        player.getPlayer().getScoreboard().getObjective(objectiveName).ifPresent(o ->
                player.getPlayer().getScoreboard().removeObjective(o));
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, boolean hearts) {
        Objective obj = player.getPlayer().getScoreboard().getObjective(objectiveName).orElseThrow(IllegalStateException::new);
        obj.setDisplayName(Text.of(title));
        obj.setDisplayMode(hearts ? ObjectiveDisplayModes.HEARTS : ObjectiveDisplayModes.INTEGER);
    }

    @Override
    public void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix, @NotNull NameVisibility visibility, @NotNull CollisionRule collision, @NotNull Collection<String> players, int options) {
        Team team = Team.builder()
                .name(name)
                .displayName(Text.of(name))
                .prefix(Text.of(prefix))
                .suffix(Text.of(suffix))
                .allowFriendlyFire((options & 0x01) != 0)
                .canSeeFriendlyInvisibles((options & 0x02) != 0)
                .collisionRule(convertCollisionRule(collision))
                .nameTagVisibility(convertVisibility(visibility))
                .build();
        for (String member : players) {
            team.addMember(Text.of(member));
        }
        player.getPlayer().getScoreboard().registerTeam(team);
    }

    @Override
    public void unregisterTeam0(@NotNull String name) {
        player.getPlayer().getScoreboard().getTeam(name).ifPresent(Team::unregister);
    }

    @Override
    public void updateTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix, @NotNull NameVisibility visibility, @NotNull CollisionRule collision, int options) {
        Team team = player.getPlayer().getScoreboard().getTeam(name).orElse(null);
        if (team == null) return;
        team.setDisplayName(Text.of(name));
        team.setPrefix(Text.of(prefix));
        team.setSuffix(Text.of(suffix));
        team.setAllowFriendlyFire((options & 0x01) != 0);
        team.setCanSeeFriendlyInvisibles((options & 0x02) != 0);
        team.setCollisionRule(convertCollisionRule(collision));
        team.setNameTagVisibility(convertVisibility(visibility));
    }

    private org.spongepowered.api.scoreboard.CollisionRule convertCollisionRule(CollisionRule rule) {
        switch (rule) {
            case ALWAYS: return CollisionRules.ALWAYS;
            case NEVER: return CollisionRules.NEVER;
            case PUSH_OTHER_TEAMS: return CollisionRules.PUSH_OTHER_TEAMS;
            case PUSH_OWN_TEAM: return CollisionRules.PUSH_OWN_TEAM;
            default: throw new IllegalArgumentException();
        }
    }

    private Visibility convertVisibility(NameVisibility visibility) {
        switch (visibility) {
            case ALWAYS: return Visibilities.ALWAYS;
            case NEVER: return Visibilities.NEVER;
            case HIDE_FOR_OTHER_TEAMS: return Visibilities.HIDE_FOR_OTHER_TEAMS;
            case HIDE_FOR_OWN_TEAM: return Visibilities.HIDE_FOR_OWN_TEAM;
            default: throw new IllegalArgumentException();
        }
    }

    @Override
    public void setScore0(@NotNull String objective, @NotNull String playerName, int score) {
        player.getPlayer().getScoreboard().getObjective(objective).ifPresent(o -> o.getOrCreateScore(Text.of(playerName)).setScore(score));
    }

    @Override
    public void removeScore0(@NotNull String objective, @NotNull String playerName) {
        player.getPlayer().getScoreboard().getObjective(objective).ifPresent(o -> o.removeScore(Text.of(playerName)));
    }
}
