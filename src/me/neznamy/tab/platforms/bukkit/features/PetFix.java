package me.neznamy.tab.platforms.bukkit.features;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.platforms.bukkit.packets.DataWatcher;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcher.Item;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.RawPacketFeature;

public class PetFix implements RawPacketFeature{

	private final int PET_OWNER_POSITION = getPetOwnerPosition();
	private final Field PacketPlayOutEntityMetadata_LIST;
	
	private int getPetOwnerPosition() {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 15) {
			//1.15.x, 1.16.1
			return 17;
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 14) {
			//1.14.x
			return 16;
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 10) {
			//1.10.x - 1.13.x
			return 14;
		} else {
			//1.9.x
			return 13;
		}
	}
	
	public PetFix() {
		PacketPlayOutEntityMetadata_LIST = PacketPlayOut.getFields(MethodAPI.PacketPlayOutEntityMetadata).get("b");
	}
	
	@Override
	public Object onPacketReceive(ITabPlayer sender, Object packet) throws Throwable {
		return packet;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object onPacketSend(ITabPlayer receiver, Object packet) throws Throwable{
		//on 1.16 server cats and parrots do not listen to sit/stand commands, but dogs do
		//disabling the feature until the issue is resolved
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 16) return packet;
		
		if (MethodAPI.PacketPlayOutEntityMetadata.isInstance(packet)) {
			List<Object> items = (List<Object>) PacketPlayOutEntityMetadata_LIST.get(packet);
			if (items == null) return packet;
			List<Object> newList = new ArrayList<Object>();
			for (Object item : items) {
				Item i = Item.fromNMS(item);
				if (i.type.position != PET_OWNER_POSITION) newList.add(i.toNMS());
			}
			PacketPlayOutEntityMetadata_LIST.set(packet, newList);
		}
		if (MethodAPI.PacketPlayOutSpawnEntityLiving.isInstance(packet) && PacketPlayOutSpawnEntityLiving.DATAWATCHER != null) {
			DataWatcher watcher = DataWatcher.fromNMS(PacketPlayOutSpawnEntityLiving.DATAWATCHER.get(packet));
			watcher.removeValue(PET_OWNER_POSITION);
			PacketPlayOutSpawnEntityLiving.DATAWATCHER.set(packet, watcher.toNMS());
		}
		return packet;
	}
	
	@Override
	public CPUFeature getCPUName() {
		return CPUFeature.PET_NAME_FIX;
	}
}