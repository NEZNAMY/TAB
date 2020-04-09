package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import org.bukkit.Location;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.ProtocolVersion;

public class NameTagLineManager {

	public static ArmorStand bindLine(ITabPlayer p, String text, double heightDifference, String ID, boolean staticOffset){
		ArmorStand as = new ArmorStand(p, text, heightDifference, ID, staticOffset);
		p.armorStands.add(as);
		return as;
	}
	public static void spawnArmorStand(ITabPlayer armorStandOwner, ITabPlayer packetReceiver, boolean addToRegistered) {
		for (ArmorStand as : armorStandOwner.getArmorStands().toArray(new ArmorStand[0])) {
			packetReceiver.sendCustomBukkitPacket(as.getSpawnPacket(packetReceiver, addToRegistered));
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 15) {
				String displayName = as.property.hasRelationalPlaceholders() ? PluginHooks.PlaceholderAPI_setRelationalPlaceholders(armorStandOwner, packetReceiver, as.property.get()) : as.property.get();
				packetReceiver.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(as.getEntityId(), as.createDataWatcher(displayName, packetReceiver).toNMS(), true));
			}
		}
	}
	public static void teleportArmorStand(ITabPlayer armorStandOwner, Location newLocation) {
		for (ArmorStand as : armorStandOwner.getArmorStands()) {
			as.updateLocation(newLocation);
			for (ITabPlayer nearby : as.getNearbyUsers()) {
				nearby.sendPacket(as.getNMSTeleportPacket(nearby));
			}
		}
	}
}