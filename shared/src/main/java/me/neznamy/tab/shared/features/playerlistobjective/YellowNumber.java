package me.neznamy.tab.shared.features.playerlistobjective;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.chat.component.SimpleTextComponent;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.message.ProxyMessage;
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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Feature handler for scoreboard objective with
 * PLAYER_LIST display slot (in tablist).
 */
public class YellowNumber extends RefreshableFeature implements JoinListener, QuitListener, Loadable,
        CustomThreaded, ProxyFeature {

    /** Objective name used by this feature */
    public static final String OBJECTIVE_NAME = "TAB-PlayerList";

    /** Scoreboard title which is unused in java */
    private static final TabComponent TITLE = new SimpleTextComponent("Java Edition is better"); // Unused by this objective slot (on Java, only visible on Bedrock)

    @Getter
    private final StringToComponentCache cache = new StringToComponentCache("Playerlist Objective", 1000);

    @Getter
    private final ThreadExecutor customThread = new ThreadExecutor("TAB Playerlist Objective Thread");

    @Getter
    private OnlinePlayers onlinePlayers;

    private final PlayerListObjectiveConfiguration configuration;

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
        disableChecker = new DisableChecker(this, Condition.getCondition(configuration.getDisableCondition()), this::onDisableConditionChange, p -> p.playerlistObjectiveData.disabled);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.YELLOW_NUMBER + "-Condition", disableChecker);
        if (proxy != null) {
            proxy.registerMessage("yellow-number", UpdateProxyPlayer.class, UpdateProxyPlayer::new);
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

    @Override
    public void load() {
        onlinePlayers = new OnlinePlayers(TAB.getInstance().getOnlinePlayers());
        Map<TabPlayer, Integer> values = new HashMap<>();
        for (TabPlayer loaded : onlinePlayers.getPlayers()) {
            loaded.playerlistObjectiveData.valueLegacy = new Property(this, loaded, configuration.getValue());
            loaded.playerlistObjectiveData.valueModern = new Property(this, loaded, configuration.getFancyValue());
            if (disableChecker.isDisableConditionMet(loaded)) {
                loaded.playerlistObjectiveData.disabled.set(true);
            } else {
                register(loaded);
            }
            values.put(loaded, getValueNumber(loaded));
            if (proxy != null) {
                proxy.sendMessage(new UpdateProxyPlayer(loaded.getTablistId(), values.get(loaded), loaded.playerlistObjectiveData.valueModern.get()));
            }
        }
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            for (Map.Entry<TabPlayer, Integer> entry : values.entrySet()) {
                setScore(viewer, entry.getKey(), entry.getValue(), entry.getKey().playerlistObjectiveData.valueModern.getFormat(viewer));
            }
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        onlinePlayers.addPlayer(connectedPlayer);
        connectedPlayer.playerlistObjectiveData.valueLegacy = new Property(this, connectedPlayer, configuration.getValue());
        connectedPlayer.playerlistObjectiveData.valueModern = new Property(this, connectedPlayer, configuration.getFancyValue());
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
            proxy.sendMessage(new UpdateProxyPlayer(connectedPlayer.getTablistId(), getValueNumber(connectedPlayer), connectedPlayer.playerlistObjectiveData.valueModern.get()));
            if (connectedPlayer.playerlistObjectiveData.disabled.get()) return;
            for (ProxyPlayer proxied : proxy.getProxyPlayers().values()) {
                if (proxied.getPlayerlistFancy() == null) continue; // This proxy player is not loaded yet
                connectedPlayer.getScoreboard().setScore(
                        OBJECTIVE_NAME,
                        proxied.getNickname(),
                        proxied.getPlayerlistNumber(),
                        null, // Unused by this objective slot
                        proxied.getPlayerlistFancy()
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
                proxy.sendMessage(new UpdateProxyPlayer(p.getTablistId(), getValueNumber(p), p.playerlistObjectiveData.valueModern.get()));
                for (ProxyPlayer proxied : proxy.getProxyPlayers().values()) {
                    if (proxied.getPlayerlistFancy() == null) continue; // This proxy player is not loaded yet
                    p.getScoreboard().setScore(
                            OBJECTIVE_NAME,
                            proxied.getNickname(),
                            proxied.getPlayerlistNumber(),
                            null, // Unused by this objective slot
                            proxied.getPlayerlistFancy()
                    );
                }
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
        if (proxy != null) proxy.sendMessage(new UpdateProxyPlayer(refreshed.getTablistId(), value, refreshed.playerlistObjectiveData.valueModern.get()));
    }

    private void register(@NotNull TabPlayer player) {
        player.getScoreboard().registerObjective(Scoreboard.DisplaySlot.PLAYER_LIST, OBJECTIVE_NAME, TITLE, configuration.getHealthDisplay(), new SimpleTextComponent(""));
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

    @Override
    public void onProxyLoadRequest() {
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            proxy.sendMessage(new UpdateProxyPlayer(all.getTablistId(), getValueNumber(all), all.playerlistObjectiveData.valueModern.get()));
        }
    }

    @Override
    public void onJoin(@NotNull ProxyPlayer player) {
        if (TAB.getInstance().getPlatform().isProxy()) return;
        if (player.getPlayerlistFancy() == null) return; // This proxy player is not loaded yet
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            if (viewer.playerlistObjectiveData.disabled.get() || viewer.getUniqueId().equals(player.getUniqueId())) continue;
            viewer.getScoreboard().setScore(
                    OBJECTIVE_NAME,
                    player.getNickname(),
                    player.getPlayerlistNumber(),
                    null, // Unused by this objective slot
                    player.getPlayerlistFancy()
            );
        }
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "Playerlist Objective";
    }

    /**
     * Class holding header/footer data for players.
     */
    public static class PlayerData {

        /** Player's score value */
        public Property valueLegacy;

        /** Player's score number format */
        public Property valueModern;

        /** Flag tracking whether this feature is disabled for the player with condition or not */
        public final AtomicBoolean disabled = new AtomicBoolean();
    }

    /**
     * Proxy message to update playerlist objective data of a player.
     */
    @NoArgsConstructor
    @AllArgsConstructor
    private class UpdateProxyPlayer extends ProxyMessage {

        private UUID playerId;
        private int value;
        private String fancyValue;

        @NotNull
        public ThreadExecutor getCustomThread() {
            return customThread;
        }

        @Override
        public void write(@NotNull ByteArrayDataOutput out) {
            writeUUID(out, playerId);
            out.writeInt(value);
            out.writeUTF(fancyValue);
        }

        @Override
        public void read(@NotNull ByteArrayDataInput in) {
            playerId = readUUID(in);
            value = in.readInt();
            fancyValue = in.readUTF();
        }

        @Override
        public void process(@NotNull ProxySupport proxySupport) {
            ProxyPlayer target = proxySupport.getProxyPlayers().get(playerId);
            if (target == null) {
                TAB.getInstance().getErrorManager().printError("Unable to process Playerlist objective update of proxy player " + playerId + ", because no such player exists", null);
                return;
            }
            if (target.getPlayerlistFancy() == null) {
                TAB.getInstance().debug("Processing playerlist objective join of proxy player " + target.getName());
            }
            target.setPlayerlistNumber(value);
            target.setPlayerlistFancy(cache.get(fancyValue));
            for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                if (viewer.playerlistObjectiveData.disabled.get()) continue;
                viewer.getScoreboard().setScore(
                        OBJECTIVE_NAME,
                        target.getNickname(),
                        target.getPlayerlistNumber(),
                        null, // Unused by this objective slot
                        target.getPlayerlistFancy()
                );
            }
        }
    }
}