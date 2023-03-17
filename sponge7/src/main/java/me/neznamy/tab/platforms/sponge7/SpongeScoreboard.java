package me.neznamy.tab.platforms.sponge7;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.util.ComponentCache;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabScoreboard;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.*;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SpongeScoreboard extends TabScoreboard {

    private static final ComponentCache<IChatBaseComponent, Text> textCache = new ComponentCache<>(10000,
            (component, version) -> TextSerializers.JSON.deserialize(component.toString(version)));
    
    private final Map<String, Objective> objectives = new HashMap<>();
    
    private final Player spongePlayer;
    
    public SpongeScoreboard(TabPlayer player) {
        super(player);
        spongePlayer = (Player) player.getPlayer();
    }

    @Override
    public void setDisplaySlot(me.neznamy.tab.api.DisplaySlot slot, @NonNull String objective) {
        spongePlayer.getScoreboard().updateDisplaySlot(objectives.get(objective), convertDisplaySlot(slot));
    }

    private static DisplaySlot convertDisplaySlot(me.neznamy.tab.api.DisplaySlot slot) {
        switch (slot) {
            case PLAYER_LIST: return DisplaySlots.LIST;
            case SIDEBAR: return DisplaySlots.SIDEBAR;
            default: return DisplaySlots.BELOW_NAME;
        }
    }

    @Override
    public void registerObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        String displayName = TAB.getInstance().getPlatform().getPacketBuilder().cutTo(title, 32);
        Objective objective = Objective.builder()
                .name(objectiveName)
                .displayName(textCache.get(IChatBaseComponent.optimizedComponent(displayName), player.getVersion()))
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
        String displayName = TAB.getInstance().getPlatform().getPacketBuilder().cutTo(title, 32);
        Objective obj = objectives.get(objectiveName);
        obj.setDisplayName(textCache.get(IChatBaseComponent.optimizedComponent(displayName), player.getVersion()));
        obj.setDisplayMode(hearts ? ObjectiveDisplayModes.HEARTS : ObjectiveDisplayModes.INTEGER);
    }

    @Override
    public void registerTeam0(@NonNull String name, String prefix, String suffix, String visibility, String collision, Collection<String> players, int options) {
        Team team = Team.builder()
                .name(name)
                .displayName(textCache.get(IChatBaseComponent.optimizedComponent(name), player.getVersion()))
                .prefix(textCache.get(IChatBaseComponent.optimizedComponent(TAB.getInstance().getPlatform().getPacketBuilder().cutTo(prefix, 16)), player.getVersion()))
                .suffix(textCache.get(IChatBaseComponent.optimizedComponent(TAB.getInstance().getPlatform().getPacketBuilder().cutTo(suffix, 16)), player.getVersion()))
                .allowFriendlyFire((options & 0x01) != 0)
                .canSeeFriendlyInvisibles((options & 0x02) != 0)
                .collisionRule(convertCollisionRule(collision))
                .nameTagVisibility(convertVisibility(visibility))
                .build();
        for (String member : players) {
            team.addMember(textCache.get(IChatBaseComponent.optimizedComponent(member), player.getVersion()));
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
        team.setDisplayName(textCache.get(IChatBaseComponent.optimizedComponent(name), player.getVersion()));
        team.setPrefix(textCache.get(IChatBaseComponent.optimizedComponent(TAB.getInstance().getPlatform().getPacketBuilder().cutTo(prefix, 16)), player.getVersion()));
        team.setSuffix(textCache.get(IChatBaseComponent.optimizedComponent(TAB.getInstance().getPlatform().getPacketBuilder().cutTo(prefix, 16)), player.getVersion()));
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
        objectives.get(objective).getOrCreateScore(textCache.get(IChatBaseComponent.optimizedComponent(playerName), player.getVersion())).setScore(score);
    }

    @Override
    public void removeScore0(@NonNull String objective, @NonNull String playerName) {
        objectives.get(objective).removeScore(textCache.get(IChatBaseComponent.optimizedComponent(playerName), player.getVersion()));
    }
}
