package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.player.tablist.BulkUpdateTabList;
import me.neznamy.tab.shared.player.tablist.Skin;
import me.neznamy.tab.shared.player.tablist.TabList;
import me.neznamy.tab.shared.player.tablist.TabListEntry;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class FabricTabList extends BulkUpdateTabList {

    private final FabricTabPlayer player;

    @Override
    public void removeEntries(@NonNull Collection<UUID> entries) {
        player.sendPacket(FabricMultiVersion.build(Action.REMOVE_PLAYER, entries.stream()
                .map(Builder::new)
                .collect(Collectors.toList())));
    }

    @Override
    public void updateDisplayNames(@NonNull Map<UUID, IChatBaseComponent> entries) {
        player.sendPacket(FabricMultiVersion.build(Action.UPDATE_DISPLAY_NAME, entries.entrySet().stream()
                .map(entry -> new Builder(entry.getKey()).setDisplayName(FabricTAB.toComponent(entry.getValue(), player.getVersion())))
                .collect(Collectors.toList())));
    }

    @Override
    public void updateLatencies(@NonNull Map<UUID, Integer> entries) {
        player.sendPacket(FabricMultiVersion.build(Action.UPDATE_LATENCY, entries.entrySet().stream()
                .map(entry -> new Builder(entry.getKey()).setLatency(entry.getValue()))
                .collect(Collectors.toList())));
    }

    @Override
    public void updateGameModes(@NonNull Map<UUID, Integer> entries) {
        player.sendPacket(FabricMultiVersion.build(Action.UPDATE_GAME_MODE, entries.entrySet().stream()
                .map(entry -> new Builder(entry.getKey()).setGameMode(entry.getValue()))
                .collect(Collectors.toList())));
    }

    @Override
    public void addEntries(@NonNull Collection<TabListEntry> entries) {
        List<Builder> converted = new ArrayList<>();
        for (TabListEntry entry : entries) {
            converted.add(new Builder(entry.getUniqueId())
                    .setName(entry.getName())
                    .setGameMode(entry.getGameMode())
                    .setLatency(entry.getLatency())
                    .setDisplayName(FabricTAB.toComponent(entry.getDisplayName(), player.getVersion())));
        }
        player.sendPacket(FabricMultiVersion.build(Action.ADD_PLAYER, converted));
    }

    @RequiredArgsConstructor
    @Getter
    public static class Builder {

        @NonNull private final UUID id;
        private String name;
        private Skin skin;
        private boolean listed;
        private int latency;
        private int gameMode;
        private Component displayName;

        public Builder setName(String name) { this.name = name; return this; }
        public Builder setSkin(Skin skin) { this.skin = skin; return this; }
        public Builder setListed(boolean listed) { this.listed = listed; return this; }
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
