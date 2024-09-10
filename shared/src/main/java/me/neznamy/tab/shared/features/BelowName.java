package me.neznamy.tab.shared.features;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.config.files.config.BelownameConfiguration;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.redis.message.RedisMessage;
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
 * Feature handler for BelowName feature
 */
public class BelowName extends RefreshableFeature implements JoinListener, QuitListener, Loadable,
        WorldSwitchListener, ServerSwitchListener, CustomThreaded, RedisFeature {

    /** Objective name used by this feature */
    public static final String OBJECTIVE_NAME = "TAB-BelowName";

    @Getter
    private final StringToComponentCache cache = new StringToComponentCache("BelowName", 1000);

    @Getter
    private final ThreadExecutor customThread = new ThreadExecutor("TAB Belowname Objective Thread");

    @Getter
    private OnlinePlayers onlinePlayers;

    private final BelownameConfiguration configuration;

    private final TextRefresher textRefresher = new TextRefresher();
    private final DisableChecker disableChecker;

    @Nullable
    private final RedisSupport redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);

    /**
     * Constructs new instance and registers disable condition checker and text refresher to feature manager.
     *
     * @param   configuration
     *          Feature configuration
     */
    public BelowName(@NotNull BelownameConfiguration configuration) {
        this.configuration = configuration;
        disableChecker = new DisableChecker(this, Condition.getCondition(configuration.disableCondition), this::onDisableConditionChange, p -> p.belowNameData.disabled);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.BELOW_NAME + "-Condition", disableChecker);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.BELOW_NAME_TEXT, textRefresher);
    }

    @Override
    public void load() {
        if (redis != null) {
            redis.registerMessage("belowname", UpdateRedisPlayer.class, UpdateRedisPlayer::new);
        }
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
            if (redis != null) {
                redis.sendMessage(new UpdateRedisPlayer(loaded.getTablistId(), values.get(loaded), loaded.belowNameData.numberFormat.get()));
            }
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
        player.belowNameData.score = new Property(this, player, configuration.number);
        player.belowNameData.numberFormat = new Property(this, player, configuration.fancyDisplayPlayers);
        player.belowNameData.text = new Property(textRefresher, player, configuration.text);
        player.belowNameData.defaultNumberFormat = new Property(textRefresher, player, configuration.fancyDisplayDefault);
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
        if (redis != null) {
            redis.sendMessage(new UpdateRedisPlayer(connectedPlayer.getTablistId(), getValue(connectedPlayer), connectedPlayer.belowNameData.numberFormat.get()));
            if (connectedPlayer.belowNameData.disabled.get()) return;
            for (RedisPlayer redisPlayer : redis.getRedisPlayers().values()) {
                if (redisPlayer.getBelowNameFancy() == null) continue; // This redis player is not loaded yet
                connectedPlayer.getScoreboard().setScore(
                        OBJECTIVE_NAME,
                        redisPlayer.getNickname(),
                        redisPlayer.getBelowNameNumber(),
                        null, // Unused by this objective slot
                        redisPlayer.getBelowNameFancy()
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
            if (redis != null) {
                redis.sendMessage(new UpdateRedisPlayer(p.getTablistId(), getValue(p), p.belowNameData.numberFormat.get()));
                for (RedisPlayer redisPlayer : redis.getRedisPlayers().values()) {
                    if (redisPlayer.getBelowNameFancy() == null) continue; // This redis player is not loaded yet
                    p.getScoreboard().setScore(
                            OBJECTIVE_NAME,
                            redisPlayer.getNickname(),
                            redisPlayer.getBelowNameNumber(),
                            null, // Unused by this objective slot
                            redisPlayer.getBelowNameFancy()
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
                TAB.getInstance().getConfigHelper().runtime().floatInBelowName(p, configuration.number, string);
                return value;
            } catch (NumberFormatException e2) {
                // Not a float (invalid)
                TAB.getInstance().getConfigHelper().runtime().invalidNumberForBelowName(p, configuration.number, string);
                return 0;
            }
        }
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating BelowName number";
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
        if (redis != null) redis.sendMessage(new UpdateRedisPlayer(refreshed.getTablistId(), number, fancy.get()));
    }

    private void register(@NotNull TabPlayer player) {
        player.getScoreboard().registerObjective(
                Scoreboard.DisplaySlot.BELOW_NAME,
                OBJECTIVE_NAME,
                cache.get(player.belowNameData.text.updateAndGet()),
                Scoreboard.HealthDisplay.INTEGER,
                cache.get(player.belowNameData.defaultNumberFormat.updateAndGet())
        );
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
        return player1.server.equals(player2.server) && player1.world.equals(player2.world);
    }

    @Override
    public void onServerChange(@NotNull TabPlayer changed, @NotNull String from, @NotNull String to) {
        updatePlayer(changed);
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer changed, @NotNull String from, @NotNull String to) {
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
    public void onRedisLoadRequest() {
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            redis.sendMessage(new UpdateRedisPlayer(all.getTablistId(), getValue(all), all.belowNameData.numberFormat.get()));
        }
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "BelowName";
    }

    private class TextRefresher extends RefreshableFeature implements CustomThreaded {

        @NotNull
        @Override
        public String getFeatureName() {
            return "BelowName";
        }

        @NotNull
        @Override
        public String getRefreshDisplayName() {
            return "Updating BelowName text";
        }

        @Override
        public void refresh(@NotNull TabPlayer refreshed, boolean force) {
            if (refreshed.belowNameData.disabled.get()) return;
            refreshed.getScoreboard().updateObjective(
                    OBJECTIVE_NAME,
                    cache.get(refreshed.belowNameData.text.updateAndGet()),
                    Scoreboard.HealthDisplay.INTEGER,
                    cache.get(refreshed.belowNameData.defaultNumberFormat.updateAndGet())
            );
        }

        @Override
        @NotNull
        public ThreadExecutor getCustomThread() {
            return customThread;
        }
    }

    /**
     * Class holding header/footer data for players.
     */
    public static class PlayerData {

        /** Player's score value (1.20.2-) */
        public Property score;

        /** Player's score number format (1.20.3+) */
        public Property numberFormat;

        /** Scoreboard title */
        public Property text;

        /** Default number format for NPCs (1.20.3+) */
        public Property defaultNumberFormat;

        /** Flag tracking whether this feature is disabled for the player with condition or not */
        public final AtomicBoolean disabled = new AtomicBoolean();
    }

    /**
     * Redis message to update belowname data of a player.
     */
    @NoArgsConstructor
    @AllArgsConstructor
    private class UpdateRedisPlayer extends RedisMessage {

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
        public void process(@NotNull RedisSupport redisSupport) {
            RedisPlayer target = redisSupport.getRedisPlayers().get(playerId);
            if (target == null) {
                TAB.getInstance().getErrorManager().printError("Unable to process Belowname objective update of redis player " + playerId + ", because no such player exists", null);
                return;
            }
            if (target.getBelowNameFancy() == null) {
                TAB.getInstance().debug("Processing belowname objective join of redis player " + target.getName());
            }
            target.setBelowNameNumber(value);
            target.setBelowNameFancy(cache.get(fancyValue));
            for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                if (viewer.belowNameData.disabled.get()) continue;
                viewer.getScoreboard().setScore(
                        OBJECTIVE_NAME,
                        target.getNickname(),
                        target.getBelowNameNumber(),
                        null, // Unused by this objective slot
                        target.getBelowNameFancy()
                );
            }
        }
    }
}