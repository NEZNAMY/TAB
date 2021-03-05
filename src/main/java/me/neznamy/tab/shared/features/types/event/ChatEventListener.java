package me.neznamy.tab.shared.features.types.event;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;

public interface ChatEventListener extends Feature {

	public boolean onChat(TabPlayer sender, String message, boolean cancelled);
}