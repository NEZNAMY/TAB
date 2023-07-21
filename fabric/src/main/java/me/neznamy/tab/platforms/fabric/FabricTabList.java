package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record FabricTabList(@NotNull FabricTabPlayer player) implements TabList {

    @Override
    public void removeEntry(@NotNull UUID entry) {
        player.sendPacket(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(entry)));
    }

    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        player.sendPacket(build(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),
                new Builder(entry).setDisplayName(displayName == null ? null : FabricTAB.toComponent(displayName, player.getVersion()))));
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        player.sendPacket(build(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY), new Builder(entry).setLatency(latency)));
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        player.sendPacket(build(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE), new Builder(entry).setGameMode(gameMode)));
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
        player.sendPacket(build(EnumSet.allOf(ClientboundPlayerInfoUpdatePacket.Action.class), new Builder(entry.getUniqueId())
                .setName(entry.getName())
                .setSkin(entry.getSkin())
                .setGameMode(entry.getGameMode())
                .setLatency(entry.getLatency())
                .setDisplayName(entry.getDisplayName() == null ? null : FabricTAB.toComponent(entry.getDisplayName(), player.getVersion()))));
    }

    @Override
    public void setPlayerListHeaderFooter(@NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        player.getPlayer().connection.send(new ClientboundTabListPacket(
                FabricTAB.toComponent(header, player.getVersion()),
                FabricTAB.toComponent(footer, player.getVersion()))
        );
    }

    private Packet<?> build(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions, FabricTabList.Builder entry) {
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(actions, Collections.emptyList());
        packet.entries = Collections.singletonList(new ClientboundPlayerInfoUpdatePacket.Entry(
                entry.getId(),
                entry.createProfile(),
                true,
                entry.getLatency(),
                GameType.byId(entry.getGameMode()),
                entry.getDisplayName(),
                null
        ));
        return packet;
    }

    @RequiredArgsConstructor
    @Getter
    public static class Builder {

        @NotNull private final UUID id;
        @Nullable private String name;
        @Nullable private Skin skin;
        private int latency;
        private int gameMode;
        @Nullable private Component displayName;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setSkin(Skin skin) {
            this.skin = skin;
            return this;
        }

        public Builder setLatency(int latency) {
            this.latency = latency;
            return this;
        }

        public Builder setGameMode(int gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public Builder setDisplayName(Component displayName) {
            this.displayName = displayName;
            return this;
        }

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
