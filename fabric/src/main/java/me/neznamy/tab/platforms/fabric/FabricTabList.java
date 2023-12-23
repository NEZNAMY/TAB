package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * TabList implementation for Fabric using packets.
 */
@RequiredArgsConstructor
public class FabricTabList implements TabList {

    /** Player this tablist belongs to */
    @NotNull
    private final FabricTabPlayer player;

    @Override
    @SneakyThrows
    public void removeEntry(@NotNull UUID entry) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket.apply(Action.UPDATE_DISPLAY_NAME, new Builder(entry)));
    }

    @Override
    @SneakyThrows
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket.apply(Action.UPDATE_DISPLAY_NAME,
                new Builder(entry).setDisplayName(displayName == null ? null : player.getPlatform().toComponent(displayName, player.getVersion()))
        ));
    }

    @Override
    @SneakyThrows
    public void updateLatency(@NotNull UUID entry, int latency) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket.apply(Action.UPDATE_LATENCY,
                new Builder(entry).setLatency(latency)));
    }

    @Override
    @SneakyThrows
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket.apply(Action.UPDATE_GAME_MODE,
                new Builder(entry).setGameMode(gameMode)));
    }

    @Override
    @SneakyThrows
    public void addEntry(@NotNull Entry entry) {
        player.sendPacket(FabricMultiVersion.buildTabListPacket.apply(Action.ADD_PLAYER,
                new Builder(entry.getUniqueId())
                .setName(entry.getName())
                .setSkin(entry.getSkin())
                .setGameMode(entry.getGameMode())
                .setLatency(entry.getLatency())
                .setDisplayName(entry.getDisplayName() == null ? null : player.getPlatform().toComponent(entry.getDisplayName(), player.getVersion()))
        ));
    }

    @Override
    @SneakyThrows
    public void setPlayerListHeaderFooter(@NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        player.sendPacket(FabricMultiVersion.newHeaderFooter.apply(
                player.getPlatform().toComponent(header, player.getVersion()),
                player.getPlatform().toComponent(footer, player.getVersion())
        ));
    }

    @Override
    @SneakyThrows
    public void onPacketSend(@NotNull Object packet) {
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

        @NotNull private final UUID id;
        @NotNull private String name = ""; // Avoid nullability issues as things are changing over versions
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
        @NotNull
        public Builder setName(@NotNull String name) {
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
        @NotNull
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
        @NotNull
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
        @NotNull
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
        @NotNull
        public Builder setDisplayName(@Nullable Component displayName) {
            this.displayName = displayName;
            return this;
        }

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
