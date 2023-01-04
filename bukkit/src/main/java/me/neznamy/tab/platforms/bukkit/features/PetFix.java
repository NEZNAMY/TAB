package me.neznamy.tab.platforms.bukkit.features;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.storage.NMSStorage;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherItem;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * A feature to disable minecraft 1.9+ feature making tamed animals
 * with custom names copy NameTag properties of their owner.
 * This is achieved by listening to entity spawn (&lt;1.15) / entity metadata
 * packets and removing owner field from the DataWatcher list.
 * Since 1.16 this results in client sending entity use packet twice,
 * so we must cancel the 2nd one to prevent double toggle.
 */
public class PetFix extends TabFeature {

    /** NMS Storage reference for quick access */
    private final NMSStorage nms = NMSStorage.getInstance();

    /** DataWatcher position of pet owner field */
    private final int petOwnerPosition = getPetOwnerPosition();

    /** Logger of last interacts to prevent feature not working on 1.16 */
    private final WeakHashMap<TabPlayer, Long> lastInteractFix = new WeakHashMap<>();

    /**
     * Since 1.16, client sends interact packet twice for entities affected
     * by removed owner field. Because of that, we need to cancel the duplicated
     * packet to avoid double toggle and preventing the entity from getting
     * its pose changed. The duplicated packet is usually sent instantly in the
     * same millisecond, however, when installing ProtocolLib with MyPet, the delay
     * is up to 3 ticks. When holding right-click on an entity, interact is sent every
     * 200 milliseconds, which is the value we should not go above. Optimal value is
     * therefore between 150 and 200 milliseconds.
     */
    private static final int INTERACT_COOLDOWN = 160;

    /**
     * Constructs new instance with given parameter
     */
    public PetFix() {
        super("Pet name fix", null);
        TabAPI.getInstance().debug("Loaded PetFix feature");
    }

    /**
     * Returns position of pet owner field based on server version
     *
     * @return  position of pet owner field based on server version
     */
    private int getPetOwnerPosition() {
        if (nms.getMinorVersion() >= 17) {
            //1.17.x, 1.18.x, 1.19.x
            return 18;
        } else if (nms.getMinorVersion() >= 15) {
            //1.15.x, 1.16.x
            return 17;
        } else if (nms.getMinorVersion() >= 14) {
            //1.14.x
            return 16;
        } else if (nms.getMinorVersion() >= 10) {
            //1.10.x - 1.13.x
            return 14;
        } else {
            //1.9.x
            return 13;
        }
    }

    /**
     * Cancels a packet if previous one arrived with no delay to prevent double toggle since 1.16
     *
     * @throws  ReflectiveOperationException
     *          if thrown by reflective operation
     */
    @Override
    public boolean onPacketReceive(TabPlayer sender, Object packet) throws ReflectiveOperationException {
        if (nms.PacketPlayInUseEntity.isInstance(packet) && isInteract(nms.PacketPlayInUseEntity_ACTION.get(packet))) {
            if (System.currentTimeMillis() - lastInteractFix.getOrDefault(sender, 0L) < INTERACT_COOLDOWN) {
                //last interact packet was sent right now, cancelling to prevent double-toggle due to this feature enabled
                return true;
            }
            //this is the first packet, saving player so the next packet can be cancelled
            lastInteractFix.put(sender, System.currentTimeMillis());
        }
        return false;
    }

    /**
     * Checks if the provided entity use action is INTERACT or not.
     *
     * @param   action
     *          Action to check
     * @return {@code true} if action is INTERACT, {@code false} if not.
     */
    private boolean isInteract(Object action) {
        if (nms.getMinorVersion() >= 17) {
            return nms.PacketPlayInUseEntity$d.isInstance(action);
        } else {
            return action.toString().equals("INTERACT");
        }
    }

    /**
     * Removes pet owner field from DataWatcher
     *
     * @throws  ReflectiveOperationException
     *          if thrown by reflective operation
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onPacketSend(TabPlayer receiver, Object packet) throws ReflectiveOperationException {
        if (nms.PacketPlayOutEntityMetadata.isInstance(packet)) {
            Object removedEntry = null;
            List<Object> items = (List<Object>) nms.PacketPlayOutEntityMetadata_LIST.get(packet);
            if (items == null) return;
            try {
                for (Object item : items) {
                    if (item == null) continue;
                    int slot;
                    Object value;
                    if (nms.is1_19_3Plus()) {
                        slot = nms.DataWatcher$DataValue_POSITION.getInt(item);
                        value = nms.DataWatcher$DataValue_VALUE.get(item);
                    } else {
                        slot = nms.DataWatcherObject_SLOT.getInt(nms.DataWatcherItem_TYPE.get(item));
                        value = nms.DataWatcherItem_VALUE.get(item);
                    }
                    if (slot == petOwnerPosition) {
                        if (value instanceof Optional || value instanceof com.google.common.base.Optional) {
                            removedEntry = item;
                        }
                    }
                }
            } catch (ConcurrentModificationException e) {
                //no idea how can this list change in another thread since it's created for the packet but whatever, try again
                onPacketSend(receiver, packet);
            }
            if (removedEntry != null) items.remove(removedEntry);
        } else if (nms.PacketPlayOutSpawnEntityLiving.isInstance(packet) && nms.PacketPlayOutSpawnEntityLiving_DATAWATCHER != null) {
            //<1.15
            DataWatcher watcher = DataWatcher.fromNMS(nms.PacketPlayOutSpawnEntityLiving_DATAWATCHER.get(packet));
            DataWatcherItem petOwner = watcher.getItem(petOwnerPosition);
            if (petOwner != null && (petOwner.getValue() instanceof Optional || petOwner.getValue() instanceof com.google.common.base.Optional)) {
                watcher.removeValue(petOwnerPosition);
                nms.setField(packet, nms.PacketPlayOutSpawnEntityLiving_DATAWATCHER, watcher.toNMS());
            }
        }
    }
}