package me.neznamy.tab.shared.features;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.*;
import me.neznamy.tab.api.feature.*;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.redis.RedisSupport;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Feature handler for BelowName feature
 */
public class BelowName extends TabFeature implements JoinListener, LoginPacketListener, Loadable, UnLoadable,
        WorldSwitchListener, ServerSwitchListener, Refreshable {

    public static final String OBJECTIVE_NAME = "TAB-BelowName";

    @Getter private final String refreshDisplayName = "Updating BelowName number";
    @Getter private final String featureName = "BelowName";
    private final String rawNumber = TAB.getInstance().getConfiguration().getConfig().getString("belowname-objective.number", TabConstants.Placeholder.HEALTH);
    private final String rawText = TAB.getInstance().getConfiguration().getConfig().getString("belowname-objective.text", "Health");
    private final TextRefresher textRefresher = new TextRefresher(this);

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
            loaded.getScoreboard().registerObjective(OBJECTIVE_NAME, loaded.getProperty(TabConstants.Property.BELOWNAME_TEXT).updateAndGet(), false);
            loaded.getScoreboard().setDisplaySlot(Scoreboard.DisplaySlot.BELOW_NAME, OBJECTIVE_NAME);
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
                    viewer.getScoreboard().setScore(OBJECTIVE_NAME, entry.getKey().getNickname(), entry.getValue());
                }
            }
        }
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (isDisabledPlayer(p)) continue;
            p.getScoreboard().unregisterObjective(OBJECTIVE_NAME);
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
        connectedPlayer.getScoreboard().registerObjective(OBJECTIVE_NAME, connectedPlayer.getProperty(TabConstants.Property.BELOWNAME_TEXT).updateAndGet(), false);
        connectedPlayer.getScoreboard().setDisplaySlot(Scoreboard.DisplaySlot.BELOW_NAME, OBJECTIVE_NAME);
        int number = getValue(connectedPlayer);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (sameServerAndWorld(all, connectedPlayer)) {
                all.getScoreboard().setScore(OBJECTIVE_NAME, connectedPlayer.getNickname(), number);
                connectedPlayer.getScoreboard().setScore(OBJECTIVE_NAME, all.getNickname(), getValue(all));
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
            p.getScoreboard().unregisterObjective(OBJECTIVE_NAME);
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
                all.getScoreboard().setScore(OBJECTIVE_NAME, p.getNickname(), number);
                p.getScoreboard().setScore(OBJECTIVE_NAME, all.getNickname(), getValue(all));
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
                all.getScoreboard().setScore(OBJECTIVE_NAME, refreshed.getNickname(), number);
            }
        }
        if (redis != null) redis.updateBelowName(refreshed, refreshed.getProperty(TabConstants.Property.BELOWNAME_NUMBER).get());
    }

    @Override
    public void onLoginPacket(TabPlayer packetReceiver) {
        if (isDisabledPlayer(packetReceiver)) return;
        packetReceiver.getScoreboard().registerObjective(OBJECTIVE_NAME, packetReceiver.getProperty(TabConstants.Property.BELOWNAME_TEXT).updateAndGet(), false);
        packetReceiver.getScoreboard().setDisplaySlot(Scoreboard.DisplaySlot.BELOW_NAME, OBJECTIVE_NAME);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all.isLoaded() && sameServerAndWorld(all, packetReceiver)) {
                packetReceiver.getScoreboard().setScore(OBJECTIVE_NAME, all.getNickname(), getValue(all));
            }
        }
    }

    private boolean sameServerAndWorld(TabPlayer player1, TabPlayer player2) {
        return player2.getWorld().equals(player1.getWorld()) && Objects.equals(player2.getServer(), player1.getServer());
    }

    @RequiredArgsConstructor
    public static class TextRefresher extends TabFeature implements Refreshable {

        @Getter private final String refreshDisplayName = "Updating BelowName text";
        @Getter private final String featureName = "BelowName";
        private final BelowName feature;

        @Override
        public void refresh(TabPlayer refreshed, boolean force) {
            if (feature.isDisabledPlayer(refreshed)) return;
            refreshed.getScoreboard().updateObjective(OBJECTIVE_NAME, refreshed.getProperty(TabConstants.Property.BELOWNAME_TEXT).updateAndGet(), false);
        }
    }
}