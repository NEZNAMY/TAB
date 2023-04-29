package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@RequiredArgsConstructor
public class FabricTabList implements TabList {

    private final FabricTabPlayer player;

    @Override
    public void removeEntry(@NonNull UUID entry) {
        player.sendPacket(FabricMultiVersion.build(Action.REMOVE_PLAYER, new Builder(entry)));
    }

    @Override
    public void updateDisplayName(@NonNull UUID id, @Nullable IChatBaseComponent displayName) {
        player.sendPacket(FabricMultiVersion.build(Action.UPDATE_DISPLAY_NAME,
                        new Builder(id).setDisplayName(displayName == null ? null : FabricTAB.getInstance().toComponent(displayName, player.getVersion()))));
    }

    @Override
    public void updateLatency(@NonNull UUID id, int latency) {
        player.sendPacket(FabricMultiVersion.build(Action.UPDATE_LATENCY, new Builder(id).setLatency(latency)));
    }

    @Override
    public void updateGameMode(@NonNull UUID id, int gameMode) {
        player.sendPacket(FabricMultiVersion.build(Action.UPDATE_GAME_MODE, new Builder(id).setGameMode(gameMode)));
    }

    @Override
    public void addEntry(@NonNull Entry entry) {
        player.sendPacket(FabricMultiVersion.build(Action.ADD_PLAYER, new Builder(entry.getUniqueId())
                .setName(entry.getName())
                .setSkin(entry.getSkin())
                .setGameMode(entry.getGameMode())
                .setLatency(entry.getLatency())
                .setDisplayName(entry.getDisplayName() == null ? null : FabricTAB.getInstance().toComponent(entry.getDisplayName(), player.getVersion()))));
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull IChatBaseComponent header, @NonNull IChatBaseComponent footer) {
        player.getPlayer().connection.send(FabricMultiVersion.setHeaderAndFooter.apply(
                FabricTAB.getInstance().toComponent(header, player.getVersion()),
                FabricTAB.getInstance().toComponent(footer, player.getVersion()))
        );
    }

    @RequiredArgsConstructor
    @Getter
    public static class Builder {

        @NonNull private final UUID id;
        @Nullable private String name;
        @Nullable private Skin skin;
        private int latency;
        private int gameMode;
        @Nullable private Component displayName;

        public Builder setName(String name) { this.name = name; return this; }
        public Builder setSkin(Skin skin) { this.skin = skin; return this; }
        public Builder setLatency(int latency) { this.latency = latency; return this; }
        public Builder setGameMode(int gameMode) { this.gameMode = gameMode; return this; }
        public Builder setDisplayName(Component displayName) { this.displayName = displayName; return this; }
        public GameProfile createProfile() {
            GameProfile profile = new GameProfile(id, name);
            if (skin != null) {
                profile.getProperties().put(TabList.TEXTURES_PROPERTY,
                        new Property(TabList.TEXTURES_PROPERTY, skin.getValue(), skin.getSignature()));
            }
            return profile;
        }
    }

    public enum Action {

        ADD_PLAYER, REMOVE_PLAYER, UPDATE_DISPLAY_NAME, UPDATE_LATENCY, UPDATE_GAME_MODE
    }
}
