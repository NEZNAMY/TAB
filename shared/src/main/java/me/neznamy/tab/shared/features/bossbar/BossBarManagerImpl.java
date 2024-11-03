package me.neznamy.tab.shared.features.bossbar;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.bossbar.BossBarConfiguration.BossBarDefinition;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Class for handling BossBar feature
 */
public class BossBarManagerImpl extends RefreshableFeature implements BossBarManager, JoinListener, CommandListener, Loadable,
        QuitListener, CustomThreaded {

    @Getter private final StringToComponentCache cache = new StringToComponentCache("BossBar", 1000);
    @Getter private final ThreadExecutor customThread = new ThreadExecutor("TAB BossBar Thread");

    //default BossBars
    private final List<String> defaultBars = new ArrayList<>();

    //registered BossBars
    @Getter private final Map<String, BossBar> registeredBossBars = new LinkedHashMap<>();
    protected BossBar[] lineValues;

    //config options
    @Getter private final BossBarConfiguration configuration;
    private final String toggleOnMessage = TAB.getInstance().getConfiguration().getMessages().getBossBarOn();
    private final String toggleOffMessage = TAB.getInstance().getConfiguration().getMessages().getBossBarOff();

    //list of currently running BossBar announcements
    @Getter private final List<BossBar> announcedBossBars = new ArrayList<>();

    //players with toggled BossBar
    private final List<String> bossBarOffPlayers;

    //time when BossBar announce ends, used for placeholder
    private long announceEndTime;

    /**
     * Constructs new instance.
     *
     * @param   configuration
     *          Feature configuration
     */
    public BossBarManagerImpl(@NonNull BossBarConfiguration configuration) {
        this.configuration = configuration;
        bossBarOffPlayers = configuration.isRememberToggleChoice() ? TAB.getInstance().getConfiguration().getPlayerDataFile()
                .getStringList("bossbar-off", new ArrayList<>()) : Collections.emptyList();
        for (Map.Entry<String, BossBarDefinition> entry : configuration.getBars().entrySet()) {
            String name = entry.getKey();
            registeredBossBars.put(name, new BossBarLine(this, name, entry.getValue()));
            if (!entry.getValue().isAnnouncementOnly()) defaultBars.add(name);
        }
        lineValues = registeredBossBars.values().toArray(new BossBar[0]);
    }

    @Override
    public void load() {
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(TabConstants.Placeholder.COUNTDOWN, 100, () -> {
            long seconds = TimeUnit.MILLISECONDS.toSeconds(announceEndTime - System.currentTimeMillis());
            if (seconds < 0) return "0";
            return Long.toString(seconds);
        });
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            onJoin(p);
        }
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating display conditions";
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (!hasBossBarVisible(p)) return;
        boolean conditionResultChange = false;
        for (BossBar line : lineValues) {
            if (((BossBarLine)line).isConditionMet(p) != p.bossbarData.visibleBossBars.containsKey(line))
                conditionResultChange = true;
        }
        if (conditionResultChange) {
            for (BossBar line : lineValues) {
                line.removePlayer(p); //remove all BossBars and then resend them again to keep them displayed in defined order
            }
            showBossBars(p);
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        setBossBarVisible(connectedPlayer, configuration.isHiddenByDefault() == bossBarOffPlayers.contains(connectedPlayer.getName()), false);
    }

    @Override
    public boolean onCommand(@NotNull TabPlayer sender, @NotNull String message) {
        if (message.equals(configuration.getToggleCommand())) {
            TAB.getInstance().getCommand().execute(sender, new String[] {"bossbar"});
            return true;
        }
        return false;
    }

    @Override
    @NotNull
    public String getCommand() {
        return configuration.getToggleCommand();
    }

    /**
     * Clears and resends all BossBars to specified player
     *
     * @param   p
     *          player to process
     */
    protected void detectBossBarsAndSend(@NonNull TabPlayer p) {
        if (!hasBossBarVisible(p)) return;
        showBossBars(p);
    }

    /**
     * Shows all boss bars the player should see and does not see already.
     *
     * @param   p
     *          Player to show boss bars to
     */
    private void showBossBars(@NonNull TabPlayer p) {
        for (String defaultBar : defaultBars) {
            BossBarLine bar = (BossBarLine) registeredBossBars.get(defaultBar);
            if (bar.isConditionMet(p) && !p.bossbarData.visibleBossBars.containsKey(bar)) {
                bar.addPlayer(p);
            }
        }
        for (BossBar announced : announcedBossBars) {
            BossBarLine bar = (BossBarLine) announced;
            if (bar.isConditionMet(p) && !p.bossbarData.visibleBossBars.containsKey(bar)) {
                bar.addPlayer(p);
            }
        }
    }

    @Override
    public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
        for (BossBar line : lineValues) {
            ((BossBarLine)line).removePlayerRaw(disconnectedPlayer);
        }
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "BossBar";
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    @NotNull
    public BossBar createBossBar(@NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style) {
        ensureActive();
        return createBossBar(title, String.valueOf(progress), color.toString(), style.toString());
    }

    @Override
    @NotNull
    public BossBar createBossBar(@NonNull String title, @NonNull String progress, @NonNull String color, @NonNull String style) {
        ensureActive();
        UUID id = UUID.randomUUID();
        BossBar bar = new BossBarLine(this, id.toString(), new BossBarDefinition(style, color, progress, title, true, null));
        registeredBossBars.put(id.toString(), bar);
        lineValues = registeredBossBars.values().toArray(new BossBar[0]);
        return bar;
    }

    @Override
    public BossBar getBossBar(@NonNull String name) {
        ensureActive();
        return registeredBossBars.get(name);
    }

    @Override
    public void toggleBossBar(@NonNull me.neznamy.tab.api.TabPlayer player, boolean sendToggleMessage) {
        ensureActive();
        setBossBarVisible(player, !hasBossBarVisible(player), sendToggleMessage);
    }

    @Override
    public boolean hasBossBarVisible(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        return ((TabPlayer)player).bossbarData.visible;
    }

    @Override
    public void setBossBarVisible(@NonNull me.neznamy.tab.api.TabPlayer p, boolean visible, boolean sendToggleMessage) {
        ensureActive();
        TabPlayer player = (TabPlayer) p;
        if (player.bossbarData.visible == visible) return;
        if (visible) {
            player.bossbarData.visible = true;
            detectBossBarsAndSend(player);
            if (sendToggleMessage) player.sendMessage(toggleOnMessage, true);
            if (configuration.isRememberToggleChoice()) {
                if (configuration.isHiddenByDefault()) {
                    if (!bossBarOffPlayers.contains(player.getName())) {
                        bossBarOffPlayers.add(player.getName());
                        savePlayers();
                    }
                } else {
                    if (bossBarOffPlayers.remove(player.getName())) {
                        savePlayers();
                    }
                }
            }
        } else {
            player.bossbarData.visible = false;
            for (BossBar l : lineValues) {
                l.removePlayer(player);
            }
            if (sendToggleMessage) player.sendMessage(toggleOffMessage, true);
            if (configuration.isRememberToggleChoice()) {
                if (configuration.isHiddenByDefault()) {
                    if (bossBarOffPlayers.remove(player.getName())) {
                        savePlayers();
                    }
                } else {
                    if (!bossBarOffPlayers.contains(player.getName())) {
                        bossBarOffPlayers.add(player.getName());
                        savePlayers();
                    }
                }
            }
        }
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setBossBarVisible(player, visible);
    }

    private void savePlayers() {
        TAB.getInstance().getConfiguration().getPlayerDataFile().set("bossbar-off", new ArrayList<>(bossBarOffPlayers));
    }

    @Override
    public void sendBossBarTemporarily(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull String bossBar, int duration) {
        ensureActive();
        BossBar line = registeredBossBars.get(bossBar);
        if (line == null) throw new IllegalArgumentException("No registered BossBar found with name " + bossBar);
        if (!hasBossBarVisible(player)) return;
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> line.addPlayer(player), getFeatureName(), "Adding temporary BossBar"));
        customThread.executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            if (((TabPlayer)player).isOnline()) line.removePlayer(player);
        }, getFeatureName(), "Removing temporary BossBar"), duration*1000);
    }

    @Override
    public void announceBossBar(@NonNull String bossBar, int duration) {
        ensureActive();
        BossBar line = registeredBossBars.get(bossBar);
        if (line == null) throw new IllegalArgumentException("No registered BossBar found with name " + bossBar);
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            List<TabPlayer> players = Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(this::hasBossBarVisible).collect(Collectors.toList());
            announcedBossBars.add(line);
            announceEndTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(duration);
            for (TabPlayer all : players) {
                if (((BossBarLine)line).isConditionMet(all)) line.addPlayer(all);
            }
        }, getFeatureName(), "Adding announced BossBar"));
        customThread.executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            List<TabPlayer> players = Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(this::hasBossBarVisible).collect(Collectors.toList());
            for (TabPlayer all : players) {
                line.removePlayer(all);
            }
            announcedBossBars.remove(line);
        }, getFeatureName(), "Removing announced BossBar"), duration*1000);
    }

    /**
     * Class storing bossbar data for players.
     */
    public static class PlayerData {

        /** Whether player wishes to see boss bars or not */
        public boolean visible;

        /** Boss bars this player can currently see */
        public Map<BossBarLine, BossBarProperties> visibleBossBars = new IdentityHashMap<>();
    }

    /**
     * Class storing properties of a bossbar for player.
     */
    @RequiredArgsConstructor
    public static class BossBarProperties {

        /** Property holding BossBar title */
        @NonNull public final Property textProperty;

        /** Property holding BossBar progress */
        @NonNull public final Property progressProperty;

        /** Property holding BossBar color */
        @NonNull public final Property colorProperty;

        /** Property holding BossBar style */
        @NonNull public final Property styleProperty;
    }
}