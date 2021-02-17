package me.neznamy.tab.shared.features.types.event;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;

public interface SneakEventListener extends Feature {

	public void onSneak(TabPlayer player, boolean isSneaking);
}
