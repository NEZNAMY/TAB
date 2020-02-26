package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.ITabPlayer;

public interface SimpleFeature {

	public void load();
	public void unload();
	public void onJoin(ITabPlayer p);
	public void onQuit(ITabPlayer p);
	public void onWorldChange(ITabPlayer p, String from, String to);
}