package me.neznamy.tab.platforms.bukkit.features;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOut;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherItem;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.RawPacketFeature;

/**
 * A feature to disable minecraft 1.9+ feature making tamed animals with custom names copy nametag properties of their owner
 * This is achieved by listening to entity spawn (<1.15) / entity metadata packets and removing owner field from the datawatcher list
 */
public class PetFix implements RawPacketFeature, QuitEventListener {

	//datawatcher position of pet owner field
	private static int PET_OWNER_POSITION;
	
	//used NMS classes and fields
	private static Class<?> PacketPlayOutEntityMetadata;
	private static Class<?> PacketPlayInUseEntity;
	private static Field PacketPlayOutEntityMetadata_LIST;
	private static Field PacketPlayInUseEntity_ACTION;
	
	//logger of last interacts to prevent feature not working on 1.16
	private Map<String, Long> lastInteractFix = new HashMap<String, Long>();
	
	/**
	 * Initializes required NMS classes and fields
	 * @throws Exception - if something fails
	 */
	public static void initializeClass() throws Exception {
		PacketPlayOutEntityMetadata = PacketPlayOut.getNMSClass("PacketPlayOutEntityMetadata");
		(PacketPlayOutEntityMetadata_LIST = PacketPlayOutEntityMetadata.getDeclaredField("b")).setAccessible(true);
		PacketPlayInUseEntity = PacketPlayOut.getNMSClass("PacketPlayInUseEntity");
		(PacketPlayInUseEntity_ACTION = PacketPlayInUseEntity.getDeclaredField("action")).setAccessible(true);
		PET_OWNER_POSITION = getPetOwnerPosition();
	}
	
	/**
	 * Returns position of pet owner field based on server version
	 * @return position of pet owner field based on server version
	 */
	private static int getPetOwnerPosition() {
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
	
	/**
	 * Cancels a packet if previous one arrived with no delay to prevent double toggle on 1.16
	 */
	@Override
	public Object onPacketReceive(TabPlayer sender, Object packet) throws Throwable {
		if (PacketPlayInUseEntity.isInstance(packet)) {
			if (lastInteractFix.containsKey(sender.getName()) && (System.currentTimeMillis() - lastInteractFix.get(sender.getName()) < 5)) {
				//last interact packet was sent right now, cancelling to prevent double-toggle due to this feature enabled
				return null;
			}
			if (PacketPlayInUseEntity_ACTION.get(packet).toString().equals("INTERACT")) {
				//this is the first packet, saving player so the next packet can be cancelled
				lastInteractFix.put(sender.getName(), System.currentTimeMillis());
			}
		}
		return packet;
	}
	
	/**
	 * Removes pet owner field from datawatcher
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onPacketSend(TabPlayer receiver, Object packet) throws Throwable {
		if (PacketPlayOutEntityMetadata.isInstance(packet)) {
			List<Object> items = (List<Object>) PacketPlayOutEntityMetadata_LIST.get(packet);
			if (items == null) return;
			List<Object> newList = new ArrayList<Object>();
			for (Object item : items) {
				DataWatcherItem i = DataWatcherItem.fromNMS(item);
				if (i.type.position == PET_OWNER_POSITION && i.value.toString().startsWith("Optional")) continue;
				newList.add(i.toNMS());
			}
			PacketPlayOutEntityMetadata_LIST.set(packet, newList);
		}
		if (PacketPlayOutSpawnEntityLiving.PacketPlayOutSpawnEntityLiving.isInstance(packet) && PacketPlayOutSpawnEntityLiving.DATAWATCHER != null) {
			DataWatcher watcher = DataWatcher.fromNMS(PacketPlayOutSpawnEntityLiving.DATAWATCHER.get(packet));
			DataWatcherItem petOwner = watcher.getItem(PET_OWNER_POSITION);
			if (petOwner != null && petOwner.value.toString().startsWith("Optional")) {
				watcher.removeValue(PET_OWNER_POSITION);
			}
			PacketPlayOutSpawnEntityLiving.DATAWATCHER.set(packet, watcher.toNMS());
		}
	}

	/**
	 * Returns name of the feature displayed in /tab cpu
	 * @return name of the feature displayed in /tab cpu
	 */
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.PET_NAME_FIX;
	}

	/**
	 * Removing player data to not have a memory leak
	 */
	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		lastInteractFix.remove(disconnectedPlayer.getName());
	}
}