package me.neznamy.tab.shared.features.belowname;

import lombok.Getter;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.types.*;
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
 * Feature handler for BelowName feature
 */
public class BelowName extends RefreshableFeature implements JoinListener, QuitListener, Loadable,
        WorldSwitchListener, ServerSwitchListener, CustomThreaded, ProxyFeature, VanishListener {

    /** Objective name used by this feature */
    public static final String OBJECTIVE_NAME = "TAB-BelowName";

    @Getter
    private final StringToComponentCache cache = new StringToComponentCache("BelowName", 1000);

    @Getter
    private final ThreadExecutor customThread = new ThreadExecutor("TAB Belowname Objective Thread");

    @Getter
    private OnlinePlayers onlinePlayers;

    private final BelowNameConfiguration configuration;

    private final BelowNameTitleRefresher textRefresher = new BelowNameTitleRefresher(this, customThread);
    private final DisableChecker disableChecker;

    @Nullable
    private final ProxySupport proxy = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PROXY_SUPPORT);

    /**
     * Constructs new instance and registers disable condition checker and text refresher to feature manager.
     *
     * @param   configuration
     *          Feature configuration
     */
    public BelowName(@NotNull BelowNameConfiguration configuration) {
        this.configuration = configuration;
        disableChecker = new DisableChecker(this, TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(configuration.getDisableCondition()), this::onDisableConditionChange, p -> p.belowNameData.disabled);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.BELOW_NAME + "-Condition", disableChecker);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.BELOW_NAME_TEXT, textRefresher);
        if (proxy != null) {
            proxy.registerMessage(BelowNameProxyPlayerData.class, in -> new BelowNameProxyPlayerData(in, this));
        }
    }

    @Override
    public void load() {
        onlinePlayers = new OnlinePlayers(TAB.getInstance().getOnlinePlayers());
        Map<TabPlayer, Integer> values = new HashMap<>();
        for (TabPlayer loaded : onlinePlayers.getPlayers()) {
            loadProperties(loaded);
            if (disableChecker.isDisableConditionMet(loaded)) {
                loaded.belowNameData.disabled.set(true);
            } else {
                register(loaded);
            }
            values.put(loaded, getValue(loaded));
            sendProxyMessage(loaded.getUniqueId(), values.get(loaded), loaded.belowNameData.numberFormat.get());
        }
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            for (Map.Entry<TabPlayer, Integer> entry : values.entrySet()) {
                TabPlayer target = entry.getKey();
                if (!sameServerAndWorld(viewer, target)) continue;
                setScore(viewer, target, entry.getValue(), target.belowNameData.numberFormat.getFormat(viewer));
            }
        }
    }

    private void loadProperties(@NotNull TabPlayer player) {
        player.belowNameData.score = new Property(this, player, configuration.getValue());
        player.belowNameData.numberFormat = new Property(this, player, configuration.getFancyValue());
        player.belowNameData.text = new Property(textRefresher, player, configuration.getTitle());
        player.belowNameData.defaultNumberFormat = new Property(textRefresher, player, configuration.getFancyValueDefault());
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        onlinePlayers.addPlayer(connectedPlayer);
        loadProperties(connectedPlayer);
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            connectedPlayer.belowNameData.disabled.set(true);
        } else {
            register(connectedPlayer);
        }
        int number = getValue(connectedPlayer);
        Property fancy = connectedPlayer.belowNameData.numberFormat;
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            if (!sameServerAndWorld(connectedPlayer, all)) continue;
            setScore(all, connectedPlayer, number, fancy.getFormat(all));
            if (all != connectedPlayer) {
                setScore(connectedPlayer, all, getValue(all), all.belowNameData.numberFormat.getFormat(connectedPlayer));
            }
        }
        if (proxy != null) {
            sendProxyMessage(connectedPlayer);
            if (connectedPlayer.belowNameData.disabled.get()) return;
            for (ProxyPlayer proxyPlayer : proxy.getProxyPlayers().values()) {
                if (proxyPlayer.getBelowname() == null) continue; // This proxy player is not loaded yet
                connectedPlayer.getScoreboard().setScore(
                        OBJECTIVE_NAME,
                        proxyPlayer.getNickname(),
                        proxyPlayer.getBelowname().getValue(),
                        null, // Unused by this objective slot
                        cache.get(proxyPlayer.getBelowname().getFancyValue())
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
                if (!sameServerAndWorld(p, all)) continue;
                setScore(p, all, getValue(all), all.belowNameData.numberFormat.getFormat(p));
            }
            if (proxy != null) {
                sendProxyMessage(p);
                for (ProxyPlayer proxyPlayer : proxy.getProxyPlayers().values()) {
                    if (proxyPlayer.getBelowname() == null) continue; // This proxy player is not loaded yet
                    p.getScoreboard().setScore(
                            OBJECTIVE_NAME,
                            proxyPlayer.getNickname(),
                            proxyPlayer.getBelowname().getValue(),
                            null, // Unused by this objective slot
                            cache.get(proxyPlayer.getBelowname().getFancyValue())
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
        String string = p.belowNameData.score.updateAndGet();
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            // Not an integer (float or invalid)
            try {
                int value = (int) Math.round(Double.parseDouble(string));
                // Float
                TAB.getInstance().getConfigHelper().runtime().floatInBelowName(p, configuration.getValue(), string);
                return value;
            } catch (NumberFormatException e2) {
                // Not a float (invalid)
                TAB.getInstance().getConfigHelper().runtime().invalidNumberForBelowName(p, configuration.getValue(), string);
                return 0;
            }
        }
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating BelowName value";
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (refreshed.belowNameData.score == null) return; // Player not loaded yet (refresh called before onJoin)
        int number = getValue(refreshed);
        Property fancy = refreshed.belowNameData.numberFormat;
        fancy.update();
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            if (!sameServerAndWorld(viewer, refreshed)) continue;
            setScore(viewer, refreshed, number, fancy.getFormat(viewer));
        }
        sendProxyMessage(refreshed.getUniqueId(), number, fancy.get());
    }

    private void register(@NotNull TabPlayer player) {
        player.getScoreboard().registerObjective(
                OBJECTIVE_NAME,
                cache.get(player.belowNameData.text.updateAndGet()),
                Scoreboard.HealthDisplay.INTEGER,
                cache.get(player.belowNameData.defaultNumberFormat.updateAndGet())
        );
        player.getScoreboard().setDisplaySlot(OBJECTIVE_NAME, Scoreboard.DisplaySlot.BELOW_NAME);
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
     * @param   fancyDisplay
     *          NumberFormat display of the score
     */
    public void setScore(@NotNull TabPlayer viewer, @NotNull TabPlayer scoreHolder, int value, @NotNull String fancyDisplay) {
        if (viewer.belowNameData.disabled.get()) return;
        if (!viewer.canSee(scoreHolder)) return; // Prevent hack clients from knowing vanished players are connected
        viewer.getScoreboard().setScore(
                OBJECTIVE_NAME,
                scoreHolder.getNickname(),
                value,
                null, // Unused by this objective slot
                cache.get(fancyDisplay)
        );
    }

    /**
     * Returns {@code true} if the two players are in the same server and world,
     * {@code false} if not.
     *
     * @param   player1
     *          First player
     * @param   player2
     *          Second player
     * @return  {@code true} if players are in the same server and world, {@code false} otherwise
     */
    private boolean sameServerAndWorld(@NotNull TabPlayer player1, @NotNull TabPlayer player2) {
        return player1.server == player2.server && player1.world == player2.world;
    }

    @Override
    public void onServerChange(@NotNull TabPlayer changed, @NotNull Server from, @NotNull Server to) {
        updatePlayer(changed);
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer changed, @NotNull World from, @NotNull World to) {
        updatePlayer(changed);
    }

    private void updatePlayer(@NotNull TabPlayer player) {
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            if (!sameServerAndWorld(all, player)) continue;
            setScore(player, all, getValue(all), all.belowNameData.numberFormat.getFormat(player));
            if (all != player) setScore(all, player, getValue(player), player.belowNameData.numberFormat.getFormat(all));
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
                setScore(viewer, player, value, player.belowNameData.numberFormat.get());
            }
        }, getFeatureName(), TabConstants.CpuUsageCategory.NICKNAME_CHANGE_PROCESS));
    }

    @Override
    public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
        onlinePlayers.removePlayer(disconnectedPlayer);
    }

    @Override
    public void onJoin(@NotNull ProxyPlayer player) {
        updatePlayer(player);
    }

    public void updatePlayer(@NotNull ProxyPlayer player) {
        if (player.getBelowname() == null) return; // Player not loaded yet
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            if (viewer.belowNameData.disabled.get()) continue;
            viewer.getScoreboard().setScore(
                    OBJECTIVE_NAME,
                    player.getNickname(),
                    player.getBelowname().getValue(),
                    null, // Unused by this objective slot
                    cache.get(player.getBelowname().getFancyValue())
            );
        }
    }

    // ------------------
    // ProxySupport
    // ------------------

    private void sendProxyMessage(@NotNull TabPlayer player) {
        if (proxy == null) return;
        sendProxyMessage(
                player.getUniqueId(),
                getValue(player),
                player.belowNameData.numberFormat.get()
        );
    }

    private void sendProxyMessage(@NotNull UUID uniqueId, int value, @NotNull String fancyValue) {
        if (proxy == null) return;
        proxy.sendMessage(new BelowNameProxyPlayerData(this, proxy.getIdCounter().incrementAndGet(), uniqueId, value, fancyValue));
    }

    @Override
    public void onProxyLoadRequest() {
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            sendProxyMessage(all);
        }
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "BelowName";
    }

    @Override
    public void onVanishStatusChange(@NotNull TabPlayer player) {
        if (player.isVanished()) return;
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            setScore(viewer, player, getValue(player), player.belowNameData.numberFormat.getFormat(viewer));
        }
    }
}