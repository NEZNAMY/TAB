package me.neznamy.tab.platforms.bukkit.scoreboard;

import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.hook.AdventureHook;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

/**
 * Scoreboard handler using Paper API, which got added
 * in early 1.16.5 builds. Unlike the Bukkit API, the new
 * methods using adventure components do not have any
 * pointless artificial limits added in.
 * <p>
 * However, it still inherits the issue with compatibility
 * with other plugins that create scoreboards.
 */
public class PaperScoreboard extends BukkitScoreboard {

    /** Flag tracking whether this implementation is available for use */
    @Getter
    private static final boolean available = ReflectionUtils.classExists("net.kyori.adventure.text.Component") &&
            ReflectionUtils.methodExists(Team.class, "prefix", Component.class);

    /**
     * Constructs new instance with given player and puts them into new scoreboard.
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public PaperScoreboard(@NotNull BukkitTabPlayer player) {
        super(player);
    }

    @Override
    public void newObjective(String objectiveName, String criteria, String title, int display) {
        scoreboard.registerNewObjective(objectiveName, criteria, toAdventure(title), RenderType.values()[display]);
    }

    @Override
    public void setDisplayName(@NotNull Objective objective, @NotNull String displayName) {
        objective.displayName(toAdventure(displayName));
    }

    @Override
    public void setPrefix(@NotNull Team team, @NotNull String prefix) {
        team.prefix(toAdventure(prefix));
    }

    @Override
    public void setSuffix(@NotNull Team team, @NotNull String suffix) {
        team.suffix(toAdventure(suffix));
    }

    /**
     * Converts raw text into adventure component
     *
     * @param   text
     *          Text to convert
     * @return  Converted component
     */
    @NotNull
    private Component toAdventure(@NotNull String text) {
        return AdventureHook.toAdventureComponent(TabComponent.optimized(text), player.getVersion());
    }
}
