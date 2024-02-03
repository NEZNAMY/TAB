package me.neznamy.tab.shared.features;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.chat.TabComponent;
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
        Refreshable, LoginPacketListener {

    /** Objective name used by this feature */
    public static final String OBJECTIVE_NAME = "TAB-BelowName";

    @Getter private final String refreshDisplayName = "Updating BelowName number";
    @Getter private final String featureName = "BelowName";
    @Getter private final String NUMBER_PROPERTY = Property.randomName();
    private final String TEXT_PROPERTY = Property.randomName();
    private final String DEFAULT_FORMAT_PROPERTY = Property.randomName();
    @Getter private final String FANCY_FORMAT_PROPERTY = Property.randomName();
    
    private final String rawNumber = config().getString("belowname-objective.number", TabConstants.Placeholder.HEALTH);
    private final String rawText = config().getString("belowname-objective.text", "Health");
    private final String fancyDisplayDefault = config().getString("belowname-objective.fancy-display-default", "NPC");
    private final String fancyDisplayPlayers = config().getString("belowname-objective.fancy-display-players", "&c" + TabConstants.Placeholder.HEALTH);

    private final TextRefresher textRefresher = new TextRefresher(this);
    private final DisableChecker disableChecker;
    private RedisSupport redis;

    /**
     * Constructs new instance and registers disable condition checker and text refresher to feature manager.
     */
    public BelowName() {
        Condition disableCondition = Condition.getCondition(config().getString("belowname-objective.disable-condition"));
        disableChecker = new DisableChecker(featureName, disableCondition, this::onDisableConditionChange);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.BELOW_NAME + "-Condition", disableChecker);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.BELOW_NAME_TEXT, textRefresher);
        TAB.getInstance().getConfigHelper().startup().checkBelowNameText(rawText);
    }

    @Override
    public void load() {
        redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        Map<TabPlayer, Integer> values = new HashMap<>();
        for (TabPlayer loaded : TAB.getInstance().getOnlinePlayers()) {
            loaded.setProperty(this, NUMBER_PROPERTY, rawNumber);
            loaded.setProperty(this, FANCY_FORMAT_PROPERTY, fancyDisplayPlayers);
            loaded.setProperty(textRefresher, TEXT_PROPERTY, rawText);
            loaded.setProperty(textRefresher, DEFAULT_FORMAT_PROPERTY, fancyDisplayDefault);
            if (disableChecker.isDisableConditionMet(loaded)) {
                disableChecker.addDisabledPlayer(loaded);
            } else {
                register(loaded);
            }
            values.put(loaded, getValue(loaded));
        }
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            for (Map.Entry<TabPlayer, Integer> entry : values.entrySet()) {
                setScore(viewer, entry.getKey(), entry.getValue(), entry.getKey().getProperty(FANCY_FORMAT_PROPERTY).getFormat(viewer));
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
        connectedPlayer.setProperty(this, NUMBER_PROPERTY, rawNumber);
        connectedPlayer.setProperty(this, FANCY_FORMAT_PROPERTY, fancyDisplayPlayers);
        connectedPlayer.setProperty(textRefresher, TEXT_PROPERTY, rawText);
        connectedPlayer.setProperty(textRefresher, DEFAULT_FORMAT_PROPERTY, fancyDisplayDefault);
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            disableChecker.addDisabledPlayer(connectedPlayer);
        } else {
            register(connectedPlayer);
        }
        int number = getValue(connectedPlayer);
        Property fancy = connectedPlayer.getProperty(FANCY_FORMAT_PROPERTY);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            setScore(all, connectedPlayer, number, fancy.getFormat(all));
            setScore(connectedPlayer, all, getValue(all), all.getProperty(FANCY_FORMAT_PROPERTY).getFormat(connectedPlayer));
        }
        if (redis != null) redis.updateBelowName(connectedPlayer, number, fancy.get());
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

    /**
     * Returns current value for specified player parsed to int.
     *
     * @param   p
     *          Player to get value of
     * @return  Current value for player
     */
    public int getValue(@NotNull TabPlayer p) {
        String string = p.getProperty(NUMBER_PROPERTY).updateAndGet();
        try {
            return (int) Math.round(Double.parseDouble(string));
        } catch (NumberFormatException e) {
            TAB.getInstance().getConfigHelper().runtime().invalidNumberForBelowName(p, rawNumber, string);
            return 0;
        }
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        int number = getValue(refreshed);
        Property fancy = refreshed.getProperty(FANCY_FORMAT_PROPERTY);
        fancy.update();
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            setScore(viewer, refreshed, number, fancy.getFormat(viewer));
        }
        if (redis != null) redis.updateBelowName(refreshed, number, fancy.get());
    }

    @Override
    public void onLoginPacket(@NotNull TabPlayer player) {
        if (disableChecker.isDisabledPlayer(player) || !player.isLoaded()) return;
        register(player);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all.isLoaded()) setScore(player, all, getValue(all), all.getProperty(FANCY_FORMAT_PROPERTY).getFormat(player));
        }
    }

    private void register(@NotNull TabPlayer player) {
        player.getScoreboard().registerObjective(
                OBJECTIVE_NAME,
                player.getProperty(TEXT_PROPERTY).updateAndGet(),
                Scoreboard.HealthDisplay.INTEGER,
                TabComponent.optimized(player.getProperty(DEFAULT_FORMAT_PROPERTY).updateAndGet())
        );
        player.getScoreboard().setDisplaySlot(Scoreboard.DisplaySlot.BELOW_NAME, OBJECTIVE_NAME);
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
        if (disableChecker.isDisabledPlayer(viewer)) return;
        viewer.getScoreboard().setScore(
                OBJECTIVE_NAME,
                scoreHolder.getNickname(),
                value,
                null, // Unused by this objective slot
                TabComponent.optimized(fancyDisplay)
        );
    }

    @RequiredArgsConstructor
    private static class TextRefresher extends TabFeature implements Refreshable {

        @Getter private final String refreshDisplayName = "Updating BelowName text";
        @Getter private final String featureName = "BelowName";
        private final BelowName feature;

        @Override
        public void refresh(@NotNull TabPlayer refreshed, boolean force) {
            if (feature.disableChecker.isDisabledPlayer(refreshed)) return;
            refreshed.getScoreboard().updateObjective(
                    OBJECTIVE_NAME,
                    refreshed.getProperty(feature.TEXT_PROPERTY).updateAndGet(),
                    Scoreboard.HealthDisplay.INTEGER,
                    TabComponent.optimized(refreshed.getProperty(feature.DEFAULT_FORMAT_PROPERTY).updateAndGet())
            );
        }
    }
}