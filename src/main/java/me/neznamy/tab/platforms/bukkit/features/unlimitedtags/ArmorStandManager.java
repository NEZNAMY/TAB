package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.shared.ITabPlayer;

public class ArmorStandManager {

	private Map<String, ArmorStand> armorStands = new LinkedHashMap<String, ArmorStand>();

	public void addArmorStand(String name, ArmorStand as) {
		armorStands.put(name, as);
	}
	public void spawn(ITabPlayer viewer) {
		if (viewer.getVersion().getMinorVersion() < 8) return;
		for (ArmorStand as : getArmorStands()) {
			for (PacketPlayOut packet : as.getSpawnPackets(viewer, true)) {
				viewer.sendCustomBukkitPacket(packet);
			}
		}
	}

	public void sneak(boolean sneaking) {
		getArmorStands().forEach(a -> a.sneak(sneaking));
	}

	public void teleport() {
		getArmorStands().forEach(a -> a.teleport());
	}

	public void teleport(ITabPlayer viewer) {
		getArmorStands().forEach(a -> viewer.sendCustomBukkitPacket(a.getTeleportPacket(viewer)));
	}

	public void refresh() {
		getArmorStands().forEach(a -> a.refresh());
	}

	public void updateVisibility() {
		getArmorStands().forEach(a -> a.updateVisibility());
	}

	public void unregisterPlayer(ITabPlayer viewer) {
		getArmorStands().forEach(a -> a.removeFromRegistered(viewer));
	}

	public void destroy() {
		getArmorStands().forEach(a -> a.destroy());
	}

	public void destroy(ITabPlayer viewer) {
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