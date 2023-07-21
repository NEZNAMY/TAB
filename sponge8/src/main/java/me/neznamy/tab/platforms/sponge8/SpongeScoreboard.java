package me.neznamy.tab.platforms.sponge8;

import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.scoreboard.*;
import org.spongepowered.api.scoreboard.criteria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;

import java.util.Collection;

public class SpongeScoreboard extends Scoreboard<SpongeTabPlayer> {

    /** Scoreboard of the player */
    private final org.spongepowered.api.scoreboard.Scoreboard sb = org.spongepowered.api.scoreboard.Scoreboard.builder().build();
    
    public SpongeScoreboard(SpongeTabPlayer player) {
        super(player);
        // Make sure each player is in different scoreboard for per-player view
        player.getPlayer().setScoreboard(sb);
    }

    @Override
    public void setDisplaySlot(@NotNull DisplaySlot slot, @NotNull String objective) {
        sb.objective(objective).ifPresent(o -> sb.updateDisplaySlot(o, convertDisplaySlot(slot)));
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
        sb.addObjective(Objective.builder()
                .name(objectiveName)
                .displayName(adventure(title))
                .objectiveDisplayMode(hearts ? ObjectiveDisplayModes.HEARTS : ObjectiveDisplayModes.INTEGER)
                .criterion(Criteria.DUMMY)
                .build());
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        sb.objective(objectiveName).ifPresent(sb::removeObjective);
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, boolean hearts) {
        Objective obj = sb.objective(objectiveName).orElseThrow(IllegalStateException::new);
        obj.setDisplayName(adventure(title));
        obj.setDisplayMode(hearts ? ObjectiveDisplayModes.HEARTS.get() : ObjectiveDisplayModes.INTEGER.get());
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
                .collisionRule(convertCollisionRule(collision))
                .nameTagVisibility(convertVisibility(visibility))
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
            team.setCollisionRule(convertCollisionRule(collision));
            team.setNameTagVisibility(convertVisibility(visibility));
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

    /**
     * Converts text to Adventure component.
     *
     * @param   text
     *          Text to convert
     * @return  Converted text
     */
    private Component adventure(String text) {
        return IChatBaseComponent.optimizedComponent(text).toAdventureComponent(player.getVersion());
    }
}
