package me.neznamy.tab.platforms.sponge7;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TabScoreboard;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.*;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SpongeScoreboard extends TabScoreboard {

    private final Map<String, Objective> objectives = new HashMap<>();
    
    private final Player spongePlayer;
    
    public SpongeScoreboard(TabPlayer player) {
        super(player);
        spongePlayer = (Player) player.getPlayer();
    }

    @Override
    public void setDisplaySlot(DisplaySlot slot, @NonNull String objective) {
        spongePlayer.getScoreboard().updateDisplaySlot(objectives.get(objective), convertDisplaySlot(slot));
    }

    private static org.spongepowered.api.scoreboard.displayslot.DisplaySlot convertDisplaySlot(DisplaySlot slot) {
        switch (slot) {
            case PLAYER_LIST: return DisplaySlots.LIST;
            case SIDEBAR: return DisplaySlots.SIDEBAR;
            default: return DisplaySlots.BELOW_NAME;
        }
    }

    @Override
    public void registerObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        Objective objective = Objective.builder()
                .name(objectiveName)
                .displayName(Sponge7TAB.getTextCache().get(IChatBaseComponent.optimizedComponent(title), player.getVersion()))
                .objectiveDisplayMode(hearts ? ObjectiveDisplayModes.HEARTS : ObjectiveDisplayModes.INTEGER)
                .criterion(Criteria.DUMMY)
                .build();
        objectives.put(objectiveName, objective);
        spongePlayer.getScoreboard().addObjective(objective);
    }

    @Override
    public void unregisterObjective0(@NonNull String objectiveName) {
        spongePlayer.getScoreboard().removeObjective(objectives.get(objectiveName));
    }

    @Override
    public void updateObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        Objective obj = objectives.get(objectiveName);
        obj.setDisplayName(Sponge7TAB.getTextCache().get(IChatBaseComponent.optimizedComponent(title), player.getVersion()));
        obj.setDisplayMode(hearts ? ObjectiveDisplayModes.HEARTS : ObjectiveDisplayModes.INTEGER);
    }

    @Override
    public void registerTeam0(@NonNull String name, String prefix, String suffix, String visibility, String collision, Collection<String> players, int options) {
        Team team = Team.builder()
                .name(name)
                .displayName(Sponge7TAB.getTextCache().get(IChatBaseComponent.optimizedComponent(name), player.getVersion()))
                .prefix(Sponge7TAB.getTextCache().get(IChatBaseComponent.optimizedComponent(prefix), player.getVersion()))
                .suffix(Sponge7TAB.getTextCache().get(IChatBaseComponent.optimizedComponent(suffix), player.getVersion()))
                .allowFriendlyFire((options & 0x01) != 0)
                .canSeeFriendlyInvisibles((options & 0x02) != 0)
                .collisionRule(convertCollisionRule(collision))
                .nameTagVisibility(convertVisibility(visibility))
                .build();
        for (String member : players) {
            team.addMember(Sponge7TAB.getTextCache().get(IChatBaseComponent.optimizedComponent(member), player.getVersion()));
        }
        spongePlayer.getScoreboard().registerTeam(team);
    }

    @Override
    public void unregisterTeam0(@NonNull String name) {
        spongePlayer.getScoreboard().getTeam(name).ifPresent(Team::unregister);
    }

    @Override
    public void updateTeam0(@NonNull String name, String prefix, String suffix, String visibility, String collision, int options) {
        Team team = spongePlayer.getScoreboard().getTeam(name).orElse(null);
        if (team == null) return;
        team.setDisplayName(Sponge7TAB.getTextCache().get(IChatBaseComponent.optimizedComponent(name), player.getVersion()));
        team.setPrefix(Sponge7TAB.getTextCache().get(IChatBaseComponent.optimizedComponent(prefix), player.getVersion()));
        team.setSuffix(Sponge7TAB.getTextCache().get(IChatBaseComponent.optimizedComponent(suffix), player.getVersion()));
        team.setAllowFriendlyFire((options & 0x01) != 0);
        team.setCanSeeFriendlyInvisibles((options & 0x02) != 0);
        team.setCollisionRule(convertCollisionRule(collision));
        team.setNameTagVisibility(convertVisibility(visibility));
    }

    private static CollisionRule convertCollisionRule(String rule) {
        switch (rule) {
            case "always": return CollisionRules.ALWAYS;
            case "never": return CollisionRules.NEVER;
            case "pushOtherTeams": return CollisionRules.PUSH_OTHER_TEAMS;
            case "pushOwnTeam": return CollisionRules.PUSH_OWN_TEAM;
            default: throw new IllegalArgumentException();
        }
    }

    private static Visibility convertVisibility(String visibility) {
        switch (visibility) {
            case "always": return Visibilities.ALWAYS;
            case "never": return Visibilities.NEVER;
            case "hideForOtherTeams": return Visibilities.HIDE_FOR_OTHER_TEAMS;
            case "hideForOwnTeam": return Visibilities.HIDE_FOR_OWN_TEAM;
            default: throw new IllegalArgumentException();
        }
    }

    @Override
    public void setScore0(@NonNull String objective, @NonNull String playerName, int score) {
        objectives.get(objective).getOrCreateScore(Sponge7TAB.getTextCache().get(IChatBaseComponent.optimizedComponent(playerName), player.getVersion())).setScore(score);
    }

    @Override
    public void removeScore0(@NonNull String objective, @NonNull String playerName) {
        objectives.get(objective).removeScore(Sponge7TAB.getTextCache().get(IChatBaseComponent.optimizedComponent(playerName), player.getVersion()));
    }
}
