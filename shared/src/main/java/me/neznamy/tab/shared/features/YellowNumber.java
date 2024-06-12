package me.neznamy.tab.shared.features;

import lombok.Getter;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.OnlinePlayers;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Feature handler for scoreboard objective with
 * PLAYER_LIST display slot (in tablist).
 */
public class YellowNumber extends RefreshableFeature implements JoinListener, QuitListener, Loadable, UnLoadable, CustomThreaded {

    /** Objective name used by this feature */
    public static final String OBJECTIVE_NAME = "TAB-PlayerList";

    /** Scoreboard title which is unused in java */
    private static final TabComponent TITLE = new SimpleComponent("PlayerListObjectiveTitle"); // Unused by this objective slot (on Java, only visible on Bedrock)

    @Getter
    private final StringToComponentCache cache = new StringToComponentCache("Playerlist Objective", 1000);

    @Getter
    private final ThreadExecutor customThread = new ThreadExecutor("TAB Playerlist Objective Thread");

    @Getter
    private OnlinePlayers onlinePlayers;

    /** Numeric value to display */
    private final String rawValue = config().getString("playerlist-objective.value", TabConstants.Placeholder.PING);
    private final String rawValueFancy = config().getString("playerlist-objective.fancy-value", "&7Ping: " + TabConstants.Placeholder.PING);

    /** Scoreboard display type */
    private final Scoreboard.HealthDisplay displayType = TabConstants.Placeholder.HEALTH.equals(rawValue) ||
            "%player_health%".equals(rawValue) || "%player_health_rounded%".equals(rawValue) ?
            Scoreboard.HealthDisplay.HEARTS : Scoreboard.HealthDisplay.INTEGER;
    private final DisableChecker disableChecker;
    private RedisSupport redis;

    /**
     * Constructs new instance and registers disable condition checker to feature manager.
     */
    public YellowNumber() {
        super("Playerlist Objective", "Updating value");
        Condition disableCondition = Condition.getCondition(config().getString("playerlist-objective.disable-condition"));
        disableChecker = new DisableChecker(getFeatureName(), disableCondition, this::onDisableConditionChange, p -> p.playerlistObjectiveData.disabled);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.YELLOW_NUMBER + "-Condition", disableChecker);
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
                TAB.getInstance().getConfigHelper().runtime().floatInPlayerlistObjective(p, rawValue, string);
                return value;
            } catch (NumberFormatException e2) {
                // Not a float (invalid)
                TAB.getInstance().getConfigHelper().runtime().invalidNumberForPlayerlistObjective(p, rawValue, string);
                return 0;
            }
        }
    }

    @Override
    public void load() {
        onlinePlayers = new OnlinePlayers(TAB.getInstance().getOnlinePlayers());
        redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        Map<TabPlayer, Integer> values = new HashMap<>();
        for (TabPlayer loaded : onlinePlayers.getPlayers()) {
            loaded.playerlistObjectiveData.valueLegacy = new Property(this, loaded, rawValue);
            loaded.playerlistObjectiveData.valueModern = new Property(this, loaded, rawValueFancy);
            if (disableChecker.isDisableConditionMet(loaded)) {
                loaded.playerlistObjectiveData.disabled.set(true);
            } else {
                register(loaded);
            }
            values.put(loaded, getValueNumber(loaded));
        }
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            for (Map.Entry<TabPlayer, Integer> entry : values.entrySet()) {
                setScore(viewer, entry.getKey(), entry.getValue(), entry.getKey().playerlistObjectiveData.valueModern.getFormat(viewer));
            }
        }
    }

    @Override
    public void unload() {
        for (TabPlayer p : onlinePlayers.getPlayers()) {
            if (p.playerlistObjectiveData.disabled.get() || p.isBedrockPlayer()) continue;
            p.getScoreboard().unregisterObjective(OBJECTIVE_NAME);
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        onlinePlayers.addPlayer(connectedPlayer);
        connectedPlayer.playerlistObjectiveData.valueLegacy = new Property(this, connectedPlayer, rawValue);
        connectedPlayer.playerlistObjectiveData.valueModern = new Property(this, connectedPlayer, rawValueFancy);
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
        if (redis != null) redis.updateYellowNumber(connectedPlayer, value, valueFancy.get());
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
        customThread.execute(() -> {
            if (disabledNow) {
                p.getScoreboard().unregisterObjective(OBJECTIVE_NAME);
            } else {
                onJoin(p);
            }
        }, getFeatureName(), TabConstants.CpuUsageCategory.DISABLE_CONDITION_CHANGE);
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        int value = getValueNumber(refreshed);
        refreshed.playerlistObjectiveData.valueModern.update();
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            setScore(viewer, refreshed, value, refreshed.playerlistObjectiveData.valueModern.getFormat(viewer));
        }
        if (redis != null) redis.updateYellowNumber(refreshed, value, refreshed.playerlistObjectiveData.valueModern.get());
    }

    private void register(@NotNull TabPlayer player) {
        if (player.isBedrockPlayer()) return;
        player.getScoreboard().registerObjective(Scoreboard.DisplaySlot.PLAYER_LIST, OBJECTIVE_NAME, TITLE, displayType, new SimpleComponent(""));
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
        if (viewer.isBedrockPlayer() || viewer.playerlistObjectiveData.disabled.get()) return;
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
        customThread.execute(() -> {
            int value = getValueNumber(player);
            for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                setScore(viewer, player, value, player.playerlistObjectiveData.valueModern.get());
            }
        }, getFeatureName(), TabConstants.CpuUsageCategory.NICKNAME_CHANGE_PROCESS);
    }

    @Override
    public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
        onlinePlayers.removePlayer(disconnectedPlayer);
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
}