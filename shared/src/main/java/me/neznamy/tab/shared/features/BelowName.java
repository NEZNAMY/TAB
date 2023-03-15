package me.neznamy.tab.shared.features;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.redis.RedisSupport;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Feature handler for BelowName feature
 */
public class BelowName extends TabFeature {

    public static final String OBJECTIVE_NAME = "TAB-BelowName";
    public static final int DISPLAY_SLOT = 2;

    @Getter private final String refreshDisplayName = "Updating BelowName number";
    @Getter private final String featureName = "BelowName";
    private final String rawNumber = TAB.getInstance().getConfiguration().getConfig().getString("belowname-objective.number", TabConstants.Placeholder.HEALTH);
    private final String rawText = TAB.getInstance().getConfiguration().getConfig().getString("belowname-objective.text", "Health");
    private final TabFeature textRefresher = new TextRefresher(this);

    private final RedisSupport redis = (RedisSupport) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);

    public BelowName() {
        super("belowname-objective");
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.BELOW_NAME_TEXT, textRefresher);
    }

    @Override
    public void load() {
        for (TabPlayer loaded : TAB.getInstance().getOnlinePlayers()) {
            loaded.setProperty(this, TabConstants.Property.BELOWNAME_NUMBER, rawNumber);
            loaded.setProperty(textRefresher, TabConstants.Property.BELOWNAME_TEXT, rawText);
            if (isDisabled(loaded.getServer(), loaded.getWorld())) {
                addDisabledPlayer(loaded);
                continue;
            }
            loaded.sendCustomPacket(new PacketPlayOutScoreboardObjective(0, OBJECTIVE_NAME, loaded.getProperty(TabConstants.Property.BELOWNAME_TEXT).updateAndGet(), EnumScoreboardHealthDisplay.INTEGER), textRefresher);
            loaded.setObjectiveDisplaySlot(DISPLAY_SLOT, OBJECTIVE_NAME);
            TAB.getInstance().getCPUManager().packetSent(textRefresher.getFeatureName());
        }
        Map<TabPlayer, Integer> values = new HashMap<>();
        for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
            if (isDisabledPlayer(target)) continue;
            values.put(target, getValue(target));
        }
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (isDisabledPlayer(viewer)) continue;
            for (Map.Entry<TabPlayer, Integer> entry : values.entrySet()) {
                if (sameServerAndWorld(entry.getKey(), viewer)) {
                    viewer.setScoreboardScore(OBJECTIVE_NAME, entry.getKey().getNickname(), entry.getValue());
                    TAB.getInstance().getCPUManager().packetSent(getFeatureName());
                }
            }
        }
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (isDisabledPlayer(p)) continue;
            p.sendCustomPacket(new PacketPlayOutScoreboardObjective(OBJECTIVE_NAME), textRefresher);
        }
    }

    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        connectedPlayer.setProperty(this, TabConstants.Property.BELOWNAME_NUMBER, rawNumber);
        connectedPlayer.setProperty(textRefresher, TabConstants.Property.BELOWNAME_TEXT, rawText);
        if (isDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld())) {
            addDisabledPlayer(connectedPlayer);
            return;
        }
        connectedPlayer.sendCustomPacket(new PacketPlayOutScoreboardObjective(0, OBJECTIVE_NAME, connectedPlayer.getProperty(TabConstants.Property.BELOWNAME_TEXT).updateAndGet(), EnumScoreboardHealthDisplay.INTEGER), textRefresher);
        connectedPlayer.setObjectiveDisplaySlot(DISPLAY_SLOT, OBJECTIVE_NAME);
        TAB.getInstance().getCPUManager().packetSent(textRefresher.getFeatureName());
        int number = getValue(connectedPlayer);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (sameServerAndWorld(all, connectedPlayer)) {
                all.setScoreboardScore(OBJECTIVE_NAME, connectedPlayer.getNickname(), number);
                TAB.getInstance().getCPUManager().packetSent(getFeatureName());
                connectedPlayer.setScoreboardScore(OBJECTIVE_NAME, all.getNickname(), getValue(all));
                TAB.getInstance().getCPUManager().packetSent(getFeatureName());
            }
        }
        if (redis != null) redis.updateBelowName(connectedPlayer, connectedPlayer.getProperty(TabConstants.Property.BELOWNAME_NUMBER).get());
    }

    @Override
    public void onServerChange(TabPlayer p, String from, String to) {
        onWorldChange(p, null, null);
    }

    @Override
    public void onWorldChange(TabPlayer p, String from, String to) {
        boolean disabledBefore = isDisabledPlayer(p);
        boolean disabledNow = false;
        if (isDisabled(p.getServer(), p.getWorld())) {
            disabledNow = true;
            addDisabledPlayer(p);
        } else {
            removeDisabledPlayer(p);
        }
        if (disabledNow && !disabledBefore) {
            p.sendCustomPacket(new PacketPlayOutScoreboardObjective(OBJECTIVE_NAME), textRefresher);
            return;
        }
        if (!disabledNow && disabledBefore) {
            onJoin(p);
            return;
        }
        if (disabledNow) return;
        int number = getValue(p);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (sameServerAndWorld(all, p)) {
                all.setScoreboardScore(OBJECTIVE_NAME, p.getNickname(), number);
                TAB.getInstance().getCPUManager().packetSent(getFeatureName());
                p.setScoreboardScore(OBJECTIVE_NAME, all.getNickname(), getValue(all));
                TAB.getInstance().getCPUManager().packetSent(getFeatureName());
            }
        }
        if (redis != null) redis.updateBelowName(p, p.getProperty(TabConstants.Property.BELOWNAME_NUMBER).get());
    }

    public int getValue(TabPlayer p) {
        return TAB.getInstance().getErrorManager().parseInteger(p.getProperty(TabConstants.Property.BELOWNAME_NUMBER).updateAndGet(), 0);
    }

    @Override
    public void refresh(TabPlayer refreshed, boolean force) {
        if (isDisabledPlayer(refreshed)) return;
        int number = getValue(refreshed);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (sameServerAndWorld(all, refreshed)) {
                all.setScoreboardScore(OBJECTIVE_NAME, refreshed.getNickname(), number);
                TAB.getInstance().getCPUManager().packetSent(getFeatureName());
            }
        }
        if (redis != null) redis.updateBelowName(refreshed, refreshed.getProperty(TabConstants.Property.BELOWNAME_NUMBER).get());
    }

    @Override
    public void onLoginPacket(TabPlayer packetReceiver) {
        if (isDisabledPlayer(packetReceiver)) return;
        packetReceiver.sendCustomPacket(new PacketPlayOutScoreboardObjective(0, OBJECTIVE_NAME, packetReceiver.getProperty(TabConstants.Property.BELOWNAME_TEXT).updateAndGet(), EnumScoreboardHealthDisplay.INTEGER), textRefresher);
        packetReceiver.setObjectiveDisplaySlot(DISPLAY_SLOT, OBJECTIVE_NAME);
        TAB.getInstance().getCPUManager().packetSent(textRefresher.getFeatureName());
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all.isLoaded() && sameServerAndWorld(all, packetReceiver)) {
                packetReceiver.setScoreboardScore(OBJECTIVE_NAME, all.getNickname(), getValue(all));
                TAB.getInstance().getCPUManager().packetSent(getFeatureName());
            }
        }
    }

    private boolean sameServerAndWorld(TabPlayer player1, TabPlayer player2) {
        return player2.getWorld().equals(player1.getWorld()) && Objects.equals(player2.getServer(), player1.getServer());
    }

    @RequiredArgsConstructor
    public class TextRefresher extends TabFeature {

        @Getter private final String refreshDisplayName = "Updating BelowName text";
        @Getter private final String featureName = "BelowName";
        private final BelowName feature;

        @Override
        public void refresh(TabPlayer refreshed, boolean force) {
            if (feature.isDisabledPlayer(refreshed)) return;
            refreshed.sendCustomPacket(new PacketPlayOutScoreboardObjective(2, OBJECTIVE_NAME, refreshed.getProperty(TabConstants.Property.BELOWNAME_TEXT).updateAndGet(), EnumScoreboardHealthDisplay.INTEGER), textRefresher);
        }
    }
}