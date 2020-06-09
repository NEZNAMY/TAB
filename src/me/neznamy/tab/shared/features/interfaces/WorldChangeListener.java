package me.neznamy.tab.shared.features.interfaces;

import me.neznamy.tab.shared.ITabPlayer;

public interface WorldChangeListener {

	public void onWorldChange(ITabPlayer changed, String from, String to);
}
