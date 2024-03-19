package me.neznamy.tab.platforms.bukkit.scoreboard;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

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

    /** Flag tracking presence of NumberFormat API added in late 1.20.4 builds */
    @Getter
    private static final boolean numberFormatAPI = ReflectionUtils.classExists("io.papermc.paper.scoreboard.numbers.NumberFormat");

    /**
     * Constructs new instance with given player and puts them into new scoreboard.
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public PaperScoreboard(@NonNull BukkitTabPlayer player) {
        super(player);
    }

    @Override
    public Objective newObjective(String objectiveName, String criteria, String title, int display) {
        return scoreboard.registerNewObjective(objectiveName, criteria,
                (Component) TabComponent.optimized(title).convert(player.getVersion()), RenderType.values()[display]);
    }

    @Override
    public void setDisplayName(@NonNull Objective objective, @NonNull String displayName) {
        objective.displayName(TabComponent.optimized(displayName).convert(player.getVersion()));
    }

    @Override
    public void setPrefix(@NonNull Team team, @NonNull String prefix) {
        team.prefix(TabComponent.optimized(prefix).convert(player.getVersion()));
    }

    @Override
    public void setSuffix(@NonNull Team team, @NonNull String suffix) {
        team.suffix(TabComponent.optimized(suffix).convert(player.getVersion()));
    }

    @Override
    @SneakyThrows
    public void setScoreDisplayName(@NotNull Score s, @Nullable TabComponent displayName) {
        if (numberFormatAPI) {
            Component component = displayName == null ? null : displayName.convert(player.getVersion());
            Method m = s.getClass().getMethod("customName", Component.class);
            m.setAccessible(true); // Why is this needed? The method is public!
            m.invoke(s, component);
        }
    }

    @Override
    @SneakyThrows
    public void setScoreNumberFormat(@NotNull Score s, @Nullable TabComponent numberFormat) {
        if (numberFormatAPI) {
            Class<?> numberFormatClass = Class.forName("io.papermc.paper.scoreboard.numbers.NumberFormat");
            Method m = s.getClass().getMethod("numberFormat", numberFormatClass);
            m.setAccessible(true); // Why is this needed? The method is public!
            m.invoke(s, numberFormat(numberFormat));
        }
    }

    @Override
    @SneakyThrows
    public void setObjectiveNumberFormat(@NotNull Objective o, @Nullable TabComponent numberFormat) {
        if (numberFormatAPI) {
            Class<?> numberFormatClass = Class.forName("io.papermc.paper.scoreboard.numbers.NumberFormat");
            Method m = o.getClass().getMethod("numberFormat", numberFormatClass);
            m.setAccessible(true); // Why is this needed? The method is public!
            m.invoke(o, numberFormat(numberFormat));
        }
    }

    @Nullable
    @SneakyThrows
    private Object numberFormat(@Nullable TabComponent numberFormat) {
        if (numberFormat == null) return null;
        return Class.forName("io.papermc.paper.scoreboard.numbers.NumberFormat").getMethod("fixed", ComponentLike.class)
                .invoke(null, numberFormat.convert(player.getVersion()));
    }
}
