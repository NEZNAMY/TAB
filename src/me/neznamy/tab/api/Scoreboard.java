package me.neznamy.tab.api;

import java.util.UUID;

public interface Scoreboard {

	public void sendTo(UUID player);
	public void removeFrom(UUID player);
	public void unregister();
	public void refresh();
}
