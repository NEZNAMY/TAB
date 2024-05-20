package me.neznamy.tab.shared.features.bossbar;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Class for handling BossBar feature
 */
public class BossBarManagerImpl extends RefreshableFeature implements BossBarManager, JoinListener, CommandListener, Loadable,
        UnLoadable, LoginPacketListener, QuitListener {

    //default BossBars
    private final List<String> defaultBars = new ArrayList<>();

    //registered BossBars
    @Getter private final Map<String, BossBar> registeredBossBars = new HashMap<>();
    protected BossBar[] lineValues;

    //config options
    @Getter private final String command = config().getString("bossbar.toggle-command", "/bossbar");
    private final boolean hiddenByDefault = config().getBoolean("bossbar.hidden-by-default", false);
    private final boolean rememberToggleChoice = config().getBoolean("bossbar.remember-toggle-choice", false);
    private final String toggleOnMessage = TAB.getInstance().getConfiguration().getMessages().getBossBarOn();
    private final String toggleOffMessage = TAB.getInstance().getConfiguration().getMessages().getBossBarOff();

    //list of currently running BossBar announcements
    @Getter private final List<BossBar> announcedBossBars = new ArrayList<>();

    //players with toggled BossBar
    private final List<String> bossBarOffPlayers = rememberToggleChoice ? TAB.getInstance().getConfiguration().getPlayerDataFile()
            .getStringList("bossbar-off", new ArrayList<>()) : Collections.emptyList();

    //time when BossBar announce ends, used for placeholder
    private long announceEndTime;

    /**
     * Constructs new instance and loads configuration
     */
    public BossBarManagerImpl() {
        super("BossBar", "Updating display conditions");
        for (Object bar : config().getConfigurationSection("bossbar.bars").keySet()) {
            BossBarLine line = loadFromConfig(bar.toString());
            registeredBossBars.put(bar.toString(), line);
            if (!line.isAnnouncementBar()) defaultBars.add(bar.toString());
        }
        lineValues = registeredBossBars.values().toArray(new BossBar[0]);
    }

    /**
     * Loads BossBar from config by its name
     *
     * @param   bar
     *          name of BossBar in config
     * @return  loaded BossBar
     */
    private @NotNull BossBarLine loadFromConfig(@NonNull String bar) {
        Map<String, Object> bossBar = config().getConfigurationSection("bossbar.bars." + bar);
        TAB.getInstance().getConfigHelper().startup().checkBossBarProperties(bossBar, bar);
        return new BossBarLine(
                this,
                bar,
                (String) bossBar.get("display-condition"),
                String.valueOf(bossBar.get("color")),
                String.valueOf(bossBar.get("style")),
                String.valueOf(bossBar.get("text")),
                String.valueOf(bossBar.get("progress")),
                (boolean) bossBar.getOrDefault("announcement-bar", false)
        );
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

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (!hasBossBarVisible(p)) return;
        boolean conditionResultChange = false;
        for (BossBar line : lineValues) {
            if (((BossBarLine)line).isConditionMet(p) != line.containsPlayer(p))
                conditionResultChange = true;
        }
        if (conditionResultChange) {
            for (BossBar line : lineValues) {
                line.removePlayer(p); //remove all BossBars and then resend them again to keep them displayed in defined order
            }
            showBossBars(p, defaultBars);
            showBossBars(p, announcedBossBars.stream().map(BossBar::getName).collect(Collectors.toList()));
        }
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            for (BossBar line : lineValues) {
                line.removePlayer(p);
            }
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        setBossBarVisible(connectedPlayer, hiddenByDefault == bossBarOffPlayers.contains(connectedPlayer.getName()), false);
    }

    @Override
    public boolean onCommand(@NotNull TabPlayer sender, @NotNull String message) {
        if (message.equals(command)) {
            TAB.getInstance().getCommand().execute(sender, new String[] {"bossbar"});
            return true;
        }
        return false;
    }

    /**
     * Clears and resends all BossBars to specified player
     *
     * @param   p
     *          player to process
     */
    protected void detectBossBarsAndSend(@NonNull TabPlayer p) {
        if (!hasBossBarVisible(p)) return;
        showBossBars(p, defaultBars);
        showBossBars(p, announcedBossBars.stream().map(BossBar::getName).collect(Collectors.toList()));
    }

    /**
     * Shows BossBars to player if display condition is met
     *
     * @param   p
     *          player to show BossBars to
     * @param   bars
     *          list of BossBars to check
     */
    private void showBossBars(@NonNull TabPlayer p, @NonNull List<String> bars) {
        for (String defaultBar : bars) {
            BossBarLine bar = (BossBarLine) registeredBossBars.get(defaultBar);
            if (bar.isConditionMet(p) && !bar.containsPlayer(p)) {
                bar.addPlayer(p);
            }
        }
    }



    @Override
    public void onLoginPacket(TabPlayer player) {
        // Since 1.20.2, Login packet clears BossBars as well
        if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) {
            for (BossBar bar : lineValues) {
                if (bar.containsPlayer(player)) {
                    ((BossBarLine)bar).sendToPlayerRaw(player);
                }
            }
        }
    }

    @Override
    public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
        for (BossBar line : lineValues) {
            ((BossBarLine)line).removePlayerRaw(disconnectedPlayer);
        }
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
        BossBar bar = new BossBarLine(this, id.toString(), null, color, style, title, progress, true);
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
    public void toggleBossBar(me.neznamy.tab.api.@NonNull TabPlayer player, boolean sendToggleMessage) {
        ensureActive();
        setBossBarVisible(player, !hasBossBarVisible(player), sendToggleMessage);
    }

    @Override
    public boolean hasBossBarVisible(me.neznamy.tab.api.@NonNull TabPlayer player) {
        ensureActive();
        return ((TabPlayer)player).bossbarData.visible;
    }

    @Override
    public void setBossBarVisible(me.neznamy.tab.api.@NonNull TabPlayer p, boolean visible, boolean sendToggleMessage) {
        ensureActive();
        TabPlayer player = (TabPlayer) p;
        if (player.bossbarData.visible == visible) return;
        if (visible) {
            player.bossbarData.visible = true;
            detectBossBarsAndSend(player);
            if (sendToggleMessage) player.sendMessage(toggleOnMessage, true);
            if (rememberToggleChoice) {
                if (hiddenByDefault) {
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
            if (rememberToggleChoice) {
                if (hiddenByDefault) {
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
        synchronized (bossBarOffPlayers) {
            TAB.getInstance().getConfiguration().getPlayerDataFile().set("bossbar-off", new ArrayList<>(bossBarOffPlayers));
        }
    }

    @Override
    public void sendBossBarTemporarily(me.neznamy.tab.api.@NonNull TabPlayer player, @NonNull String bossBar, int duration) {
        ensureActive();
        if (!hasBossBarVisible(player)) return;
        BossBar line = registeredBossBars.get(bossBar);
        if (line == null) throw new IllegalArgumentException("No registered BossBar found with name " + bossBar);
        TAB.getInstance().getCPUManager().runMeasuredTask(getFeatureName(), "Adding temporary BossBar", () -> line.addPlayer(player));
        TAB.getInstance().getCPUManager().runTaskLater((int) TimeUnit.SECONDS.toMillis(duration),
                getFeatureName(), "Removing temporary BossBar", () -> line.removePlayer(player));
    }

    @Override
    public void announceBossBar(@NonNull String bossBar, int duration) {
        ensureActive();
        BossBar line = registeredBossBars.get(bossBar);
        if (line == null) throw new IllegalArgumentException("No registered BossBar found with name " + bossBar);
        List<TabPlayer> players = Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(
                this::hasBossBarVisible).collect(Collectors.toList());
        TAB.getInstance().getCPUManager().runMeasuredTask(getFeatureName(), "Adding announced BossBar", () -> {
            announcedBossBars.add(line);
            announceEndTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(duration);
            for (TabPlayer all : players) {
                if (((BossBarLine)line).isConditionMet(all)) line.addPlayer(all);
            }
        });
        TAB.getInstance().getCPUManager().runTaskLater((int) TimeUnit.SECONDS.toMillis(duration),
                getFeatureName(), "Removing announced BossBar", () -> {
                    for (TabPlayer all : players) {
                        line.removePlayer(all);
                    }
                    announcedBossBars.remove(line);
                });
    }

    /**
     * Class storing bossbar data for players.
     */
    public static class PlayerData {

        /** Whether player wishes to see boss bars or not */
        public boolean visible;
    }
}