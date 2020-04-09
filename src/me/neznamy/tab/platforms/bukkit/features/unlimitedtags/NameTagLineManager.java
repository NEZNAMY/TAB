package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.ProtocolVersion;

public class NameTagLineManager {

	public static void spawnArmorStand(ITabPlayer armorStandOwner, ITabPlayer packetReceiver) {
		for (ArmorStand as : armorStandOwner.getArmorStands().toArray(new ArmorStand[0])) {
			packetReceiver.sendCustomBukkitPacket(as.getSpawnPacket(packetReceiver, true));
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 15) {
				String displayName = as.property.hasRelationalPlaceholders() ? PluginHooks.PlaceholderAPI_setRelationalPlaceholders(armorStandOwner, packetReceiver, as.property.get()) : as.property.get();
				packetReceiver.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(as.getEntityId(), as.createDataWatcher(displayName, packetReceiver).toNMS(), true));
			}
		}
	}
}