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
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.OnlinePlayers;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Feature handler for scoreboard objective with
 * PLAYER_LIST display slot (in tablist).
 */
@Getter
public class YellowNumber extends RefreshableFeature implements JoinListener, QuitListener, Loadable,
        CustomThreaded, ProxyFeature {

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
     * Constructs new instance and registers disable condition checker to feature manager.
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
            values.put(loaded, getValueNumber(loaded));
            sendProxyMessage(loaded.getUniqueId(), values.get(loaded), loaded.playerlistObjectiveData.valueModern.get());
        }
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            for (Map.Entry<TabPlayer, Integer> entry : values.entrySet()) {
                setScore(viewer, entry.getKey(), entry.getValue(), entry.getKey().playerlistObjectiveData.valueModern.getFormat(viewer));
            }
        }
    }

    private void loadProperties(@NotNull TabPlayer player) {
        player.playerlistObjectiveData.valueLegacy = new Property(this, player, configuration.getValue());
        player.playerlistObjectiveData.valueModern = new Property(this, player, configuration.getFancyValue());
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
        int value = getValueNumber(connectedPlayer);
        Property valueFancy = connectedPlayer.playerlistObjectiveData.valueModern;
        valueFancy.update();
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            setScore(all, connectedPlayer, value, valueFancy.getFormat(connectedPlayer));
            if (all != connectedPlayer) {
                setScore(connectedPlayer, all, getValueNumber(all), all.playerlistObjectiveData.valueModern.getFormat(connectedPlayer));
            }
        }
        if (proxy != null) {
            sendProxyMessage(connectedPlayer);
            if (connectedPlayer.playerlistObjectiveData.disabled.get()) return;
            for (ProxyPlayer proxied : proxy.getProxyPlayers().values()) {
                if (proxied.getPlayerlist() == null) continue; // This proxy player is not loaded yet
                connectedPlayer.getScoreboard().setScore(
                        OBJECTIVE_NAME,
                        proxied.getNickname(),
                        proxied.getPlayerlist().getValue(),
                        null, // Unused by this objective slot
                        cache.get(proxied.getPlayerlist().getFancyValue())
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
                setScore(p, all, getValueNumber(all), all.playerlistObjectiveData.valueModern.getFormat(p));
            }
            if (proxy != null) {
                sendProxyMessage(p);
                for (ProxyPlayer proxied : proxy.getProxyPlayers().values()) {
                    if (proxied.getPlayerlist() == null) continue; // This proxy player is not loaded yet
                    p.getScoreboard().setScore(
                            OBJECTIVE_NAME,
                            proxied.getNickname(),
                            proxied.getPlayerlist().getValue(),
                            null, // Unused by this objective slot
                            cache.get(proxied.getPlayerlist().getFancyValue())
                    );
                }
            }
        }
    }

    /**
     * Returns current value for specified player parsed to int
     *
     * @param   p
     *          Player to get value of
     * @return  Current value of player
     */
    public int getValueNumber(@NotNull TabPlayer p) {
        String string = p.playerlistObjectiveData.valueLegacy.updateAndGet();
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
        return "Updating value";
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (refreshed.playerlistObjectiveData.valueLegacy == null) return; // Player not loaded yet (refresh called before onJoin)
        int value = getValueNumber(refreshed);
        refreshed.playerlistObjectiveData.valueModern.update();
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            setScore(viewer, refreshed, value, refreshed.playerlistObjectiveData.valueModern.getFormat(viewer));
        }
        sendProxyMessage(refreshed.getUniqueId(), value, refreshed.playerlistObjectiveData.valueModern.get());
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
        viewer.getScoreboard().setScore(
                OBJECTIVE_NAME,
                scoreHolder.getNickname(),
                value,
                null, // Unused by this objective slot
                cache.get(fancyValue)
        );
    }

    /**
     * Processes nickname change of player by updating score with player's new nickname.
     *
     * @param   player
     *          Player to process nickname change of
     */
    public void processNicknameChange(@NotNull TabPlayer player) {
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            int value = getValueNumber(player);
            for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                setScore(viewer, player, value, player.playerlistObjectiveData.valueModern.get());
            }
        }, getFeatureName(), TabConstants.CpuUsageCategory.NICKNAME_CHANGE_PROCESS));
    }

    @Override
    public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
        onlinePlayers.removePlayer(disconnectedPlayer);
    }

    // ------------------
    // ProxySupport
    // ------------------

    private void sendProxyMessage(@NotNull TabPlayer player) {
        if (proxy == null) return;
        sendProxyMessage(
                player.getUniqueId(),
                getValueNumber(player),
                player.playerlistObjectiveData.valueModern.get()
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

    @Override
    public void onJoin(@NotNull ProxyPlayer player) {
        updatePlayer(player);
    }

    /**
     * Updates playerlist objective data of the specified player to all other players.
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

    @NotNull
    @Override
    public String getFeatureName() {
        return "Playerlist Objective";
    }
}