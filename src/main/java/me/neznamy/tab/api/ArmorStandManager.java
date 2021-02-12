package me.neznamy.tab.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A helper class for easy management of armor stands of a player
 */
public class ArmorStandManager {

	private Map<String, ArmorStand> armorStands = new LinkedHashMap<String, ArmorStand>();

	public void addArmorStand(String name, ArmorStand as) {
		armorStands.put(name, as);
	}
	
	public void removeArmorStand(String name) {
		armorStands.remove(name);
	}
	
	public void spawn(TabPlayer viewer) {
		if (viewer.getVersion().getMinorVersion() < 8) return;
		getArmorStands().forEach(a -> a.spawn(viewer));
	}

	public void sneak(boolean sneaking) {
		getArmorStands().forEach(a -> a.sneak(sneaking));
	}

	public void teleport() {
		getArmorStands().forEach(a -> a.teleport());
	}

	public void teleport(TabPlayer viewer) {
		getArmorStands().forEach(a -> a.teleport(viewer));
	}

	public void refresh() {
		getArmorStands().forEach(a -> a.refresh());
	}

	public void updateVisibility(boolean force) {
		getArmorStands().forEach(a -> a.updateVisibility(force));
	}

	public void unregisterPlayer(TabPlayer viewer) {
		getArmorStands().forEach(a -> a.removeFromRegistered(viewer));
	}

	public void destroy() {
		getArmorStands().forEach(a -> a.destroy());
	}

	public void destroy(TabPlayer viewer) {
		getArmorStands().forEach(a -> a.destroy(viewer));
	}

	public boolean hasArmorStandWithID(int entityId) {
		for (ArmorStand as : getArmorStands()) {
			if (as.getEntityId() == entityId) {
				return true;
			}
		}
		return false;
	}
	
	public Collection<ArmorStand> getArmorStands(){
		return new ArrayList<>(armorStands.values());
	}
}