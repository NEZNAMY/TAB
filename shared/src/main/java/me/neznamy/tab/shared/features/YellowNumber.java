package me.neznamy.tab.shared.features;

import lombok.Getter;
import me.neznamy.tab.api.*;
import me.neznamy.tab.api.feature.*;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.redis.RedisSupport;

/**
 * Feature handler for scoreboard objective with
 * PLAYER_LIST display slot (in tablist).
 */
public class YellowNumber extends TabFeature implements JoinListener, LoginPacketListener, Loadable, UnLoadable,
        WorldSwitchListener, ServerSwitchListener, Refreshable {

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

    private final RedisSupport redis = (RedisSupport) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);

    /**
     * Constructs new instance and sends debug message that feature loaded.
     */
    public YellowNumber() {
        super("yellow-number-in-tablist");
    }

    /**
     * Returns current value for specified player
     *
     * @param   p
     *          Player to get value of
     * @return  Current value of player
     */
    public int getValue(TabPlayer p) {
        return TAB.getInstance().getErrorManager().parseInteger(p.getProperty(TabConstants.Property.YELLOW_NUMBER).updateAndGet(), 0);
    }

    @Override
    public void load() {
        for (TabPlayer loaded : TAB.getInstance().getOnlinePlayers()) {
            loaded.setProperty(this, TabConstants.Property.YELLOW_NUMBER, rawValue);
            if (isDisabled(loaded.getServer(), loaded.getWorld())) {
                addDisabledPlayer(loaded);
                continue;
            }
            if (loaded.isBedrockPlayer()) continue;
            loaded.getScoreboard().registerObjective(OBJECTIVE_NAME, TITLE, displayType);
            loaded.getScoreboard().setDisplaySlot(Scoreboard.DisplaySlot.PLAYER_LIST, OBJECTIVE_NAME);
        }
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (isDisabledPlayer(viewer) || viewer.isBedrockPlayer()) continue;
            for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
                viewer.getScoreboard().setScore(OBJECTIVE_NAME, target.getNickname(), getValue(target));
            }
        }
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (isDisabledPlayer(p) || p.isBedrockPlayer()) continue;
            p.getScoreboard().unregisterObjective(OBJECTIVE_NAME);
        }
    }

    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        connectedPlayer.setProperty(this, TabConstants.Property.YELLOW_NUMBER, rawValue);
        if (isDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld())) {
            addDisabledPlayer(connectedPlayer);
            return;
        }
        if (!connectedPlayer.isBedrockPlayer()) {
            connectedPlayer.getScoreboard().registerObjective(OBJECTIVE_NAME, TITLE, displayType);
            connectedPlayer.getScoreboard().setDisplaySlot(Scoreboard.DisplaySlot.PLAYER_LIST, OBJECTIVE_NAME);
        }
        int value = getValue(connectedPlayer);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (!isDisabledPlayer(all)) {
                if (!all.isBedrockPlayer()) {
                    all.getScoreboard().setScore(OBJECTIVE_NAME, connectedPlayer.getNickname(), value);
                }
                if (!connectedPlayer.isBedrockPlayer()) {
                    connectedPlayer.getScoreboard().setScore(OBJECTIVE_NAME, all.getNickname(), getValue(all));
                }
            }
        }
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
            if (!p.isBedrockPlayer()) p.getScoreboard().unregisterObjective(OBJECTIVE_NAME);
        }
        if (!disabledNow && disabledBefore) {
            onJoin(p);
            if (redis != null) redis.updateYellowNumber(p, p.getProperty(TabConstants.Property.YELLOW_NUMBER).get());
        }
    }

    @Override
    public void refresh(TabPlayer refreshed, boolean force) {
        int value = getValue(refreshed);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (isDisabledPlayer(all) || all.isBedrockPlayer()) continue;
            all.getScoreboard().setScore(OBJECTIVE_NAME, refreshed.getNickname(), value);
        }
        if (redis != null) redis.updateYellowNumber(refreshed, refreshed.getProperty(TabConstants.Property.YELLOW_NUMBER).get());
    }

    @Override
    public void onLoginPacket(TabPlayer packetReceiver) {
        if (isDisabledPlayer(packetReceiver) || packetReceiver.isBedrockPlayer()) return;
        packetReceiver.getScoreboard().registerObjective(OBJECTIVE_NAME, TITLE, displayType);
        packetReceiver.getScoreboard().setDisplaySlot(Scoreboard.DisplaySlot.PLAYER_LIST, OBJECTIVE_NAME);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all.isLoaded()) {
                packetReceiver.getScoreboard().setScore(OBJECTIVE_NAME, all.getNickname(), getValue(all));
            }
        }
    }
}