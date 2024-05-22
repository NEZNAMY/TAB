package me.neznamy.tab.shared.features.bossbar;

import java.util.*;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.Property;
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
    private final Set<TabPlayer> players = new HashSet<>();

    //refreshers
    private final TextRefresher textRefresher;
    private final ProgressRefresher progressRefresher;
    private final ColorRefresher colorRefresher;
    private final StyleRefresher styleRefresher;

    //property names
    private final String propertyTitle = Property.randomName();
    private final String propertyProgress = Property.randomName();
    private final String propertyColor = Property.randomName();
    private final String propertyStyle = Property.randomName();

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
     * @param   announcementOnly
     *          Whether this bossbar is for announcements only
     */
    public BossBarLine(@NonNull BossBarManagerImpl manager, @NonNull String name, @Nullable String displayCondition,
                       @NonNull String color, @NonNull String style, @NonNull String title, @NonNull String progress, boolean announcementOnly) {
        this.name = name;
        this.displayCondition = Condition.getCondition(displayCondition);
        if (this.displayCondition != null) {
            manager.addUsedPlaceholder(TabConstants.Placeholder.condition(this.displayCondition.getName()));
        }
        this.color = color;
        this.style = style;
        this.title = title;
        this.progress = progress;
        announcementBar = announcementOnly;
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
    @NotNull
    public BarColor parseColor(@NonNull String color) {
        try {
            return BarColor.valueOf(color);
        } catch (IllegalArgumentException e) {
            // TODO send warn
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
    @NotNull
    public BarStyle parseStyle(@NonNull String style) {
        try {
            return BarStyle.valueOf(style);
        } catch (IllegalArgumentException e) {
            // TODO send warn
            return BarStyle.PROGRESS;
        }
    }

    /**
     * Parses string into progress and returns it. If parsing failed, 100 is returned instead and
     * error message is printed into error log
     *
     * @param   player
     *          Player to parse the value for
     * @param   progress
     *          string to parse
     * @return  parsed progress
     */
    public float parseProgress(@NotNull TabPlayer player, @NotNull String progress) {
        try {
            float value = Float.parseFloat(progress);
            if (value < 0) value = 0;
            if (value > 100) value = 100;
            return value;
        } catch (NumberFormatException e) {
            TAB.getInstance().getConfigHelper().runtime().invalidNumberForBossBarProgress(
                    this,
                    progress,
                    player.getProperty(propertyProgress).getCurrentRawValue(),
                    player
            );
            return 100;
        }
    }

    /**
     * Resends bossbar to the player.
     *
     * @param   player
     *          Player to resend bossbar to
     */
    public void sendToPlayerRaw(@NotNull TabPlayer player) {
        player.getBossBar().create(
                uniqueId,
                player.getProperty(propertyTitle).updateAndGet(),
                parseProgress(player, player.getProperty(propertyProgress).updateAndGet())/100,
                parseColor(player.getProperty(propertyColor).updateAndGet()),
                parseStyle(player.getProperty(propertyStyle).updateAndGet())
        );
    }

    /**
     * Removes player from set of players.
     *
     * @param   player
     *          Player to remove
     */
    public void removePlayerRaw(@NotNull TabPlayer player) {
        players.remove(player);
    }

    // ------------------
    // API Implementation
    // ------------------

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
    public void addPlayer(@NonNull me.neznamy.tab.api.TabPlayer p) {
        TabPlayer player = (TabPlayer) p;
        if (players.contains(player)) return;
        player.setProperty(textRefresher, propertyTitle, title);
        player.setProperty(progressRefresher, propertyProgress, progress);
        player.setProperty(colorRefresher, propertyColor, color);
        player.setProperty(styleRefresher, propertyStyle, style);
        sendToPlayerRaw(player);
        players.add(player);
    }

    @Override
    public void removePlayer(@NonNull me.neznamy.tab.api.TabPlayer p) {
        TabPlayer player = (TabPlayer) p;
        if (!players.contains(player)) return;
        players.remove(player);
        player.getBossBar().remove(uniqueId);
    }

    @Override
    @NotNull
    public List<me.neznamy.tab.api.TabPlayer> getPlayers() {
        return new ArrayList<>(players);
    }

    @Override
    public boolean containsPlayer(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return players.contains((TabPlayer) player);
    }

    private class TextRefresher extends TabFeature implements Refreshable {

        @Override
        public void refresh(@NotNull TabPlayer refreshed, boolean force) {
            if (!players.contains(refreshed)) return;
            refreshed.getBossBar().update(uniqueId, refreshed.getProperty(propertyTitle).updateAndGet());
        }

        @Override
        @NotNull
        public String getRefreshDisplayName() {
            return "Updating text";
        }

        @Override
        @NotNull
        public String getFeatureName() {
            return "BossBar";
        }
    }

    private class ProgressRefresher extends TabFeature implements Refreshable {

        @Override
        public void refresh(@NotNull TabPlayer refreshed, boolean force) {
            if (!players.contains(refreshed)) return;
            refreshed.getBossBar().update(uniqueId, parseProgress(refreshed, refreshed.getProperty(propertyProgress).updateAndGet())/100);
        }

        @Override
        @NotNull
        public String getRefreshDisplayName() {
            return "Updating progress";
        }

        @Override
        @NotNull
        public String getFeatureName() {
            return "BossBar";
        }
    }

    private class ColorRefresher extends TabFeature implements Refreshable {

        @Override
        public void refresh(@NotNull TabPlayer refreshed, boolean force) {
            if (!players.contains(refreshed)) return;
            refreshed.getBossBar().update(uniqueId, parseColor(refreshed.getProperty(propertyColor).updateAndGet()));
        }

        @Override
        @NotNull
        public String getRefreshDisplayName() {
            return "Updating color";
        }

        @Override
        @NotNull
        public String getFeatureName() {
            return "BossBar";
        }
    }

    private class StyleRefresher extends TabFeature implements Refreshable {

        @Override
        public void refresh(@NotNull TabPlayer refreshed, boolean force) {
            if (!players.contains(refreshed)) return;
            refreshed.getBossBar().update(uniqueId, parseStyle(refreshed.getProperty(propertyStyle).updateAndGet()));
        }

        @Override
        @NotNull
        public String getRefreshDisplayName() {
            return "Updating style";
        }

        @Override
        @NotNull
        public String getFeatureName() {
            return "BossBar";
        }
    }
}