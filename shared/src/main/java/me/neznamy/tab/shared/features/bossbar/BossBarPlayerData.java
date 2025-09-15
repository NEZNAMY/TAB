package me.neznamy.tab.shared.features.bossbar;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Class storing bossbar data for players.
 */
public class BossBarPlayerData {

    /** Whether player wishes to see boss bars or not */
    public boolean visible;

    /** Boss bars this player can currently see */
    public final Map<BossBarLine, BossBarLinePlayerProperties> visibleBossBars = new IdentityHashMap<>();
}