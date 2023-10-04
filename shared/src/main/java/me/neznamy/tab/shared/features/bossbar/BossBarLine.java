package me.neznamy.tab.shared.features.bossbar;

import java.util.*;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class representing a BossBar from configuration
 */
public class BossBarLine implements BossBar {

    //BossBar name
    @Getter private final String name;

    //display condition
    private final Condition displayCondition;

    //uuid
    @Getter private final UUID uniqueId = UUID.randomUUID();

    //BossBar style
    @Getter private String style;

    //BossBar color
    @Getter private String color;

    //BossBar title
    @Getter private String title;

    //BossBar progress
    @Getter private String progress;

    @Getter private final boolean announcementBar;

    //set of players seeing this BossBar
    private final Set<TabPlayer> players = Collections.newSetFromMap(new WeakHashMap<>());

    //refreshers
    private final TextRefresher textRefresher;
    private final ProgressRefresher progressRefresher;
    private final ColorRefresher colorRefresher;
    private final StyleRefresher styleRefresher;

    //property names
    private final String propertyTitle;
    private final String propertyProgress;
    private final String propertyColor;
    private final String propertyStyle;

    /**
     * Constructs new instance with given parameters
     *
     * @param   manager
     *          BossBar manager to count sent packets for
     * @param   name
     *          name of BossBar
     * @param   displayCondition
     *          display condition
     * @param   color
     *          BossBar color
     * @param   style
     *          BossBar style
     * @param   title
     *          BossBar title
     * @param   progress
     *          BossBar progress
     */
    public BossBarLine(@NonNull BossBarManagerImpl manager, @NonNull String name, @Nullable String displayCondition,
                       @NonNull String color, @NonNull String style, @NonNull String title, @NonNull String progress, boolean announcementOnly) {
        this.name = name;
        this.displayCondition = Condition.getCondition(displayCondition);
        if (this.displayCondition != null) {
            manager.addUsedPlaceholders(Collections.singletonList(TabConstants.Placeholder.condition(this.displayCondition.getName())));
        }
        this.color = color;
        this.style = style;
        this.title = title;
        this.progress = progress;
        this.announcementBar = announcementOnly;
        propertyTitle = TabConstants.Property.bossbarTitle(name);
        propertyProgress = TabConstants.Property.bossbarProgress(name);
        propertyColor = TabConstants.Property.bossbarColor(name);
        propertyStyle = TabConstants.Property.bossbarStyle(name);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.bossBarTitle(name),
                textRefresher = new TextRefresher());
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.bossBarProgress(name),
                progressRefresher = new ProgressRefresher());
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.bossBarColor(name),
                colorRefresher = new ColorRefresher());
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.bossBarStyle(name),
                styleRefresher = new StyleRefresher());
    }

    /**
     * Returns true if condition is null or is met, false otherwise.
     *
     * @param   p
     *          player to check condition for
     * @return  true if met, false if not
     */
    public boolean isConditionMet(@NonNull TabPlayer p) {
        if (displayCondition == null) return true;
        return displayCondition.isMet(p);
    }

    /**
     * Parses string into color and returns it. If parsing failed, PURPLE is returned.
     *
     * @param   color
     *          string to parse
     * @return  parsed color
     */
    public @NotNull BarColor parseColor(@NonNull String color) {
        try {
            return BarColor.valueOf(color);
        } catch (IllegalArgumentException e) {
            return BarColor.PURPLE;
        }
    }

    /**
     * Parses string into style and returns it. If parsing failed, PROGRESS is returned.
     *
     * @param   style
     *          string to parse
     * @return  parsed style
     */
    public @NotNull BarStyle parseStyle(@NonNull String style) {
        try {
            return BarStyle.valueOf(style);
        } catch (IllegalArgumentException e) {
            return BarStyle.PROGRESS;
        }
    }

    /**
     * Parses string into progress and returns it. If parsing failed, 100 is returned instead and
     * error message is printed into error log
     *
     * @param   progress
     *          string to parse
     * @return  parsed progress
     */
    public float parseProgress(@NonNull TabPlayer player, @NonNull String progress) {
        try {
            float value = Float.parseFloat(progress);
            if (value < 0) value = 0;
            if (value > 100) value = 100;
            return value;
        } catch (NumberFormatException e) {
            TAB.getInstance().getMisconfigurationHelper().invalidNumberForBossBarProgress(
                    name,
                    progress,
                    player.getProperty(propertyProgress).getCurrentRawValue(),
                    player
            );
            return 100;
        }
    }

    @Override
    public void setTitle(@NonNull String title) {
        if (this.title.equals(title)) return;
        this.title = title;
        for (TabPlayer p : players) {
            p.setProperty(textRefresher, propertyTitle, title);
            p.getBossBar().update(uniqueId, p.getProperty(propertyTitle).get());
        }
    }

    @Override
    public void setProgress(@NonNull String progress) {
        if (this.progress.equals(progress)) return;
        this.progress = progress;
        for (TabPlayer p : players) {
            p.setProperty(progressRefresher, propertyProgress, progress);
            p.getBossBar().update(uniqueId, parseProgress(p, p.getProperty(propertyProgress).get())/100);
        }
    }

    @Override
    public void setProgress(float progress) {
        setProgress(String.valueOf(progress));
    }

    @Override
    public void setColor(@NonNull String color) {
        if (this.color.equals(color)) return;
        this.color = color;
        for (TabPlayer p : players) {
            p.setProperty(colorRefresher, propertyColor, color);
            p.getBossBar().update(uniqueId, parseColor(p.getProperty(propertyColor).get()));
        }
    }

    @Override
    public void setColor(@NonNull BarColor color) {
        setColor(color.toString());
    }

    @Override
    public void setStyle(@NonNull String style) {
        if (this.style.equals(style)) return;
        this.style = style;
        for (TabPlayer p : players) {
            p.setProperty(styleRefresher, propertyColor, style);
            p.getBossBar().update(uniqueId, parseStyle(p.getProperty(propertyStyle).get()));
        }
    }

    @Override
    public void setStyle(@NonNull BarStyle style) {
        setStyle(style.toString());
    }

    @Override
    public void addPlayer(me.neznamy.tab.api.@NonNull TabPlayer p) {
        TabPlayer player = (TabPlayer) p;
        if (players.contains(player)) return;
        players.add(player);
        player.setProperty(textRefresher, propertyTitle, title);
        player.setProperty(progressRefresher, propertyProgress, progress);
        player.setProperty(colorRefresher, propertyColor, color);
        player.setProperty(styleRefresher, propertyStyle, style);
        player.getBossBar().create(
                uniqueId,
                player.getProperty(propertyTitle).updateAndGet(),
                parseProgress(player, player.getProperty(propertyProgress).updateAndGet())/100,
                parseColor(player.getProperty(propertyColor).updateAndGet()),
                parseStyle(player.getProperty(propertyStyle).updateAndGet())
        );
    }

    @Override
    public void removePlayer(me.neznamy.tab.api.@NonNull TabPlayer p) {
        TabPlayer player = (TabPlayer) p;
        if (!players.contains(player)) return;
        players.remove(player);
        player.getBossBar().remove(uniqueId);
    }

    @Override
    public @NotNull List<me.neznamy.tab.api.TabPlayer> getPlayers() {
        return players.stream().filter(TabPlayer::isOnline).collect(Collectors.toList());
    }

    @Override
    public boolean containsPlayer(me.neznamy.tab.api.TabPlayer player) {
        return players.contains((TabPlayer) player);
    }

    public class TextRefresher extends TabFeature implements Refreshable {

        @Getter private final String featureName = "BossBar";
        @Getter private final String refreshDisplayName = "Updating text";

        @Override
        public void refresh(@NotNull TabPlayer refreshed, boolean force) {
            if (!players.contains(refreshed)) return;
            refreshed.getBossBar().update(uniqueId, refreshed.getProperty(propertyTitle).updateAndGet());
        }
    }

    public class ProgressRefresher extends TabFeature implements Refreshable {

        @Getter private final String featureName = "BossBar";
        @Getter private final String refreshDisplayName = "Updating progress";

        @Override
        public void refresh(@NotNull TabPlayer refreshed, boolean force) {
            if (!players.contains(refreshed)) return;
            refreshed.getBossBar().update(uniqueId, parseProgress(refreshed, refreshed.getProperty(propertyProgress).updateAndGet())/100);
        }
    }

    public class ColorRefresher extends TabFeature implements Refreshable {

        @Getter private final String featureName = "BossBar";
        @Getter private final String refreshDisplayName = "Updating color";

        @Override
        public void refresh(@NotNull TabPlayer refreshed, boolean force) {
            if (!players.contains(refreshed)) return;
            refreshed.getBossBar().update(uniqueId, parseColor(refreshed.getProperty(propertyColor).updateAndGet()));
        }
    }

    public class StyleRefresher extends TabFeature implements Refreshable {

        @Getter private final String featureName = "BossBar";
        @Getter private final String refreshDisplayName = "Updating style";

        @Override
        public void refresh(@NotNull TabPlayer refreshed, boolean force) {
            if (!players.contains(refreshed)) return;
            refreshed.getBossBar().update(uniqueId, parseStyle(refreshed.getProperty(propertyStyle).updateAndGet()));
        }
    }
}