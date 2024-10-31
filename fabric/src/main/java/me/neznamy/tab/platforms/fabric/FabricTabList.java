package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.*;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * TabList implementation for Fabric using packets.
 */
public class FabricTabList extends TrackedTabList<FabricTabPlayer, Component> {

    /**
     * Constructs new instance.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    public FabricTabList(@NotNull FabricTabPlayer player) {
        super(player);
    }

    @Override
    public void removeEntry(@NonNull UUID entry) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket(Action.REMOVE_PLAYER,
                new Builder(entry, "", null, false, 0, 0, null, 0, false)));
    }

    @Override
    public void updateDisplayName(@NonNull UUID entry, @Nullable Component displayName) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket(Action.UPDATE_DISPLAY_NAME,
                new Builder(entry, "", null, false, 0, 0, displayName, 0, false)));
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket(Action.UPDATE_LATENCY,
                new Builder(entry, "", null, false, latency, 0, null, 0, false)));
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket(Action.UPDATE_GAME_MODE,
                new Builder(entry, "", null, false, 0, gameMode, null, 0, false)));
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        if (player.getPlatform().getServerVersion().getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) {
            player.sendPacket(FabricMultiVersion.buildTabListPacket(Action.UPDATE_LISTED,
                    new Builder(entry, "", null, listed, 0, 0, null, 0, false)));
        }
    }

    @Override
    public void updateListOrder(@NonNull UUID entry, int listOrder) {
        if (player.getPlatform().getServerVersion().getNetworkId() >= ProtocolVersion.V1_21_2.getNetworkId()) {
            player.sendPacket(FabricMultiVersion.buildTabListPacket(Action.UPDATE_LIST_ORDER,
                    new Builder(entry, "", null, false, 0, 0, null, listOrder, false)));
        }
    }

    @Override
    public void updateHat(@NonNull UUID entry, boolean showHat) {
        if (player.getPlatform().getServerVersion().getNetworkId() >= ProtocolVersion.V1_21_4.getNetworkId()) {
            player.sendPacket(FabricMultiVersion.buildTabListPacket(Action.UPDATE_HAT,
                    new Builder(entry, "", null, false, 0, 0, null, 0, showHat)));
        }
    }

    @Override
    public void addEntry(@NonNull UUID id, @NonNull String name, @Nullable Skin skin, boolean listed, int latency,
                         int gameMode, @Nullable Component displayName, int listOrder, boolean showHat) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket(Action.ADD_PLAYER,
                new Builder(id, name, skin, listed, latency, gameMode, displayName, listOrder, showHat)));
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull TabComponent header, @NonNull TabComponent footer) {
        player.sendPacket(FabricMultiVersion.newHeaderFooter(toComponent(header), toComponent(footer)));
    }

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return true; // TODO?
    }

    @Override
    public void onPacketSend(@NonNull Object packet) {
        if (FabricMultiVersion.isPlayerInfo((Packet<?>) packet)) {
            FabricMultiVersion.onPlayerInfo(player, packet);
        }
    }

    /**
     * TabList entry builder.
     */
    @AllArgsConstructor
    @Getter
    public static class Builder {

        @NonNull private final UUID id;
        @NonNull private String name;
        @Nullable private Skin skin;
        private boolean listed;
        private int latency;
        private int gameMode;
        @Nullable private Component displayName;
        private int listOrder;
        private boolean showHat;

        /**
         * Creates profile of this entry.
         *
         * @return  Profile of this entry
         */
        @NotNull
        public GameProfile createProfile() {
            GameProfile profile = new GameProfile(id, name);
            if (skin != null) {
                profile.getProperties().put(TabList.TEXTURES_PROPERTY,
                        new Property(TabList.TEXTURES_PROPERTY, skin.getValue(), skin.getSignature()));
            }
            return profile;
        }
    }
}
