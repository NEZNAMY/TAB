package me.neznamy.tab.shared.features.bossbar;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.placeholder.ServerPlaceholder;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.PerformanceUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Class representing a BossBar from configuration
 */
public class BossBarLine implements BossBar {

    private final BossBarManagerImpl manager;

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

    private int announceTimeTotalSeconds;
    private long announceEndSystemTime;

    @NotNull
    private final ServerPlaceholder announceEndPlaceholder;

    /**
     * Constructs new instance with given parameters
     *
     * @param   manager
     *          BossBar manager to count sent packets for
     * @param   name
     *          name of BossBar
     * @param   configuration
     *          Boss bar configuration
     */
    public BossBarLine(@NonNull BossBarManagerImpl manager, @NonNull String name, @NonNull BossBarConfiguration.BossBarDefinition configuration) {
        this.manager = manager;
        this.name = name;
        displayCondition = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(configuration.getDisplayCondition());
        if (displayCondition != null) {
            manager.addUsedPlaceholder(TabConstants.Placeholder.condition(displayCondition.getName()));
        }
        color = configuration.getColor();
        style = configuration.getStyle();
        title = configuration.getText();
        progress = configuration.getProgress();
        announcementBar = configuration.isAnnouncementOnly();
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.bossBarTitle(name),
                textRefresher = new TextRefresher());
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.bossBarProgress(name),
                progressRefresher = new ProgressRefresher());
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.bossBarColor(name),
                colorRefresher = new ColorRefresher());
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.bossBarStyle(name),
                styleRefresher = new StyleRefresher());
        announceEndPlaceholder = TAB.getInstance().getPlaceholderManager().registerInternalServerPlaceholder(
                TabConstants.Placeholder.bossbarAnnounceTotal(name), -1, () -> PerformanceUtil.toString(announceTimeTotalSeconds));
        TAB.getInstance().getPlaceholderManager().registerInternalServerPlaceholder(
                TabConstants.Placeholder.bossbarAnnounceLeft(name), 100, () -> {
            long seconds = TimeUnit.MILLISECONDS.toSeconds(announceEndSystemTime - System.currentTimeMillis());
            if (seconds < 0) return "0";
            return PerformanceUtil.toString((int) seconds);
        });
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
     * @param   player
     *          Player to parse color for
     * @param   color
     *          string to parse
     * @return  parsed color
     */
    @NotNull
    public BarColor parseColor(@NotNull TabPlayer player, @NonNull String color) {
        try {
            return BarColor.valueOf(color);
        } catch (IllegalArgumentException e) {
            TAB.getInstance().getConfigHelper().runtime().invalidBossBarProperty(
                    this,
                    color,
                    player.bossbarData.visibleBossBars.get(this).colorProperty.getCurrentRawValue(),
                    player,
                    "color",
                    "one of the pre-defined values " + Arrays.toString(BarColor.values())
            );
            return BarColor.PURPLE;
        }
    }

    /**
     * Parses string into style and returns it. If parsing failed, PROGRESS is returned.
     *
     * @param   player
     *          Player to parse style for
     * @param   style
     *          string to parse
     * @return  parsed style
     */
    @NotNull
    public BarStyle parseStyle(@NotNull TabPlayer player, @NonNull String style) {
        try {
            return BarStyle.valueOf(style);
        } catch (IllegalArgumentException e) {
            TAB.getInstance().getConfigHelper().runtime().invalidBossBarProperty(
                    this,
                    style,
                    player.bossbarData.visibleBossBars.get(this).styleProperty.getCurrentRawValue(),
                    player,
                    "style",
                    "one of the pre-defined values " + Arrays.toString(BarStyle.values())
            );
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
            TAB.getInstance().getConfigHelper().runtime().invalidBossBarProperty(
                    this,
                    progress,
                    player.bossbarData.visibleBossBars.get(this).progressProperty.getCurrentRawValue(),
                    player,
                    "progress",
                    "a number between 0 and 100"
            );
            return 100;
        }
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

    public void announce(int timeSeconds) {
        announceTimeTotalSeconds = timeSeconds;
        announceEndSystemTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeSeconds);
        announceEndPlaceholder.update();
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (manager.hasBossBarVisible(all) && isConditionMet(all)) {
                addPlayer(all);
            }
        }
    }

    public boolean isBeingAnnounced() {
        return announceEndSystemTime > System.currentTimeMillis();
    }

    public void unAnnounce() {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (manager.hasBossBarVisible(all)) {
                removePlayer(all);
            }
        }
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    public void setTitle(@NonNull String title) {
        if (this.title.equals(title)) return;
        this.title = title;
        for (TabPlayer p : players) {
            p.bossbarData.visibleBossBars.get(this).textProperty.changeRawValue(title);
            p.getBossBar().update(uniqueId, manager.getCache().get(p.bossbarData.visibleBossBars.get(this).textProperty.get()));
        }
    }

    @Override
    public void setProgress(@NonNull String progress) {
        if (this.progress.equals(progress)) return;
        this.progress = progress;
        for (TabPlayer p : players) {
            p.bossbarData.visibleBossBars.get(this).progressProperty.changeRawValue(progress);
            p.getBossBar().update(uniqueId, parseProgress(p, p.bossbarData.visibleBossBars.get(this).progressProperty.get())/100);
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
            p.bossbarData.visibleBossBars.get(this).colorProperty.changeRawValue(color);
            p.getBossBar().update(uniqueId, parseColor(p, p.bossbarData.visibleBossBars.get(this).colorProperty.get()));
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
            p.bossbarData.visibleBossBars.get(this).styleProperty.changeRawValue(style);
            p.getBossBar().update(uniqueId, parseStyle(p, p.bossbarData.visibleBossBars.get(this).styleProperty.get()));
        }
    }

    @Override
    public void setStyle(@NonNull BarStyle style) {
        setStyle(style.toString());
    }

    @Override
    public void addPlayer(@NonNull me.neznamy.tab.api.TabPlayer p) {
        TabPlayer player = (TabPlayer) p;
        if (player.bossbarData.visibleBossBars.containsKey(this)) return;
        BossBarLinePlayerProperties properties = new BossBarLinePlayerProperties(
                new Property(textRefresher, player, title),
                new Property(progressRefresher, player, progress),
                new Property(colorRefresher, player, color),
                new Property(styleRefresher, player, style)
        );
        player.bossbarData.visibleBossBars.put(this, properties);
        player.getBossBar().create(
                uniqueId,
                manager.getCache().get(properties.textProperty.get()),
                parseProgress(player, properties.progressProperty.get())/100,
                parseColor(player, properties.colorProperty.get()),
                parseStyle(player, properties.styleProperty.get())
        );
        players.add(player);
    }

    @Override
    public void removePlayer(@NonNull me.neznamy.tab.api.TabPlayer p) {
        TabPlayer player = (TabPlayer) p;
        if (!player.bossbarData.visibleBossBars.containsKey(this)) return;
        players.remove(player);
        player.bossbarData.visibleBossBars.remove(this);
        player.getBossBar().remove(uniqueId);
    }

    @Override
    @NotNull
    public List<me.neznamy.tab.api.TabPlayer> getPlayers() {
        return new ArrayList<>(players);
    }

    @Override
    public boolean containsPlayer(@NonNull me.neznamy.tab.api.TabPlayer player) {
        return ((TabPlayer)player).bossbarData.visibleBossBars.containsKey(this);
    }

    private class TextRefresher extends RefreshableFeature implements CustomThreaded {

        @Override
        public void refresh(@NotNull TabPlayer refreshed, boolean force) {
            if (!refreshed.bossbarData.visibleBossBars.containsKey(BossBarLine.this)) return;
            refreshed.getBossBar().update(uniqueId, manager.getCache().get(refreshed.bossbarData.visibleBossBars.get(BossBarLine.this).textProperty.updateAndGet()));
        }

        @Override
        @NotNull
        public ThreadExecutor getCustomThread() {
            return manager.getCustomThread();
        }

        @NotNull
        @Override
        public String getFeatureName() {
            return "BossBar";
        }

        @NotNull
        @Override
        public String getRefreshDisplayName() {
            return "Updating text";
        }
    }

    private class ProgressRefresher extends RefreshableFeature implements CustomThreaded {

        @Override
        public void refresh(@NotNull TabPlayer refreshed, boolean force) {
            if (!refreshed.bossbarData.visibleBossBars.containsKey(BossBarLine.this)) return;
            refreshed.getBossBar().update(uniqueId, parseProgress(refreshed, refreshed.bossbarData.visibleBossBars.get(BossBarLine.this).progressProperty.updateAndGet())/100);
        }

        @Override
        @NotNull
        public ThreadExecutor getCustomThread() {
            return manager.getCustomThread();
        }

        @NotNull
        @Override
        public String getFeatureName() {
            return "BossBar";
        }

        @NotNull
        @Override
        public String getRefreshDisplayName() {
            return "Updating progress";
        }
    }

    private class ColorRefresher extends RefreshableFeature implements CustomThreaded {

        @Override
        public void refresh(@NotNull TabPlayer refreshed, boolean force) {
            if (!refreshed.bossbarData.visibleBossBars.containsKey(BossBarLine.this)) return;
            refreshed.getBossBar().update(uniqueId, parseColor(refreshed, refreshed.bossbarData.visibleBossBars.get(BossBarLine.this).colorProperty.updateAndGet()));
        }

        @Override
        @NotNull
        public ThreadExecutor getCustomThread() {
            return manager.getCustomThread();
        }

        @NotNull
        @Override
        public String getFeatureName() {
            return "BossBar";
        }

        @NotNull
        @Override
        public String getRefreshDisplayName() {
            return "Updating color";
        }
    }

    private class StyleRefresher extends RefreshableFeature implements CustomThreaded {

        @Override
        public void refresh(@NotNull TabPlayer refreshed, boolean force) {
            if (!refreshed.bossbarData.visibleBossBars.containsKey(BossBarLine.this)) return;
            refreshed.getBossBar().update(uniqueId, parseStyle(refreshed, refreshed.bossbarData.visibleBossBars.get(BossBarLine.this).styleProperty.updateAndGet()));
        }

        @Override
        @NotNull
        public ThreadExecutor getCustomThread() {
            return manager.getCustomThread();
        }

        @NotNull
        @Override
        public String getFeatureName() {
            return "BossBar";
        }

        @NotNull
        @Override
        public String getRefreshDisplayName() {
            return "Updating style";
        }
    }
}