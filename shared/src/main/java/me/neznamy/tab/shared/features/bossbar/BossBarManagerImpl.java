package me.neznamy.tab.shared.features.bossbar;

import java.util.*;
import java.util.stream.Collectors;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.features.TabExpansion;

/**
 * Class for handling BossBar feature
 */
public class BossBarManagerImpl extends TabFeature implements BossBarManager {

    //default BossBars
    private final List<String> defaultBars = new ArrayList<>();

    //registered BossBars
    private final Map<String, BossBar> lines = new HashMap<>();
    private BossBar[] lineValues;

    //config options
    private final String toggleCommand = TAB.getInstance().getConfiguration().getConfig().getString("bossbar.toggle-command", "/bossbar");
    private final boolean hiddenByDefault = TAB.getInstance().getConfiguration().getConfig().getBoolean("bossbar.hidden-by-default", false);
    private final boolean rememberToggleChoice = TAB.getInstance().getConfiguration().getConfig().getBoolean("bossbar.remember-toggle-choice", false);
    private final String toggleOnMessage = TAB.getInstance().getConfiguration().getMessages().getBossBarOn();
    private final String toggleOffMessage = TAB.getInstance().getConfiguration().getMessages().getBossBarOff();

    //list of currently running BossBar announcements
    private final List<BossBar> announcements = new ArrayList<>();

    //players with toggled BossBar
    private final List<String> bossBarOffPlayers = rememberToggleChoice ? TAB.getInstance().getConfiguration().getPlayerDataFile().getStringList("bossbar-off", new ArrayList<>()) : Collections.emptyList();

    //time when BossBar announce ends, used for placeholder
    private long announceEndTime;

    private final Set<TabPlayer> visiblePlayers = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * Constructs new instance and loads configuration
     */
    public BossBarManagerImpl() {
        super("BossBar", "Processing display conditions", "bossbar");
        for (Object bar : TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("bossbar.bars").keySet()){
            BossBarLine line = loadFromConfig(bar.toString());
            lines.put(bar.toString(), line);
            if (!line.isAnnouncementBar()) defaultBars.add(bar.toString());
        }
        lineValues = lines.values().toArray(new BossBar[0]);
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(TabConstants.Placeholder.COUNTDOWN, 100, () -> (announceEndTime - System.currentTimeMillis()) / 1000);
        TAB.getInstance().debug(String.format("Loaded BossBar feature with parameters disabledWorlds=%s, disabledServers=%s, toggleCommand=%s, defaultBars=%s, hiddenByDefault=%s, remember_toggle_choice=%s",
                Arrays.toString(disabledWorlds), Arrays.toString(disabledServers), toggleCommand, defaultBars, hiddenByDefault, rememberToggleChoice));
    }

    /**
     * Loads BossBar from config by its name
     *
     * @param   bar
     *          name of BossBar in config
     * @return  loaded BossBar
     */
    private BossBarLine loadFromConfig(String bar) {
        Map<String, Object> bossBar = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("bossbar.bars." + bar);
        String condition = (String) bossBar.get("display-condition");
        String style = (String) bossBar.get("style");
        String color = (String) bossBar.get("color");
        String progress = String.valueOf(bossBar.get("progress"));
        String text = (String) bossBar.get("text");
        if (style == null) {
            TAB.getInstance().getErrorManager().missingAttribute(getFeatureName(), bar, "style");
            style = "PROGRESS";
        }
        if (color == null) {
            TAB.getInstance().getErrorManager().missingAttribute(getFeatureName(), bar, "color");
            color = "WHITE";
        }
        if (progress == null) {
            progress = "100";
            TAB.getInstance().getErrorManager().missingAttribute(getFeatureName(), bar, "progress");
        }
        if (text == null) {
            text = "";
            TAB.getInstance().getErrorManager().missingAttribute(getFeatureName(), bar, "text");
        }
        return new BossBarLine(this, bar, condition, color, style, text, progress, (boolean) bossBar.getOrDefault("announcement-bar", false));
    }

    @Override
    public void load() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            onJoin(p);
        }
    }

    @Override
    public void refresh(TabPlayer p, boolean force) {
        if (!hasBossBarVisible(p) || isDisabledPlayer(p)) return;
        for (BossBar line : lineValues) {
            line.removePlayer(p); //remove all BossBars and then resend them again to keep them displayed in defined order
        }
        showBossBars(p, defaultBars);
        showBossBars(p, announcements.stream().map(BossBar::getName).collect(Collectors.toList()));
    }

    @Override
    public void unload() {
        for (BossBar line : lineValues) {
            for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
                line.removePlayer(p);
            }
        }
    }

    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        if (isDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld())) {
            addDisabledPlayer(connectedPlayer);
        }
        setBossBarVisible(connectedPlayer, hiddenByDefault == bossBarOffPlayers.contains(connectedPlayer.getName()), false);
    }

    @Override
    public void onServerChange(TabPlayer p, String from, String to) {
        onWorldChange(p, null, null);
    }

    @Override
    public void onWorldChange(TabPlayer p, String from, String to) {
        if (isDisabled(p.getServer(), p.getWorld())) {
            addDisabledPlayer(p);
        } else {
            removeDisabledPlayer(p);
        }
        for (BossBar line : lineValues) {
            line.removePlayer(p);
        }
        detectBossBarsAndSend(p);
    }

    @Override
    public boolean onCommand(TabPlayer sender, String message) {
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
    protected void detectBossBarsAndSend(TabPlayer p) {
        if (isDisabledPlayer(p) || !hasBossBarVisible(p)) return;
        showBossBars(p, defaultBars);
        showBossBars(p, announcements.stream().map(BossBar::getName).collect(Collectors.toList()));
    }

    /**
     * Shows BossBars to player if display condition is met
     *
     * @param   p
     *          player to show BossBars to
     * @param   bars
     *          list of BossBars to check
     */
    private void showBossBars(TabPlayer p, List<String> bars) {
        for (String defaultBar : bars) {
            BossBarLine bar = (BossBarLine) lines.get(defaultBar);
            if (bar.isConditionMet(p) && !bar.containsPlayer(p)) {
                bar.addPlayer(p);
            }
        }
    }

    @Override
    public BossBar createBossBar(String title, float progress, BarColor color, BarStyle style) {
        return createBossBar(title, String.valueOf(progress), color.toString(), style.toString());
    }

    @Override
    public BossBar createBossBar(String title, String progress, String color, String style) {
        UUID id = UUID.randomUUID();
        BossBar bar = new BossBarLine(this, id.toString(), null, color, style, title, progress, true);
        lines.put(id.toString(), bar);
        lineValues = lines.values().toArray(new BossBar[0]);
        return bar;
    }

    @Override
    public BossBar getBossBar(String name) {
        return lines.get(name);
    }

    @Override
    public BossBar getBossBar(UUID id) {
        for (BossBar line : lineValues) {
            if (line.getUniqueId() == id) return line;
        }
        return null;
    }

    @Override
    public void toggleBossBar(TabPlayer player, boolean sendToggleMessage) {
        setBossBarVisible(player, !hasBossBarVisible(player), sendToggleMessage);
    }

    @Override
    public Map<String, BossBar> getRegisteredBossBars() {
        return lines;
    }

    @Override
    public boolean hasBossBarVisible(TabPlayer player) {
        return visiblePlayers.contains(player);
    }

    @Override
    public void setBossBarVisible(TabPlayer player, boolean visible, boolean sendToggleMessage) {
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
        TabExpansion expansion = TAB.getInstance().getPlaceholderManager().getTabExpansion();
        if (expansion != null) expansion.setBossBarVisible(player, visible);
    }

    @Override
    public void sendBossBarTemporarily(TabPlayer player, String bossBar, int duration) {
        if (!hasBossBarVisible(player)) return;
        BossBar line = lines.get(bossBar);
        if (line == null) throw new IllegalArgumentException("No registered BossBar found with name " + bossBar);
        TAB.getInstance().getCPUManager().runTask(() -> line.addPlayer(player));
        TAB.getInstance().getCPUManager().runTaskLater(duration*1000,
                this, "Removing temporary BossBar", () -> line.removePlayer(player));
    }

    @Override
    public void announceBossBar(String bossBar, int duration) {
        BossBar line = lines.get(bossBar);
        if (line == null) throw new IllegalArgumentException("No registered BossBar found with name " + bossBar);
        TAB.getInstance().getCPUManager().runTask(() -> {
            TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.COUNTDOWN).markAsUsed();
            announcements.add(line);
            announceEndTime = System.currentTimeMillis() + duration* 1000L;
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (!hasBossBarVisible(all)) continue;
                if (((BossBarLine)line).isConditionMet(all)) line.addPlayer(all);
            }
        });
        TAB.getInstance().getCPUManager().runTaskLater(duration*1000,
                this, "Removing announced BossBar", () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (!hasBossBarVisible(all)) continue;
                line.removePlayer(all);
            }
            announcements.remove(line);
        });
    }

    @Override
    public List<BossBar> getAnnouncedBossBars() {
        return announcements;
    }
}