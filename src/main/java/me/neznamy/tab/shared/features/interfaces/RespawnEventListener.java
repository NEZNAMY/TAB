package me.neznamy.tab.shared.features.interfaces;

import me.neznamy.tab.api.TabPlayer;

public interface RespawnEventListener extends Feature {

	public void onRespawn(TabPlayer respawned);
}
