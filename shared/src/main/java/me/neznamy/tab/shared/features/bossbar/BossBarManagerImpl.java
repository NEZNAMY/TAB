package me.neznamy.tab.shared.features.bossbar;

import lombok.Getter;
import lombok.NonNull;
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
import java.util.stream.Collectors;

/**
 * Class for handling BossBar feature
 */
public class BossBarManagerImpl extends TabFeature implements BossBarManager, JoinListener, CommandListener, Loadable,
        UnLoadable, Refreshable {

    //default BossBars
    private final List<String> defaultBars = new ArrayList<>();

    //registered BossBars
    @Getter private final Map<String, BossBar> registeredBossBars = new HashMap<>();
    protected BossBar[] lineValues;

    //config options
    @Getter private final String toggleCommand = TAB.getInstance().getConfiguration().getConfig().getString("bossbar.toggle-command", "/bossbar");
    private final boolean hiddenByDefault = TAB.getInstance().getConfiguration().getConfig().getBoolean("bossbar.hidden-by-default", false);
    private final boolean rememberToggleChoice = TAB.getInstance().getConfiguration().getConfig().getBoolean("bossbar.remember-toggle-choice", false);
    private final String toggleOnMessage = TAB.getInstance().getConfiguration().getMessages().getBossBarOn();
    private final String toggleOffMessage = TAB.getInstance().getConfiguration().getMessages().getBossBarOff();

    //list of currently running BossBar announcements
    @Getter private final List<BossBar> announcedBossBars = new ArrayList<>();

    //players with toggled BossBar
    private final List<String> bossBarOffPlayers = rememberToggleChoice ? TAB.getInstance().getConfiguration().getPlayerDataFile()
            .getStringList("bossbar-off", new ArrayList<>()) : Collections.emptyList();

    //time when BossBar announce ends, used for placeholder
    private long announceEndTime;

    private final Set<me.neznamy.tab.api.TabPlayer> visiblePlayers = Collections.newSetFromMap(new WeakHashMap<>());

    @Getter protected final String featureName = "BossBar";
    @Getter private final String refreshDisplayName = "Updating display conditions";

    /**
     * Constructs new instance and loads configuration
     */
    public BossBarManagerImpl() {
        for (Object bar : TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("bossbar.bars").keySet()) {
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
        Map<String, Object> bossBar = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("bossbar.bars." + bar);
        TAB.getInstance().getMisconfigurationHelper().checkBossBarProperties(bossBar, bar);
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
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(TabConstants.Placeholder.COUNTDOWN, 100, () -> (announceEndTime - System.currentTimeMillis()) / 1000);
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
        if (message.equalsIgnoreCase(toggleCommand)) {
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
            if (bar.isConditionMet(p) && !bar.getPlayers().contains(p)) {
                bar.addPlayer(p);
            }
        }
    }

    @Override
    public @NotNull BossBar createBossBar(@NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style) {
        return createBossBar(title, String.valueOf(progress), color.toString(), style.toString());
    }

    @Override
    public @NotNull BossBar createBossBar(@NonNull String title, @NonNull String progress, @NonNull String color, @NonNull String style) {
        UUID id = UUID.randomUUID();
        BossBar bar = new BossBarLine(this, id.toString(), null, color, style, title, progress, true);
        registeredBossBars.put(id.toString(), bar);
        lineValues = registeredBossBars.values().toArray(new BossBar[0]);
        return bar;
    }

    @Override
    public BossBar getBossBar(@NonNull String name) {
        return registeredBossBars.get(name);
    }

    @Override
    public void toggleBossBar(me.neznamy.tab.api.@NonNull TabPlayer player, boolean sendToggleMessage) {
        setBossBarVisible(player, !hasBossBarVisible(player), sendToggleMessage);
    }

    @Override
    public boolean hasBossBarVisible(me.neznamy.tab.api.@NonNull TabPlayer player) {
        return visiblePlayers.contains(player);
    }

    @Override
    public void setBossBarVisible(me.neznamy.tab.api.@NonNull TabPlayer p, boolean visible, boolean sendToggleMessage) {
        TabPlayer player = (TabPlayer) p;
        if (visiblePlayers.contains(player) == visible) return;
        if (visible) {
            visiblePlayers.add(player);
            detectBossBarsAndSend(player);
            if (sendToggleMessage) player.sendMessage(toggleOnMessage, true);
            if (rememberToggleChoice) {
                if (hiddenByDefault) {
                    if (!bossBarOffPlayers.contains(player.getName())) bossBarOffPlayers.add(player.getName());
                } else {
                    bossBarOffPlayers.remove(player.getName());
                }
                TAB.getInstance().getConfiguration().getPlayerDataFile().set("bossbar-off", new ArrayList<>(bossBarOffPlayers));
            }
        } else {
            visiblePlayers.remove(player);
            for (BossBar l : lineValues) {
                l.removePlayer(player);
            }
            if (sendToggleMessage) player.sendMessage(toggleOffMessage, true);
            if (rememberToggleChoice) {
                if (hiddenByDefault) {
                    bossBarOffPlayers.remove(player.getName());
                } else {
                    if (!bossBarOffPlayers.contains(player.getName())) bossBarOffPlayers.add(player.getName());
                }
                TAB.getInstance().getConfiguration().getPlayerDataFile().set("bossbar-off", new ArrayList<>(bossBarOffPlayers));
            }
        }
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setBossBarVisible(player, visible);
    }

    @Override
    public void sendBossBarTemporarily(me.neznamy.tab.api.@NonNull TabPlayer player, @NonNull String bossBar, int duration) {
        if (!hasBossBarVisible(player)) return;
        BossBar line = registeredBossBars.get(bossBar);
        if (line == null) throw new IllegalArgumentException("No registered BossBar found with name " + bossBar);
        TAB.getInstance().getCPUManager().runTask(() -> line.addPlayer(player));
        TAB.getInstance().getCPUManager().runTaskLater(duration*1000,
                featureName, "Removing temporary BossBar", () -> line.removePlayer(player));
    }

    @Override
    public void announceBossBar(@NonNull String bossBar, int duration) {
        BossBar line = registeredBossBars.get(bossBar);
        if (line == null) throw new IllegalArgumentException("No registered BossBar found with name " + bossBar);
        List<TabPlayer> players = Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(
                this::hasBossBarVisible).collect(Collectors.toList());
        TAB.getInstance().getCPUManager().runTask(() -> {
            announcedBossBars.add(line);
            announceEndTime = System.currentTimeMillis() + duration* 1000L;
            for (TabPlayer all : players) {
                if (((BossBarLine)line).isConditionMet(all)) line.addPlayer(all);
            }
        });
        TAB.getInstance().getCPUManager().runTaskLater(duration*1000,
                featureName, "Removing announced BossBar", () -> {
            for (TabPlayer all : players) {
                line.removePlayer(all);
            }
            announcedBossBars.remove(line);
        });
    }
}