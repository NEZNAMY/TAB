package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Feature handler for BelowName feature
 */
public class BelowName extends RefreshableFeature implements JoinListener, Loadable, UnLoadable,
        LoginPacketListener, WorldSwitchListener, ServerSwitchListener {

    /** Objective name used by this feature */
    public static final String OBJECTIVE_NAME = "TAB-BelowName";
    
    private final String rawNumber = config().getString("belowname-objective.number", TabConstants.Placeholder.HEALTH);
    private final String rawText = config().getString("belowname-objective.text", "Health");
    private final String fancyDisplayDefault = config().getString("belowname-objective.fancy-display-default", "NPC");
    private final String fancyDisplayPlayers = config().getString("belowname-objective.fancy-display-players", "&c" + TabConstants.Placeholder.HEALTH);

    private final TextRefresher textRefresher = new TextRefresher();
    private final DisableChecker disableChecker;
    private RedisSupport redis;

    /**
     * Constructs new instance and registers disable condition checker and text refresher to feature manager.
     */
    public BelowName() {
        super("BelowName", "Updating BelowName number");
        Condition disableCondition = Condition.getCondition(config().getString("belowname-objective.disable-condition"));
        disableChecker = new DisableChecker(getFeatureName(), disableCondition, this::onDisableConditionChange, p -> p.belowNameData.disabled);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.BELOW_NAME + "-Condition", disableChecker);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.BELOW_NAME_TEXT, textRefresher);
        TAB.getInstance().getConfigHelper().startup().checkBelowNameText(rawText);
    }

    @Override
    public void load() {
        redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        Map<TabPlayer, Integer> values = new HashMap<>();
        for (TabPlayer loaded : TAB.getInstance().getOnlinePlayers()) {
            loadProperties(loaded);
            if (disableChecker.isDisableConditionMet(loaded)) {
                loaded.belowNameData.disabled.set(true);
            } else {
                register(loaded);
            }
            values.put(loaded, getValue(loaded));
        }
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            for (Map.Entry<TabPlayer, Integer> entry : values.entrySet()) {
                TabPlayer target = entry.getKey();
                if (!sameServerAndWorld(viewer, target)) continue;
                setScore(viewer, target, entry.getValue(), target.belowNameData.numberFormat.getFormat(viewer));
            }
        }
    }

    private void loadProperties(@NotNull TabPlayer player) {
        player.belowNameData.score = new Property(this, player, rawNumber);
        player.belowNameData.numberFormat = new Property(this, player, fancyDisplayPlayers);
        player.belowNameData.text = new Property(textRefresher, player, rawText);
        player.belowNameData.defaultNumberFormat = new Property(textRefresher, player, fancyDisplayDefault);
    }

    @Override
    public void unload() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (p.belowNameData.disabled.get()) continue;
            p.getScoreboard().unregisterObjective(OBJECTIVE_NAME);
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        loadProperties(connectedPlayer);
        if (disableChecker.isDisableConditionMet(connectedPlayer)) {
            connectedPlayer.belowNameData.disabled.set(true);
        } else {
            register(connectedPlayer);
        }
        int number = getValue(connectedPlayer);
        Property fancy = connectedPlayer.belowNameData.numberFormat;
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (!sameServerAndWorld(connectedPlayer, all)) continue;
            setScore(all, connectedPlayer, number, fancy.getFormat(all));
            if (all != connectedPlayer) {
                setScore(connectedPlayer, all, getValue(all), all.belowNameData.numberFormat.getFormat(connectedPlayer));
            }
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
        String string = p.belowNameData.score.updateAndGet();
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            // Not an integer (float or invalid)
            try {
                int value = (int) Math.round(Double.parseDouble(string));
                // Float
                TAB.getInstance().getConfigHelper().runtime().floatInBelowName(p, rawNumber, string);
                return value;
            } catch (NumberFormatException e2) {
                // Not a float (invalid)
                TAB.getInstance().getConfigHelper().runtime().invalidNumberForBelowName(p, rawNumber, string);
                return 0;
            }
        }
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        int number = getValue(refreshed);
        Property fancy = refreshed.belowNameData.numberFormat;
        fancy.update();
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (!sameServerAndWorld(viewer, refreshed)) continue;
            setScore(viewer, refreshed, number, fancy.getFormat(viewer));
        }
        if (redis != null) redis.updateBelowName(refreshed, number, fancy.get());
    }

    @Override
    public void onLoginPacket(@NotNull TabPlayer player) {
        if (player.belowNameData.disabled.get() || !player.isLoaded()) return;
        register(player);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (!sameServerAndWorld(all, player)) continue;
            if (all.isLoaded()) setScore(player, all, getValue(all), all.belowNameData.numberFormat.getFormat(player));
        }
    }

    private void register(@NotNull TabPlayer player) {
        player.getScoreboard().registerObjective(
                OBJECTIVE_NAME,
                player.belowNameData.text.updateAndGet(),
                Scoreboard.HealthDisplay.INTEGER,
                TabComponent.optimized(player.belowNameData.defaultNumberFormat.updateAndGet())
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
        if (viewer.belowNameData.disabled.get()) return;
        viewer.getScoreboard().setScore(
                OBJECTIVE_NAME,
                scoreHolder.getNickname(),
                value,
                null, // Unused by this objective slot
                TabComponent.optimized(fancyDisplay)
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
        return player1.getServer().equals(player2.getServer()) && player1.getWorld().equals(player2.getWorld());
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
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (!sameServerAndWorld(all, player)) continue;
            setScore(player, all, getValue(all), all.belowNameData.numberFormat.getFormat(player));
            if (all != player) setScore(all, player, getValue(player), player.belowNameData.numberFormat.getFormat(all));
        }
    }

    private static class TextRefresher extends RefreshableFeature {

        private TextRefresher() {
            super("BelowName", "Updating BelowName text");
        }

        @Override
        public void refresh(@NotNull TabPlayer refreshed, boolean force) {
            if (refreshed.belowNameData.disabled.get()) return;
            refreshed.getScoreboard().updateObjective(
                    OBJECTIVE_NAME,
                    refreshed.belowNameData.text.updateAndGet(),
                    Scoreboard.HealthDisplay.INTEGER,
                    TabComponent.optimized(refreshed.belowNameData.defaultNumberFormat.updateAndGet())
            );
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
}