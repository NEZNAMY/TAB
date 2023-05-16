package me.neznamy.tab.shared.features;

import lombok.Getter;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.types.*;
import org.jetbrains.annotations.NotNull;

/**
 * Feature handler for scoreboard objective with
 * PLAYER_LIST display slot (in tablist).
 */
public class YellowNumber extends TabFeature implements JoinListener, Loadable, UnLoadable,
        ServerSwitchListener, Refreshable {

    @Getter private final String featureName = "Yellow Number";
    @Getter private final String refreshDisplayName = "Updating value";

    /** Objective name used by this feature */
    public static final String OBJECTIVE_NAME = "TAB-YellowNumber";

    /** Scoreboard title which is unused in java */
    private static final String TITLE = "PlayerListObjectiveTitle";

    /** Numeric value to display */
    private final String rawValue = TAB.getInstance().getConfiguration().getConfig().getString("yellow-number-in-tablist.value", TabConstants.Placeholder.PING);

    /** Display type, true for HEARTS, false for INTEGER */
    private final boolean displayType = TabConstants.Placeholder.HEALTH.equals(rawValue) || "%player_health%".equals(rawValue) || "%player_health_rounded%".equals(rawValue);
    private final DisableChecker disableChecker;
    private RedisSupport redis;

    public YellowNumber() {
        Condition disableCondition = Condition.getCondition(TAB.getInstance().getConfig().getString("yellow-number-in-tablist.disable-condition"));
        disableChecker = new DisableChecker(featureName, disableCondition, this::onDisableConditionChange);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.YELLOW_NUMBER + "-Condition", disableChecker);
    }

    /**
     * Returns current value for specified player
     *
     * @param   p
     *          Player to get value of
     * @return  Current value of player
     */
    public int getValue(@NotNull TabPlayer p) {
        return TAB.getInstance().getErrorManager().parseInteger(p.getProperty(TabConstants.Property.YELLOW_NUMBER).updateAndGet(), 0);
    }

    @Override
    public void load() {
        redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        for (TabPlayer loaded : TAB.getInstance().getOnlinePlayers()) {
            loaded.setProperty(this, TabConstants.Property.YELLOW_NUMBER, rawValue);
            if (disableChecker.isDisableConditionMet(loaded)) {
                disableChecker.addDisabledPlayer(loaded);
                continue;
            }
            if (loaded.isBedrockPlayer()) continue;
            loaded.getScoreboard().registerObjective(OBJECTIVE_NAME, TITLE, displayType);
            loaded.getScoreboard().setDisplaySlot(Scoreboard.DisplaySlot.PLAYER_LIST, OBJECTIVE_NAME);
        }
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (disableChecker.isDisabledPlayer(viewer) || viewer.isBedrockPlayer()) continue;
            for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
                viewer.getScoreboard().setScore(OBJECTIVE_NAME, target.getNickname(), getValue(target));
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
        connectedPlayer.setProperty(this, TabConstants.Property.YELLOW_NUMBER, rawValue);
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            disableChecker.addDisabledPlayer(connectedPlayer);
            return;
        }
        if (!connectedPlayer.isBedrockPlayer()) {
            connectedPlayer.getScoreboard().registerObjective(OBJECTIVE_NAME, TITLE, displayType);
            connectedPlayer.getScoreboard().setDisplaySlot(Scoreboard.DisplaySlot.PLAYER_LIST, OBJECTIVE_NAME);
        }
        int value = getValue(connectedPlayer);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (!disableChecker.isDisabledPlayer(all)) {
                if (!all.isBedrockPlayer()) {
                    all.getScoreboard().setScore(OBJECTIVE_NAME, connectedPlayer.getNickname(), value);
                }
                if (!connectedPlayer.isBedrockPlayer()) {
                    connectedPlayer.getScoreboard().setScore(OBJECTIVE_NAME, all.getNickname(), getValue(all));
                }
            }
        }
        if (redis != null) redis.updateYellowNumber(connectedPlayer, value);
    }

    @Override
    public void onServerChange(@NotNull TabPlayer p, @NotNull String from, @NotNull String to) {
        if (disableChecker.isDisabledPlayer(p) || p.isBedrockPlayer()) return;
        p.getScoreboard().registerObjective(OBJECTIVE_NAME, TITLE, displayType);
        p.getScoreboard().setDisplaySlot(Scoreboard.DisplaySlot.PLAYER_LIST, OBJECTIVE_NAME);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            p.getScoreboard().setScore(OBJECTIVE_NAME, all.getNickname(), getValue(all));
        }
    }

    public void onDisableConditionChange(TabPlayer p, boolean disabledNow) {
        if (disabledNow) {
            p.getScoreboard().unregisterObjective(OBJECTIVE_NAME);
        } else {
            onJoin(p);
        }
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        int value = getValue(refreshed);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (disableChecker.isDisabledPlayer(all) || all.isBedrockPlayer()) continue;
            all.getScoreboard().setScore(OBJECTIVE_NAME, refreshed.getNickname(), value);
        }
        if (redis != null) redis.updateYellowNumber(refreshed, value);
    }
}