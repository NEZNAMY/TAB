package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record FabricTabList(@NotNull FabricTabPlayer player) implements TabList {

    @Override
    public void removeEntry(@NotNull UUID entry) {
        player.sendPacket(FabricMultiVersion.build(Action.REMOVE_PLAYER, new Builder(entry)));
    }

    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        player.sendPacket(FabricMultiVersion.build(Action.UPDATE_DISPLAY_NAME,
                new Builder(entry).setDisplayName(displayName == null ? null : player.getPlatform().toComponent(displayName, player.getVersion()))
        ));
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        player.sendPacket(FabricMultiVersion.build(Action.UPDATE_LATENCY, new Builder(entry).setLatency(latency)));
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        player.sendPacket(FabricMultiVersion.build(Action.UPDATE_GAME_MODE, new Builder(entry).setGameMode(gameMode)));
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
        player.sendPacket(FabricMultiVersion.build(Action.ADD_PLAYER, new Builder(entry.getUniqueId())
                .setName(entry.getName())
                .setSkin(entry.getSkin())
                .setGameMode(entry.getGameMode())
                .setLatency(entry.getLatency())
                .setDisplayName(entry.getDisplayName() == null ? null : player.getPlatform().toComponent(entry.getDisplayName(), player.getVersion()))
        ));
    }

    @Override
    public void setPlayerListHeaderFooter(@NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        player.getPlayer().connection.send(FabricMultiVersion.setHeaderFooter(
                player.getPlatform().toComponent(header, player.getVersion()),
                player.getPlatform().toComponent(footer, player.getVersion()))
        );
    }

    @RequiredArgsConstructor
    @Getter
    public static class Builder {

        @NotNull private final UUID id;
        @NotNull private String name = ""; // Avoid nullability issues as things are changing over versions
        @Nullable private Skin skin;
        private int latency;
        private int gameMode;
        @Nullable private Component displayName;

        @NotNull
        public Builder setName(@NotNull String name) {
            this.name = name;
            return this;
        }

        @NotNull
        public Builder setSkin(@Nullable Skin skin) {
            this.skin = skin;
            return this;
        }

        @NotNull
        public Builder setLatency(int latency) {
            this.latency = latency;
            return this;
        }

        @NotNull
        public Builder setGameMode(int gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        @NotNull
        public Builder setDisplayName(@Nullable Component displayName) {
            this.displayName = displayName;
            return this;
        }

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
