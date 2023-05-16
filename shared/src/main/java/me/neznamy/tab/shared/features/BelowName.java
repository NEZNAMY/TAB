package me.neznamy.tab.shared.features;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
 * Feature handler for BelowName feature
 */
public class BelowName extends TabFeature implements JoinListener, Loadable, UnLoadable,
        ServerSwitchListener, Refreshable {

    public static final String OBJECTIVE_NAME = "TAB-BelowName";

    @Getter private final String refreshDisplayName = "Updating BelowName number";
    @Getter private final String featureName = "BelowName";
    private final String rawNumber = TAB.getInstance().getConfiguration().getConfig().getString("belowname-objective.number", TabConstants.Placeholder.HEALTH);
    private final String rawText = TAB.getInstance().getConfiguration().getConfig().getString("belowname-objective.text", "Health");
    private final TextRefresher textRefresher = new TextRefresher(this);
    private final DisableChecker disableChecker;
    private RedisSupport redis;

    public BelowName() {
        Condition disableCondition = Condition.getCondition(TAB.getInstance().getConfig().getString("belowname-objective.disable-condition"));
        disableChecker = new DisableChecker(featureName, disableCondition, this::onDisableConditionChange);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.BELOW_NAME + "-Condition", disableChecker);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.BELOW_NAME_TEXT, textRefresher);
        TAB.getInstance().getMisconfigurationHelper().checkBelowNameText(rawText);
    }

    @Override
    public void load() {
        redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        for (TabPlayer loaded : TAB.getInstance().getOnlinePlayers()) {
            loaded.setProperty(this, TabConstants.Property.BELOWNAME_NUMBER, rawNumber);
            loaded.setProperty(textRefresher, TabConstants.Property.BELOWNAME_TEXT, rawText);
            if (disableChecker.isDisableConditionMet(loaded)) {
                disableChecker.addDisabledPlayer(loaded);
                continue;
            }
            loaded.getScoreboard().registerObjective(OBJECTIVE_NAME, loaded.getProperty(TabConstants.Property.BELOWNAME_TEXT).updateAndGet(), false);
            loaded.getScoreboard().setDisplaySlot(Scoreboard.DisplaySlot.BELOW_NAME, OBJECTIVE_NAME);
        }
        Map<TabPlayer, Integer> values = new HashMap<>();
        for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
            if (disableChecker.isDisabledPlayer(target)) continue;
            values.put(target, getValue(target));
        }
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (disableChecker.isDisabledPlayer(viewer)) continue;
            for (Map.Entry<TabPlayer, Integer> entry : values.entrySet()) {
                viewer.getScoreboard().setScore(OBJECTIVE_NAME, entry.getKey().getNickname(), entry.getValue());
            }
        }
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (disableChecker.isDisabledPlayer(p)) continue;
            p.getScoreboard().unregisterObjective(OBJECTIVE_NAME);
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        connectedPlayer.setProperty(this, TabConstants.Property.BELOWNAME_NUMBER, rawNumber);
        connectedPlayer.setProperty(textRefresher, TabConstants.Property.BELOWNAME_TEXT, rawText);
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            disableChecker.addDisabledPlayer(connectedPlayer);
            return;
        }
        connectedPlayer.getScoreboard().registerObjective(OBJECTIVE_NAME, connectedPlayer.getProperty(TabConstants.Property.BELOWNAME_TEXT).updateAndGet(), false);
        connectedPlayer.getScoreboard().setDisplaySlot(Scoreboard.DisplaySlot.BELOW_NAME, OBJECTIVE_NAME);
        int number = getValue(connectedPlayer);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (!disableChecker.isDisabledPlayer(all)) all.getScoreboard().setScore(OBJECTIVE_NAME, connectedPlayer.getNickname(), number);
            connectedPlayer.getScoreboard().setScore(OBJECTIVE_NAME, all.getNickname(), getValue(all));
        }
        if (redis != null) redis.updateBelowName(connectedPlayer, number);
    }

    @Override
    public void onServerChange(@NotNull TabPlayer player, @NotNull String from, @NotNull String to) {
        if (disableChecker.isDisabledPlayer(player)) return;
        player.getScoreboard().registerObjective(OBJECTIVE_NAME, player.getProperty(TabConstants.Property.BELOWNAME_TEXT).updateAndGet(), false);
        player.getScoreboard().setDisplaySlot(Scoreboard.DisplaySlot.BELOW_NAME, OBJECTIVE_NAME);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            player.getScoreboard().setScore(OBJECTIVE_NAME, all.getNickname(), getValue(all));
        }
    }

    public void onDisableConditionChange(TabPlayer p, boolean disabledNow) {
        if (disabledNow) {
            p.getScoreboard().unregisterObjective(OBJECTIVE_NAME);
        } else {
            onJoin(p);
        }
    }

    public int getValue(@NotNull TabPlayer p) {
        return TAB.getInstance().getErrorManager().parseInteger(p.getProperty(TabConstants.Property.BELOWNAME_NUMBER).updateAndGet(), 0);
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (disableChecker.isDisabledPlayer(refreshed)) return;
        int number = getValue(refreshed);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            all.getScoreboard().setScore(OBJECTIVE_NAME, refreshed.getNickname(), number);
        }
        if (redis != null) redis.updateBelowName(refreshed, number);
    }

    @RequiredArgsConstructor
    public static class TextRefresher extends TabFeature implements Refreshable {

        @Getter private final String refreshDisplayName = "Updating BelowName text";
        @Getter private final String featureName = "BelowName";
        private final BelowName feature;

        @Override
        public void refresh(@NotNull TabPlayer refreshed, boolean force) {
            if (feature.disableChecker.isDisabledPlayer(refreshed)) return;
            refreshed.getScoreboard().updateObjective(OBJECTIVE_NAME, refreshed.getProperty(TabConstants.Property.BELOWNAME_TEXT).updateAndGet(), false);
        }
    }
}