package me.neznamy.tab.shared.features.interfaces;

import java.util.Set;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.cpu.CPUFeature;

/**
 * Classes implementing this interface will receive refresh call when a placeholder changes it's value
 */
public interface Refreshable {

	public void refresh(ITabPlayer refreshed, boolean force);
	public CPUFeature getRefreshCPU();
	public Set<String> getUsedPlaceholders();
	public void refreshUsedPlaceholders();
}
