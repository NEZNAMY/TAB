package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.ITabPlayer;

public interface SimpleFeature {

	public void load();
	public void unload();
	public void onJoin(ITabPlayer connectedPlayer);
	public void onQuit(ITabPlayer disconnectedPlayer);
	public void onWorldChange(ITabPlayer p, String from, String to);
}