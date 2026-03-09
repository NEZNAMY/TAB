package me.neznamy.tab.shared.features.playerlistobjective;

import lombok.Getter;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.DumpUtils;
import me.neznamy.tab.shared.util.OnlinePlayers;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Feature handler for scoreboard objective with PLAYER_LIST display slot (in tablist).
 */
@Getter
public class YellowNumber extends RefreshableFeature implements JoinListener, QuitListener, Loadable,
        CustomThreaded, ProxyFeature, VanishListener, Dumpable {

    /** Objective name used by this feature */
    public static final String OBJECTIVE_NAME = "TAB-PlayerList";

    private final StringToComponentCache cache = new StringToComponentCache("Playerlist Objective", 1000);

    private final ThreadExecutor customThread = new ThreadExecutor("TAB Playerlist Objective Thread");

    private OnlinePlayers onlinePlayers;

    private final PlayerListObjectiveConfiguration configuration;

    private final PlayerListObjectiveTitleRefresher titleRefresher = new PlayerListObjectiveTitleRefresher(this);
    private final DisableChecker disableChecker;

    @Nullable
    private final ProxySupport proxy = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PROXY_SUPPORT);

    /**
     * Constructs new instance and registers disable condition checker and title refresher to feature manager.
     *
     * @param   configuration
     *          Feature configuration
     */
    public YellowNumber(@NotNull PlayerListObjectiveConfiguration configuration) {
        this.configuration = configuration;
        disableChecker = new DisableChecker(this, TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(configuration.getDisableCondition()), this::onDisableConditionChange, p -> p.playerlistObjectiveData.disabled);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.YELLOW_NUMBER + "-Condition", disableChecker);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.YELLOW_NUMBER_TEXT, titleRefresher);
        if (proxy != null) {
            proxy.registerMessage(PlayerListObjectiveProxyPlayerData.class, in -> new PlayerListObjectiveProxyPlayerData(this, in));
        }
    }

    @Override
    public void load() {
        onlinePlayers = new OnlinePlayers(TAB.getInstance().getOnlinePlayers());
        Map<TabPlayer, Integer> values = new HashMap<>();
        for (TabPlayer loaded : onlinePlayers.getPlayers()) {
            loadProperties(loaded);
            if (disableChecker.isDisableConditionMet(loaded)) {
                loaded.playerlistObjectiveData.disabled.set(true);
            } else {
                register(loaded);
            }
            values.put(loaded, getValue(loaded));
            sendProxyMessage(loaded.getUniqueId(), values.get(loaded), loaded.playerlistObjectiveData.fancyValue.get());
        }
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            for (Map.Entry<TabPlayer, Integer> entry : values.entrySet()) {
                setScore(viewer, entry.getKey(), entry.getValue(), entry.getKey().playerlistObjectiveData.fancyValue.getFormat(viewer));
            }
        }
    }

    private void loadProperties(@NotNull TabPlayer player) {
        player.playerlistObjectiveData.value = new Property(this, player, configuration.getValue());
        player.playerlistObjectiveData.fancyValue = new Property(this, player, configuration.getFancyValue());
        player.playerlistObjectiveData.title = new Property(titleRefresher, player, configuration.getTitle());
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        onlinePlayers.addPlayer(connectedPlayer);
        loadProperties(connectedPlayer);
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            connectedPlayer.playerlistObjectiveData.disabled.set(true);
        } else {
            register(connectedPlayer);
        }
        int value = getValue(connectedPlayer);
        Property fancyValue = connectedPlayer.playerlistObjectiveData.fancyValue;
        fancyValue.update();
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            setScore(all, connectedPlayer, value, fancyValue.getFormat(all));
            if (all != connectedPlayer) {
                setScore(connectedPlayer, all, getValue(all), all.playerlistObjectiveData.fancyValue.getFormat(connectedPlayer));
            }
        }
        if (proxy != null) {
            sendProxyMessage(connectedPlayer);
            if (connectedPlayer.playerlistObjectiveData.disabled.get()) return;
            for (ProxyPlayer proxyPlayer : proxy.getProxyPlayers().values()) {
                if (proxyPlayer.getPlayerlist() == null) continue; // This proxy player is not loaded yet
                connectedPlayer.getScoreboard().setScore(
                        OBJECTIVE_NAME,
                        proxyPlayer.getNickname(),
                        proxyPlayer.getPlayerlist().getValue(),
                        null, // Unused by this objective slot
                        cache.get(proxyPlayer.getPlayerlist().getFancyValue())
                );
            }
        }
    }

    /**
     * Processes disable condition change.
     *
     * @param   p
     *          Player who the condition has changed for
     * @param   disabledNow
     *          Whether the feature is disabled now or not
     */
    public void onDisableConditionChange(TabPlayer p, boolean disabledNow) {
        if (disabledNow) {
            p.getScoreboard().unregisterObjective(OBJECTIVE_NAME);
        } else {
            register(p);
            for (TabPlayer all : onlinePlayers.getPlayers()) {
                setScore(p, all, getValue(all), all.playerlistObjectiveData.fancyValue.getFormat(p));
            }
            if (proxy != null) {
                sendProxyMessage(p);
                for (ProxyPlayer proxyPlayer : proxy.getProxyPlayers().values()) {
                    if (proxyPlayer.getPlayerlist() == null) continue; // This proxy player is not loaded yet
                    p.getScoreboard().setScore(
                            OBJECTIVE_NAME,
                            proxyPlayer.getNickname(),
                            proxyPlayer.getPlayerlist().getValue(),
                            null, // Unused by this objective slot
                            cache.get(proxyPlayer.getPlayerlist().getFancyValue())
                    );
                }
            }
        }
    }

    /**
     * Returns current value for specified player parsed to int.
     *
     * @param   p
     *          Player to get value of
     * @return  Current value for player
     */
    public int getValue(@NotNull TabPlayer p) {
        String string = p.playerlistObjectiveData.value.updateAndGet();
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            // Not an integer (float or invalid)
            try {
                int value = (int) Math.round(Double.parseDouble(string));
                // Float
                TAB.getInstance().getConfigHelper().runtime().floatInPlayerlistObjective(p, configuration.getValue(), string);
                return value;
            } catch (NumberFormatException e2) {
                // Not a float (invalid)
                TAB.getInstance().getConfigHelper().runtime().invalidNumberForPlayerlistObjective(p, configuration.getValue(), string);
                return 0;
            }
        }
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating value / fancy value";
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (refreshed.playerlistObjectiveData.value == null) return; // Player not loaded yet (refresh called before onJoin)
        int value = getValue(refreshed);
        Property fancyValue = refreshed.playerlistObjectiveData.fancyValue;
        fancyValue.update();
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            setScore(viewer, refreshed, value, fancyValue.getFormat(viewer));
        }
        sendProxyMessage(refreshed.getUniqueId(), value, fancyValue.get());
    }

    private void register(@NotNull TabPlayer player) {
        player.getScoreboard().registerObjective(
                OBJECTIVE_NAME,
                cache.get(player.playerlistObjectiveData.title.updateAndGet()),
                configuration.getHealthDisplay(),
                TabComponent.empty()
        );
        player.getScoreboard().setDisplaySlot(OBJECTIVE_NAME, Scoreboard.DisplaySlot.PLAYER_LIST);
    }

    /**
     * Updates score of specified entry to player.
     *
     * @param   viewer
     *          Player to send update to
     * @param   scoreHolder
     *          Owner of the score
     * @param   value
     *          Numeric value of the score
     * @param   fancyValue
     *          NumberFormat display of the score
     */
    public void setScore(@NotNull TabPlayer viewer, @NotNull TabPlayer scoreHolder, int value, @NotNull String fancyValue) {
        if (viewer.playerlistObjectiveData.disabled.get()) return;
        if (viewer.canSee(scoreHolder)) {
            viewer.getScoreboard().setScore(
                    OBJECTIVE_NAME,
                    scoreHolder.getNickname(),
                    value,
                    null, // Unused by this objective slot
                    cache.get(fancyValue)
            );
        }
    }

    /**
     * Processes nickname change of player by updating score with player's new nickname.
     *
     * @param   player
     *          Player to process nickname change of
     */
    public void processNicknameChange(@NotNull TabPlayer player) {
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            int value = getValue(player);
            for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                setScore(viewer, player, value, player.playerlistObjectiveData.fancyValue.get());
            }
        }, getFeatureName(), TabConstants.CpuUsageCategory.NICKNAME_CHANGE_PROCESS));
    }

    @Override
    public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
        onlinePlayers.removePlayer(disconnectedPlayer);
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "Playerlist Objective";
    }

    @Override
    public void onVanishStatusChange(@NotNull TabPlayer player) {
        if (player.isVanished()) return;
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            setScore(viewer, player, getValue(player), player.playerlistObjectiveData.fancyValue.getFormat(viewer));
        }
    }

    @Override
    @NotNull
    public Object dump(@NotNull TabPlayer analyzed) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("configuration", configuration.getSection().getMap());
        List<String> header = Arrays.asList("Player", "value", "fancy-value", "title", "Disabled with condition");
        List<List<String>> players = Arrays.stream(onlinePlayers.getPlayers()).map(p -> Arrays.asList(
                p.getName(),
                p.playerlistObjectiveData.value.get(),
                p.playerlistObjectiveData.fancyValue.get(),
                p.playerlistObjectiveData.title.get(),
                String.valueOf(p.playerlistObjectiveData.disabled.get())
        )).collect(Collectors.toList());
        if (proxy != null) {
            players.addAll(proxy.getProxyPlayers().values().stream().map(p -> Arrays.asList(
                    "[Proxy] " + p.getName(),
                    p.getPlayerlist() == null ? "null" : String.valueOf(p.getPlayerlist().getValue()),
                    p.getPlayerlist() == null ? "null" : p.getPlayerlist().getFancyValue(),
                    "N/A",
                    "N/A"
            )).collect(Collectors.toList()));
        }
        map.put("current values for all players (without applying relational placeholders)", DumpUtils.tableToLines(header, players));
        return map;
    }

    // ------------------
    // ProxySupport
    // ------------------

    @Override
    public void onJoin(@NotNull ProxyPlayer player) {
        updatePlayer(player);
    }

    /**
     * Updates playerlist objective data of the specified player to all connected players.
     *
     * @param   player
     *          Player to update
     */
    public void updatePlayer(@NotNull ProxyPlayer player) {
        if (player.getPlayerlist() == null) return; // Player not loaded yet
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            if (viewer.playerlistObjectiveData.disabled.get()) continue;
            viewer.getScoreboard().setScore(
                    OBJECTIVE_NAME,
                    player.getNickname(),
                    player.getPlayerlist().getValue(),
                    null, // Unused by this objective slot
                    cache.get(player.getPlayerlist().getFancyValue())
            );
        }
    }

    private void sendProxyMessage(@NotNull TabPlayer player) {
        if (proxy == null) return;
        sendProxyMessage(
                player.getUniqueId(),
                getValue(player),
                player.playerlistObjectiveData.fancyValue.get()
        );
    }

    private void sendProxyMessage(@NotNull UUID uniqueId, int value, @NotNull String fancyValue) {
        if (proxy == null) return;
        proxy.sendMessage(new PlayerListObjectiveProxyPlayerData(this, proxy.getIdCounter().incrementAndGet(), uniqueId, value, fancyValue));
    }

    @Override
    public void onProxyLoadRequest() {
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            sendProxyMessage(all);
        }
    }
}
