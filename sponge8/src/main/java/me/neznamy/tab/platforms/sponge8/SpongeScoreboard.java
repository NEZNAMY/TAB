package me.neznamy.tab.platforms.sponge8;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.hook.AdventureHook;
import me.neznamy.tab.shared.platform.Scoreboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.scoreboard.CollisionRules;
import org.spongepowered.api.scoreboard.Score;
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
    public void setDisplaySlot0(int slot, @NonNull String objective) {
        sb.objective(objective).ifPresent(o -> sb.updateDisplaySlot(o, displaySlots[slot]));
    }

    @Override
    public void registerObjective0(@NonNull String objectiveName, @NonNull String title, int display,
                                   @Nullable TabComponent numberFormat) {
        sb.addObjective(Objective.builder()
                .name(objectiveName)
                .displayName(adventure(title))
                .objectiveDisplayMode(healthDisplays[display])
                .criterion(Criteria.DUMMY)
                .build()
        );
    }

    @Override
    public void unregisterObjective0(@NonNull String objectiveName) {
        sb.objective(objectiveName).ifPresent(sb::removeObjective);
    }

    @Override
    public void updateObjective0(@NonNull String objectiveName, @NonNull String title, int display,
                                 @Nullable TabComponent numberFormat) {
        sb.objective(objectiveName).ifPresent(obj -> {
            obj.setDisplayName(adventure(title));
            obj.setDisplayMode(healthDisplays[display]);
        });
     }

    @Override
    public void registerTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix,
                              @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                              @NonNull Collection<String> players, int options, @NonNull EnumChatFormat color) {
        Team team = Team.builder()
                .name(name)
                .displayName(adventure(name))
                .prefix(adventure(prefix))
                .suffix(adventure(suffix))
                .color(NamedTextColor.NAMES.valueOr(color.name(), NamedTextColor.WHITE))
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
    public void unregisterTeam0(@NonNull String name) {
        sb.team(name).ifPresent(Team::unregister);
    }

    @Override
    public void updateTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix,
                            @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                            int options, @NonNull EnumChatFormat color) {
        sb.team(name).ifPresent(team -> {
            team.setDisplayName(adventure(name));
            team.setPrefix(adventure(prefix));
            team.setSuffix(adventure(suffix));
            team.setColor(NamedTextColor.NAMES.valueOr(color.name(), NamedTextColor.WHITE));
            team.setAllowFriendlyFire((options & 0x01) != 0);
            team.setCanSeeFriendlyInvisibles((options & 0x02) != 0);
            team.setCollisionRule(collisionRules[collision.ordinal()]);
            team.setNameTagVisibility(visibilities[visibility.ordinal()]);
        });
    }

    @Override
    public void setScore0(@NonNull String objective, @NonNull String scoreHolder, int score,
                          @Nullable TabComponent displayName, @Nullable TabComponent numberFormat) {
        sb.objective(objective).ifPresent(o -> findOrCreateScore(o, scoreHolder).setScore(score));
    }

    @Override
    public void removeScore0(@NonNull String objective, @NonNull String scoreHolder) {
        sb.objective(objective).ifPresent(o -> o.removeScore(findOrCreateScore(o, scoreHolder)));
    }

    @NotNull
    @SneakyThrows
    private Score findOrCreateScore(@NotNull Objective objective, @NonNull String holder) {
        try {
            // Sponge 8 - 10
            return objective.findOrCreateScore(adventure(holder));
        } catch (NoSuchMethodError e) {
            // Sponge 11+
            return (Score) objective.getClass().getMethod("findOrCreateScore", String.class).invoke(objective, holder);
        }
    }

    /**
     * Converts text to Adventure component.
     *
     * @param   text
     *          Text to convert
     * @return  Converted text
     */
    @NotNull
    private Component adventure(@NonNull String text) {
        return AdventureHook.toAdventureComponent(TabComponent.optimized(text), player.getVersion().supportsRGB());
    }
}
