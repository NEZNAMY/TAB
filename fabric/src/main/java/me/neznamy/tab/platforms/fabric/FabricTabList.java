package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.*;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * TabList implementation for Fabric using packets.
 */
public class FabricTabList extends TabList<FabricTabPlayer, Component> {

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
                new Builder(entry, "", null, 0, 0, null)));
    }

    @Override
    public void updateDisplayName0(@NonNull UUID entry, @Nullable Component displayName) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket(Action.UPDATE_DISPLAY_NAME,
                new Builder(entry, "", null, 0, 0, displayName)));
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket(Action.UPDATE_LATENCY,
                new Builder(entry, "", null, latency, 0, null)));
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket(Action.UPDATE_GAME_MODE,
                new Builder(entry, "", null, 0, gameMode, null)));
    }

    @Override
    public void addEntry0(@NonNull UUID id, @NonNull String name, @Nullable Skin skin, int latency, int gameMode, @Nullable Component displayName) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket(Action.ADD_PLAYER,
                new Builder(id, name, skin, latency, gameMode, displayName)));
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

    @Override
    public Component toComponent(@NonNull TabComponent component) {
        return player.getPlatform().toComponent(component, player.getVersion());
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
        private int latency;
        private int gameMode;
        @Nullable private Component displayName;

        /**
         * Creates profile of this entry.
         *
         * @return  Profile of this entry
         */
        @NonNull
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
