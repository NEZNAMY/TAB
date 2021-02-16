package me.neznamy.tab.shared.features.types.event;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;

public interface RespawnEventListener extends Feature {

	public void onRespawn(TabPlayer respawned);
}
