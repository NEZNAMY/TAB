package me.neznamy.tab.shared.features;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
 * Feature handler for BelowName feature
 */
public class BelowName extends TabFeature implements JoinListener, Loadable, UnLoadable,
        Refreshable, LoginPacketListener {

    public static final String OBJECTIVE_NAME = "TAB-BelowName";

    @Getter private final String refreshDisplayName = "Updating BelowName number";
    @Getter private final String featureName = "BelowName";
    @Getter private final String NUMBER_PROPERTY = Property.randomName();
    private final String TEXT_PROPERTY = Property.randomName();
    private final String DEFAULT_FORMAT_PROPERTY = Property.randomName();
    @Getter private final String FANCY_FORMAT_PROPERTY = Property.randomName();
    
    private final String rawNumber = TAB.getInstance().getConfig().getString("belowname-objective.number", TabConstants.Placeholder.HEALTH);
    private final String rawText = TAB.getInstance().getConfig().getString("belowname-objective.text", "Health");
    private final String npcText = TAB.getInstance().getConfig().getString("belowname-objective.npc-text", "NPC");
    private final String fancyDisplay = TAB.getInstance().getConfig().getString("belowname-objective.fancy-display", "&c" + TabConstants.Placeholder.HEALTH);

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
            loaded.setProperty(this, NUMBER_PROPERTY, rawNumber);
            loaded.setProperty(this, FANCY_FORMAT_PROPERTY, fancyDisplay);
            loaded.setProperty(textRefresher, TEXT_PROPERTY, rawText);
            loaded.setProperty(textRefresher, DEFAULT_FORMAT_PROPERTY, npcText);
            if (disableChecker.isDisableConditionMet(loaded)) {
                disableChecker.addDisabledPlayer(loaded);
                continue;
            }
            register(loaded);
        }
        Map<TabPlayer, Integer> values = new HashMap<>();
        for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
            if (disableChecker.isDisabledPlayer(target)) continue;
            values.put(target, getValue(target));
            target.getProperty(FANCY_FORMAT_PROPERTY).updateAndGet();
        }
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (disableChecker.isDisabledPlayer(viewer)) continue;
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
        connectedPlayer.setProperty(this, FANCY_FORMAT_PROPERTY, fancyDisplay);
        connectedPlayer.setProperty(textRefresher, TEXT_PROPERTY, rawText);
        connectedPlayer.setProperty(textRefresher, DEFAULT_FORMAT_PROPERTY, npcText);
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            disableChecker.addDisabledPlayer(connectedPlayer);
            return;
        }
        register(connectedPlayer);
        int number = getValue(connectedPlayer);
        Property fancy = connectedPlayer.getProperty(FANCY_FORMAT_PROPERTY);
        fancy.update();
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (!disableChecker.isDisabledPlayer(all)) setScore(all, connectedPlayer, number, fancy.getFormat(all));
            setScore(connectedPlayer, all, getValue(all), all.getProperty(FANCY_FORMAT_PROPERTY).getFormat(connectedPlayer));
        }
        if (redis != null) redis.updateBelowName(connectedPlayer, number, fancy.get());
    }

    public void onDisableConditionChange(TabPlayer p, boolean disabledNow) {
        if (disabledNow) {
            p.getScoreboard().unregisterObjective(OBJECTIVE_NAME);
        } else {
            onJoin(p);
        }
    }

    public int getValue(@NotNull TabPlayer p) {
        return TAB.getInstance().getErrorManager().parseInteger(p.getProperty(NUMBER_PROPERTY).updateAndGet(), 0);
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (disableChecker.isDisabledPlayer(refreshed)) return;
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
                IChatBaseComponent.emptyToNullOptimizedComponent(player.getProperty(DEFAULT_FORMAT_PROPERTY).updateAndGet())
        );
        player.getScoreboard().setDisplaySlot(Scoreboard.DisplaySlot.BELOW_NAME, OBJECTIVE_NAME);
    }

    public void setScore(@NotNull TabPlayer viewer, @NotNull TabPlayer scoreHolder, int value, @NotNull String fancyDisplay) {
        viewer.getScoreboard().setScore(
                OBJECTIVE_NAME,
                scoreHolder.getNickname(),
                value,
                null, // Unused by this objective slot
                IChatBaseComponent.emptyToNullOptimizedComponent(fancyDisplay)
        );
    }

    @RequiredArgsConstructor
    public static class TextRefresher extends TabFeature implements Refreshable {

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
                    IChatBaseComponent.emptyToNullOptimizedComponent(refreshed.getProperty(feature.DEFAULT_FORMAT_PROPERTY).updateAndGet())
            );
        }
    }
}