package me.neznamy.tab.platforms.sponge8;

import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.hook.AdventureHook;
import me.neznamy.tab.shared.platform.Scoreboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.scoreboard.CollisionRules;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.Visibilities;
import org.spongepowered.api.scoreboard.Visibility;
import org.spongepowered.api.scoreboard.criteria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayMode;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;

import java.util.Collection;

/**
 * Scoreboard implementation for Sponge 8 using its API.
 */
public class SpongeScoreboard extends Scoreboard<SpongeTabPlayer> {

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
        sb.objective(objective).ifPresent(o -> sb.updateDisplaySlot(o, displaySlots[slot]));
    }

    @Override
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, int display,
                                   @Nullable IChatBaseComponent numberFormat) {
        sb.addObjective(Objective.builder()
                .name(objectiveName)
                .displayName(adventure(title))
                .objectiveDisplayMode(healthDisplays[display])
                .criterion(Criteria.DUMMY)
                .build()
        );
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        sb.objective(objectiveName).ifPresent(sb::removeObjective);
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, int display,
                                 @Nullable IChatBaseComponent numberFormat) {
        sb.objective(objectiveName).ifPresent(obj -> {
            obj.setDisplayName(adventure(title));
            obj.setDisplayMode(healthDisplays[display]);
        });
     }

    @Override
    public void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                              @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                              @NotNull Collection<String> players, int options, @NotNull EnumChatFormat color) {
        Team team = Team.builder()
                .name(name)
                .displayName(adventure(name))
                .prefix(adventure(prefix))
                .suffix(adventure(suffix))
                .color(NamedTextColor.namedColor(color.getHexCode()))
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
                            @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                            int options, @NotNull EnumChatFormat color) {
        sb.team(name).ifPresent(team -> {
            team.setDisplayName(adventure(name));
            team.setPrefix(adventure(prefix));
            team.setSuffix(adventure(suffix));
            team.setColor(NamedTextColor.namedColor(color.getHexCode()));
            team.setAllowFriendlyFire((options & 0x01) != 0);
            team.setCanSeeFriendlyInvisibles((options & 0x02) != 0);
            team.setCollisionRule(collisionRules[collision.ordinal()]);
            team.setNameTagVisibility(visibilities[visibility.ordinal()]);
        });
    }

    @Override
    public void setScore0(@NotNull String objective, @NotNull String scoreHolder, int score,
                          @Nullable IChatBaseComponent displayName, @Nullable IChatBaseComponent numberFormat) {
        sb.objective(objective).ifPresent(o -> o.findOrCreateScore(adventure(scoreHolder)).setScore(score));
    }

    @Override
    public void removeScore0(@NotNull String objective, @NotNull String scoreHolder) {
        sb.objective(objective).ifPresent(o -> o.removeScore(adventure(scoreHolder)));
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
        return AdventureHook.toAdventureComponent(IChatBaseComponent.optimizedComponent(text), player.getVersion());
    }
}
