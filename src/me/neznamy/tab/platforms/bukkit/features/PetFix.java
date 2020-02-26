package me.neznamy.tab.platforms.bukkit.features;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.neznamy.tab.platforms.bukkit.packets.DataWatcher;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcher.Item;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.features.RawPacketFeature;

public class PetFix implements RawPacketFeature{

	@Override
	public Object onPacketReceive(ITabPlayer sender, Object packet) throws Throwable {
		return packet;
	}
	@SuppressWarnings("unchecked")
	@Override
	public Object onPacketSend(ITabPlayer receiver, Object packet) throws Throwable{
			if (MethodAPI.PacketPlayOutEntityMetadata.isInstance(packet)) {
				List<Object> items = (List<Object>) MethodAPI.PacketPlayOutEntityMetadata_LIST.get(packet);
				List<Object> newList = new ArrayList<Object>();
				for (Object item : items) {
					Item i = Item.fromNMS(item);
					if (i.type.position == ProtocolVersion.SERVER_VERSION.getPetOwnerPosition()) {
						modifyDataWatcherItem(i);
					}
					newList.add(i.toNMS());
				}
				MethodAPI.PacketPlayOutEntityMetadata_LIST.set(packet, newList);
			}
			if (MethodAPI.PacketPlayOutSpawnEntityLiving.isInstance(packet) && PacketPlayOutSpawnEntityLiving.DATAWATCHER != null) {
				DataWatcher watcher = DataWatcher.fromNMS(PacketPlayOutSpawnEntityLiving.DATAWATCHER.get(packet));
				Item petOwner = watcher.getItem(ProtocolVersion.SERVER_VERSION.getPetOwnerPosition());
				if (petOwner != null) modifyDataWatcherItem(petOwner);
				PacketPlayOutSpawnEntityLiving.DATAWATCHER.set(packet, watcher.toNMS());
			}
		return packet;
	}
	@SuppressWarnings({ "rawtypes" })
	private void modifyDataWatcherItem(Item petOwner) {
		//1.12-
		if (petOwner.value instanceof com.google.common.base.Optional) {
			com.google.common.base.Optional o = (com.google.common.base.Optional) petOwner.value;
			if (o.isPresent() && o.get() instanceof UUID) {
				petOwner.value = com.google.common.base.Optional.of(UUID.randomUUID());
			}
		}
		//1.13+
		if (petOwner.value instanceof java.util.Optional) {
			java.util.Optional o = (java.util.Optional) petOwner.value;
			if (o.isPresent() && o.get() instanceof UUID) {
				petOwner.value = java.util.Optional.of(UUID.randomUUID());
			}
		}
	}
	@Override
	public String getCPUName() {
		return "Pet name fix";
	}
}