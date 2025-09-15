package me.neznamy.tab.shared.features.bossbar;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.ToggleManager;
import me.neznamy.tab.shared.features.bossbar.BossBarConfiguration.BossBarDefinition;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class for handling BossBar feature
 */
public class BossBarManagerImpl extends RefreshableFeature implements BossBarManager, JoinListener, CommandListener, Loadable,
        QuitListener, CustomThreaded {

    @Getter private final StringToComponentCache cache = new StringToComponentCache("BossBar", 1000);
    @Getter private final ThreadExecutor customThread = new ThreadExecutor("TAB BossBar Thread");

    //registered BossBars
    private final Map<String, BossBarLine> registeredBossBars = new LinkedHashMap<>();
    protected BossBarLine[] lineValues;

    //config options
    @Getter private final BossBarConfiguration configuration;
    private final String toggleOnMessage = TAB.getInstance().getConfiguration().getMessages().getBossBarOn();
    private final String toggleOffMessage = TAB.getInstance().getConfiguration().getMessages().getBossBarOff();

    /** Manager for toggled players if remembering is enabled in config */
    @Nullable
    private ToggleManager toggleManager;

    /**
     * Constructs new instance.
     *
     * @param   configuration
     *          Feature configuration
     */
    public BossBarManagerImpl(@NonNull BossBarConfiguration configuration) {
        this.configuration = configuration;
        if (configuration.isRememberToggleChoice()) {
            toggleManager = new ToggleManager(TAB.getInstance().getConfiguration().getPlayerData(), "bossbar-off");
        }
        for (Map.Entry<String, BossBarDefinition> entry : configuration.getBars().entrySet()) {
            String name = entry.getKey();
            registeredBossBars.put(name, new BossBarLine(this, name, entry.getValue()));
        }
        lineValues = registeredBossBars.values().toArray(new BossBarLine[0]);
    }

    @Override
    public void load() {
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
        for (BossBarLine line : lineValues) {
            if (line.isConditionMet(p) != p.bossbarData.visibleBossBars.containsKey(line))
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
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setBossBarVisible(connectedPlayer, false);
        if (toggleManager != null) toggleManager.convert(connectedPlayer);
        setBossBarVisible(connectedPlayer, configuration.isHiddenByDefault() == (toggleManager != null && toggleManager.contains(connectedPlayer)), false);
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
        for (BossBarLine bossbar : lineValues) {
            if (bossbar.isConditionMet(p)) {
                if (!bossbar.isAnnouncementBar() || bossbar.isBeingAnnounced()) {
                    bossbar.addPlayer(p);
                }
            }
        }
    }

    @Override
    public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
        for (BossBarLine line : lineValues) {
            line.removePlayerRaw(disconnectedPlayer);
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
        BossBarLine bar = new BossBarLine(this, id.toString(), new BossBarDefinition(style, color, progress, title, true, null));
        registeredBossBars.put(id.toString(), bar);
        lineValues = registeredBossBars.values().toArray(new BossBarLine[0]);
        return bar;
    }

    @Override
    public BossBar getBossBar(@NonNull String name) {
        ensureActive();
        return registeredBossBars.get(name);
    }

    @Override
    @NotNull
    public Map<String, BossBar> getRegisteredBossBars() {
        return Collections.unmodifiableMap(registeredBossBars);
    }

    @Override
    public void removeBossBar(@NonNull String name) {
        ensureActive();
        BossBar bar = registeredBossBars.remove(name);
        if (bar == null) throw new IllegalArgumentException("No registered BossBar found with name " + name);
        lineValues = registeredBossBars.values().toArray(new BossBarLine[0]);
        for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
            bar.removePlayer(player);
            player.bossbarData.visibleBossBars.remove(bar);
        }
    }

    @Override
    public void removeBossBar(@NonNull BossBar bossBar) {
        ensureActive();
        BossBarLine bar = (BossBarLine) bossBar;
        if (!registeredBossBars.remove(bar.getName(), bar)) {
            throw new IllegalArgumentException("This bossbar (" + bar.getName() + ") is not registered.");
        }
        lineValues = registeredBossBars.values().toArray(new BossBarLine[0]);
        for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
            bar.removePlayer(player);
            player.bossbarData.visibleBossBars.remove(bar);
        }
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
            if (sendToggleMessage) player.sendMessage(toggleOnMessage);
            if (toggleManager != null) {
                if (configuration.isHiddenByDefault()) {
                    toggleManager.add(player);
                } else {
                    toggleManager.remove(player);
                }
            }
        } else {
            player.bossbarData.visible = false;
            for (BossBar l : lineValues) {
                l.removePlayer(player);
            }
            if (sendToggleMessage) player.sendMessage(toggleOffMessage);
            if (toggleManager != null) {
                if (configuration.isHiddenByDefault()) {
                    toggleManager.remove(player);
                } else {
                    toggleManager.add(player);
                }
            }
        }
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setBossBarVisible(player, visible);
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
        BossBarLine line = registeredBossBars.get(bossBar);
        if (line == null) throw new IllegalArgumentException("No registered BossBar found with name " + bossBar);
        if (!line.isAnnouncementBar()) throw new IllegalArgumentException("BossBar " + bossBar + " is not an announcement bar");
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> line.announce(duration), getFeatureName(), "Adding announced BossBar"));
        customThread.executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), line::unAnnounce, getFeatureName(), "Removing announced BossBar"), duration*1000);
    }

    @Override
    @NotNull
    public List<BossBar> getAnnouncedBossBars() {
        return registeredBossBars.values().stream().filter(BossBarLine::isBeingAnnounced).collect(Collectors.toList());
    }


}