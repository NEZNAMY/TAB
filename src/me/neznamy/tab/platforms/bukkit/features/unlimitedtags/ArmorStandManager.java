package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.shared.ITabPlayer;

public class ArmorStandManager {

	private Map<String, ArmorStand> armorStands = new ConcurrentHashMap<String, ArmorStand>();

	public void addArmorStand(String name, ArmorStand as) {
		armorStands.put(name, as);
	}
	public void spawn(ITabPlayer viewer) {
		if (viewer.getVersion().getMinorVersion() < 8) return;
		for (ArmorStand as : armorStands.values()) {
			for (PacketPlayOut packet : as.getSpawnPackets(viewer, true)) {
				viewer.sendCustomBukkitPacket(packet);
			}
		}
	}

	public void sneak(boolean sneaking) {
		armorStands.values().forEach(a -> a.sneak(sneaking));
	}

	public void teleport() {
		armorStands.values().forEach(a -> a.teleport());
	}

	public void teleport(ITabPlayer viewer) {
		armorStands.values().forEach(a -> viewer.sendCustomBukkitPacket(a.getTeleportPacket(viewer)));
	}

	public void refresh() {
		armorStands.values().forEach(a -> a.refresh());
	}

	public void updateVisibility() {
		armorStands.values().forEach(a -> a.updateVisibility());
	}

	public void unregisterPlayer(ITabPlayer viewer) {
		armorStands.values().forEach(a -> a.removeFromRegistered(viewer));
	}

	public void destroy() {
		armorStands.values().forEach(a -> a.destroy());
	}

	public void destroy(ITabPlayer viewer) {
		armorStands.values().forEach(a -> a.destroy(viewer));
	}

	public boolean hasArmorStandWithID(int entityId) {
		for (ArmorStand as : armorStands.values()) {
			if (as.getEntityId() == entityId) {
				return true;
			}
		}
		return false;
	}
	public Collection<ArmorStand> getArmorStands(){
		return armorStands.values();
	}
}