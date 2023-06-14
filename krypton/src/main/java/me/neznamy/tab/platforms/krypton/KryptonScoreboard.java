package me.neznamy.tab.platforms.krypton;

import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.kryptonmc.api.scoreboard.Objective;
import org.kryptonmc.api.scoreboard.ObjectiveRenderType;
import org.kryptonmc.api.scoreboard.Team;
import org.kryptonmc.api.scoreboard.Visibility;
import org.kryptonmc.api.scoreboard.criteria.Criteria;

import java.util.Collection;

public class KryptonScoreboard extends Scoreboard<KryptonTabPlayer> {

    public KryptonScoreboard(@NotNull KryptonTabPlayer player) {
        super(player);
        // Create a new, blank scoreboard for each player to avoid conflicts
        player.getPlayer().showScoreboard(org.kryptonmc.api.scoreboard.Scoreboard.create());
    }

    @Override
    public void setDisplaySlot(@NotNull DisplaySlot slot, @NotNull String objectiveName) {
        Objective objective = getScoreboard().getObjective(objectiveName);
        if (objective != null) getScoreboard().updateSlot(objective, convertDisplaySlot(slot));
    }

    private org.kryptonmc.api.scoreboard.DisplaySlot convertDisplaySlot(DisplaySlot slot) {
        return org.kryptonmc.api.scoreboard.DisplaySlot.values()[slot.ordinal()];
    }

    @Override
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, boolean hearts) {
        Objective ignored = getScoreboard().createObjectiveBuilder()
                .name(objectiveName)
                .criterion(Criteria.DUMMY.get())
                .displayName(IChatBaseComponent.optimizedComponent(title).toAdventureComponent(player.getVersion()))
                .renderType(hearts ? ObjectiveRenderType.HEARTS : ObjectiveRenderType.INTEGER)
                .buildAndRegister();
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        Objective objective = getScoreboard().getObjective(objectiveName);
        if (objective != null) getScoreboard().removeObjective(objective);
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, boolean hearts) {
        Objective objective = getScoreboard().getObjective(objectiveName);
        if (objective == null) return;
        objective.setDisplayName(IChatBaseComponent.optimizedComponent(title).toAdventureComponent(player.getVersion()));
        objective.setRenderType(hearts ? ObjectiveRenderType.HEARTS : ObjectiveRenderType.INTEGER);
    }

    @Override
    public void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix, @NotNull NameVisibility visibility, @NotNull CollisionRule collision, @NotNull Collection<String> players, int options) {
        Team team = getScoreboard().createTeamBuilder(name)
                .displayName(IChatBaseComponent.optimizedComponent(name).toAdventureComponent(player.getVersion()))
                .prefix(IChatBaseComponent.optimizedComponent(prefix).toAdventureComponent(player.getVersion()))
                .suffix(IChatBaseComponent.optimizedComponent(suffix).toAdventureComponent(player.getVersion()))
                .friendlyFire((options & 0x01) != 0)
                .canSeeInvisibleMembers((options & 0x02) != 0)
                .collisionRule(org.kryptonmc.api.scoreboard.CollisionRule.valueOf(collision.name()))
                .nameTagVisibility(Visibility.valueOf(visibility.name()))
                .buildAndRegister();
        for (String member : players) {
            team.addMember(IChatBaseComponent.optimizedComponent(member).toAdventureComponent(player.getVersion()));
        }
    }

    @Override
    public void unregisterTeam0(@NotNull String name) {
        Team team = getScoreboard().getTeam(name);
        if (team != null) getScoreboard().removeTeam(team);
    }

    @Override
    public void updateTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix, @NotNull NameVisibility visibility, @NotNull CollisionRule collision, int options) {
        Team team = getScoreboard().getTeam(name);
        if (team == null) return;
        team.setDisplayName(IChatBaseComponent.optimizedComponent(name).toAdventureComponent(player.getVersion()));
        team.setPrefix(IChatBaseComponent.optimizedComponent(prefix).toAdventureComponent(player.getVersion()));
        team.setSuffix(IChatBaseComponent.optimizedComponent(suffix).toAdventureComponent(player.getVersion()));
        team.setAllowFriendlyFire((options & 0x01) != 0);
        team.setCanSeeInvisibleMembers((options & 0x02) != 0);
        team.setCollisionRule(org.kryptonmc.api.scoreboard.CollisionRule.valueOf(collision.name()));
        team.setNameTagVisibility(Visibility.valueOf(visibility.name()));
    }

    @Override
    public void setScore0(@NotNull String objectiveName, @NotNull String playerName, int score) {
        Objective objective = getScoreboard().getObjective(objectiveName);
        if (objective != null)
            objective.getOrCreateScore(IChatBaseComponent.optimizedComponent(playerName)
                    .toAdventureComponent(player.getVersion())).setScore(score);
    }

    @Override
    public void removeScore0(@NotNull String objectiveName, @NotNull String playerName) {
        Objective objective = getScoreboard().getObjective(objectiveName);
        if (objective != null) objective.removeScore(IChatBaseComponent.optimizedComponent(playerName).toAdventureComponent(player.getVersion()));
    }

    private org.kryptonmc.api.scoreboard.Scoreboard getScoreboard() {
        return player.getPlayer().getScoreboard();
    }
}