package me.neznamy.tab.shared.features;

import lombok.Getter;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.types.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Feature handler for scoreboard objective with
 * PLAYER_LIST display slot (in tablist).
 */
public class YellowNumber extends TabFeature implements JoinListener, Loadable, UnLoadable,
        Refreshable, LoginPacketListener {

    @Getter private final String featureName = "Playerlist Objective";
    @Getter private final String refreshDisplayName = "Updating value";
    @Getter private final String PROPERTY_VALUE = Property.randomName();
    @Getter private final String PROPERTY_VALUE_FANCY = Property.randomName();

    /** Objective name used by this feature */
    public static final String OBJECTIVE_NAME = "TAB-PlayerList";

    /** Scoreboard title which is unused in java */
    private static final String TITLE = "PlayerListObjectiveTitle"; // Unused by this objective slot (on Java, only visible on Bedrock)

    /** Numeric value to display */
    private final String rawValue = config().getString("playerlist-objective.value", TabConstants.Placeholder.PING);
    private final String rawValueFancy = config().getString("playerlist-objective.fancy-value", "&7Ping: %ping%");

    /** Scoreboard display type */
    private final int displayType = TabConstants.Placeholder.HEALTH.equals(rawValue) ||
            "%player_health%".equals(rawValue) || "%player_health_rounded%".equals(rawValue) ?
            Scoreboard.HealthDisplay.HEARTS : Scoreboard.HealthDisplay.INTEGER;
    private final DisableChecker disableChecker;
    private RedisSupport redis;

    /**
     * Constructs new instance and registers disable condition checker to feature manager.
     */
    public YellowNumber() {
        Condition disableCondition = Condition.getCondition(config().getString("playerlist-objective.disable-condition"));
        disableChecker = new DisableChecker(featureName, disableCondition, this::onDisableConditionChange);
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
        return TAB.getInstance().getErrorManager().parseInteger(p.getProperty(PROPERTY_VALUE).updateAndGet(), 0);
    }

    @Override
    public void load() {
        redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        Map<TabPlayer, Integer> values = new HashMap<>();
        for (TabPlayer loaded : TAB.getInstance().getOnlinePlayers()) {
            loaded.setProperty(this, PROPERTY_VALUE, rawValue);
            loaded.setProperty(this, PROPERTY_VALUE_FANCY, rawValueFancy);
            if (disableChecker.isDisableConditionMet(loaded)) {
                disableChecker.addDisabledPlayer(loaded);
            } else {
                register(loaded);
            }
            values.put(loaded, getValueNumber(loaded));
        }
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            for (Map.Entry<TabPlayer, Integer> entry : values.entrySet()) {
                setScore(viewer, entry.getKey(), entry.getValue(), entry.getKey().getProperty(PROPERTY_VALUE_FANCY).getFormat(viewer));
            }
        }
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (disableChecker.isDisabledPlayer(p) || p.isBedrockPlayer()) continue;
            p.getScoreboard().unregisterObjective(OBJECTIVE_NAME);
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        connectedPlayer.setProperty(this, PROPERTY_VALUE, rawValue);
        connectedPlayer.setProperty(this, PROPERTY_VALUE_FANCY, rawValueFancy);
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            disableChecker.addDisabledPlayer(connectedPlayer);
        } else {
            register(connectedPlayer);
        }
        int value = getValueNumber(connectedPlayer);
        Property valueFancy = connectedPlayer.getProperty(PROPERTY_VALUE_FANCY);
        valueFancy.update();
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            setScore(all, connectedPlayer, value, valueFancy.getFormat(connectedPlayer));
            setScore(connectedPlayer, all, getValueNumber(all), all.getProperty(PROPERTY_VALUE_FANCY).getFormat(connectedPlayer));
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
        if (disabledNow) {
            p.getScoreboard().unregisterObjective(OBJECTIVE_NAME);
        } else {
            onJoin(p);
        }
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        int value = getValueNumber(refreshed);
        Property fancy = refreshed.getProperty(PROPERTY_VALUE_FANCY);
        fancy.update();
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            setScore(viewer, refreshed, value, fancy.getFormat(viewer));
        }
        if (redis != null) redis.updateYellowNumber(refreshed, value, fancy.get());
    }

    @Override
    public void onLoginPacket(@NotNull TabPlayer p) {
        if (disableChecker.isDisabledPlayer(p) || !p.isLoaded()) return;
        register(p);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all.isLoaded()) setScore(p, all, getValueNumber(all), all.getProperty(PROPERTY_VALUE_FANCY).getFormat(p));
        }
    }

    private void register(@NotNull TabPlayer player) {
        if (player.isBedrockPlayer()) return;
        player.getScoreboard().registerObjective(OBJECTIVE_NAME, TITLE, displayType, new IChatBaseComponent(""));
        player.getScoreboard().setDisplaySlot(Scoreboard.DisplaySlot.PLAYER_LIST, OBJECTIVE_NAME);
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
        if (viewer.isBedrockPlayer() || disableChecker.isDisabledPlayer(viewer)) return;
        viewer.getScoreboard().setScore(
                OBJECTIVE_NAME,
                scoreHolder.getNickname(),
                value,
                null, // Unused by this objective slot
                IChatBaseComponent.emptyToNullOptimizedComponent(fancyValue)
        );
    }
}