package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * TabList implementation for Fabric using packets.
 */
@RequiredArgsConstructor
public class FabricTabList implements TabList {

    /** Player this tablist belongs to */
    @NonNull
    private final FabricTabPlayer player;

    @Override
    @SneakyThrows
    public void removeEntry(@NonNull UUID entry) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket.apply(Action.UPDATE_DISPLAY_NAME, new Builder(entry)));
    }

    @Override
    @SneakyThrows
    public void updateDisplayName(@NonNull UUID entry, @Nullable TabComponent displayName) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket.apply(Action.UPDATE_DISPLAY_NAME,
                new Builder(entry).setDisplayName(displayName == null ? null : player.getPlatform().toComponent(displayName, player.getVersion()))
        ));
    }

    @Override
    @SneakyThrows
    public void updateLatency(@NonNull UUID entry, int latency) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket.apply(Action.UPDATE_LATENCY,
                new Builder(entry).setLatency(latency)));
    }

    @Override
    @SneakyThrows
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket.apply(Action.UPDATE_GAME_MODE,
                new Builder(entry).setGameMode(gameMode)));
    }

    @Override
    @SneakyThrows
    public void addEntry(@NonNull Entry entry) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket.apply(Action.ADD_PLAYER,
                new Builder(entry.getUniqueId())
                .setName(entry.getName())
                .setSkin(entry.getSkin())
                .setGameMode(entry.getGameMode())
                .setLatency(entry.getLatency())
                .setDisplayName(entry.getDisplayName() == null ? null : player.getPlatform().toComponent(entry.getDisplayName(), player.getVersion()))
        ));

        if (player.getVersion().getMinorVersion() == 8) {
            // Compensation for 1.8.0 client sided bug
            updateDisplayName(entry.getUniqueId(), entry.getDisplayName());
        }
    }

    @Override
    @SneakyThrows
    public void setPlayerListHeaderFooter(@NonNull TabComponent header, @NonNull TabComponent footer) {
        player.sendPacket(FabricMultiVersion.newHeaderFooter.apply(
                player.getPlatform().toComponent(header, player.getVersion()),
                player.getPlatform().toComponent(footer, player.getVersion())
        ));
    }

    @Override
    @SneakyThrows
    public void onPacketSend(@NonNull Object packet) {
        if (FabricMultiVersion.isPlayerInfo.apply((Packet<?>) packet)) {
            FabricMultiVersion.onPlayerInfo.accept(player, packet);
        }
    }

    /**
     * TabList entry builder.
     */
    @RequiredArgsConstructor
    @Getter
    public static class Builder {

        @NonNull private final UUID id;
        @NonNull private String name = ""; // Avoid nullability issues as things are changing over versions
        @Nullable private Skin skin;
        private int latency;
        private int gameMode;
        @Nullable private Component displayName;

        /**
         * Sets entry name.
         *
         * @param   name
         *          Name to use
         * @return  this
         */
        @NonNull
        public Builder setName(@NonNull String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets entry skin.
         *
         * @param   skin
         *          Skin to use
         * @return  this
         */
        @NonNull
        public Builder setSkin(@Nullable Skin skin) {
            this.skin = skin;
            return this;
        }

        /**
         * Sets entry latency.
         *
         * @param   latency
         *          Latency to use
         * @return  this
         */
        @NonNull
        public Builder setLatency(int latency) {
            this.latency = latency;
            return this;
        }

        /**
         * Sets entry gamemode.
         *
         * @param   gameMode
         *          gamemode to use
         * @return  this
         */
        @NonNull
        public Builder setGameMode(int gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        /**
         * Sets entry display name.
         *
         * @param   displayName
         *          Display name to use
         * @return  this
         */
        @NonNull
        public Builder setDisplayName(@Nullable Component displayName) {
            this.displayName = displayName;
            return this;
        }

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
