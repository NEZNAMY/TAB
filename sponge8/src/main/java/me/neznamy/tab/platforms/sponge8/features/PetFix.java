package me.neznamy.tab.platforms.sponge8.features;

import lombok.Getter;
import me.neznamy.tab.api.feature.PacketReceiveListener;
import me.neznamy.tab.api.feature.PacketSendListener;
import me.neznamy.tab.api.feature.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.sponge8.nms.NMSStorage;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.syncher.SynchedEntityData;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;

public class PetFix extends TabFeature implements PacketReceiveListener, PacketSendListener {

    /** NMS Storage reference for quick access */
    private final NMSStorage nms = NMSStorage.getInstance();

    /** DataWatcher position of pet owner field */
    private static final int petOwnerPosition = 17;

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

    @Getter private final String featureName = "Pet name fix";

    /**
     * Cancels a packet if previous one arrived with no delay to prevent double toggle since 1.16
     *
     */
    @Override
    public boolean onPacketReceive(TabPlayer sender, Object packet) {
        if (packet instanceof ServerboundInteractPacket && ((ServerboundInteractPacket) packet).getAction() == ServerboundInteractPacket.Action.INTERACT) {
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
     * Removes pet owner field from DataWatcher
     *
     * @throws  ReflectiveOperationException
     *          if thrown by reflective operation
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onPacketSend(TabPlayer receiver, Object packet) throws ReflectiveOperationException {
        if (packet instanceof ClientboundSetEntityDataPacket) {
            Object removedEntry = null;
            List<SynchedEntityData.DataItem<?>> items = (List<SynchedEntityData.DataItem<?>>) nms.ClientboundSetEntityDataPacket_data.get(packet);
            if (items == null) return;
            try {
                for (SynchedEntityData.DataItem<?> item : items) {
                    if (item == null) continue;
                    if (item.getAccessor().getId() == petOwnerPosition) {
                        if (item.getValue() instanceof Optional || item.getValue() instanceof com.google.common.base.Optional) {
                            removedEntry = item;
                        }
                    }
                }
            } catch (ConcurrentModificationException e) {
                //no idea how can this list change in another thread since it's created for the packet but whatever, try again
                onPacketSend(receiver, packet);
            }
            if (removedEntry != null) items.remove(removedEntry);
        }
    }
}
