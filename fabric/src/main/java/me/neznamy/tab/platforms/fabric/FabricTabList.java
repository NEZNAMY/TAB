package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.player.tablist.BulkUpdateTabList;
import me.neznamy.tab.shared.player.tablist.Skin;
import me.neznamy.tab.shared.player.tablist.TabList;
import me.neznamy.tab.shared.player.tablist.TabListEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry;
import net.minecraft.world.level.GameType;

@RequiredArgsConstructor
public class FabricTabList extends BulkUpdateTabList {

    private final FabricTabPlayer player;

    @Override
    public void removeEntries(@NonNull Collection<UUID> entries) {
        player.sendPacket(new ClientboundPlayerInfoRemovePacket(new ArrayList<>(entries)));
    }

    @Override
    public void updateDisplayNames(@NonNull Map<UUID, IChatBaseComponent> entries) {
        sendPacket(EnumSet.of(Action.UPDATE_DISPLAY_NAME), entries.entrySet().stream()
                .map(entry -> new Entry(entry.getKey(), new GameProfile(entry.getKey(), null), false, 0, GameType.DEFAULT_MODE,
                        FabricTAB.toComponent(entry.getValue(), player.getVersion()), null))
                .collect(Collectors.toList()));
    }

    @Override
    public void updateLatencies(@NonNull Map<UUID, Integer> entries) {
        sendPacket(EnumSet.of(Action.UPDATE_LATENCY), entries.entrySet().stream()
                .map(entry -> new Entry(entry.getKey(), new GameProfile(entry.getKey(), null), false, entry.getValue(), GameType.DEFAULT_MODE, null, null))
                .collect(Collectors.toList()));
    }

    @Override
    public void updateGameModes(@NonNull Map<UUID, Integer> entries) {
        sendPacket(EnumSet.of(Action.UPDATE_GAME_MODE), entries.entrySet().stream()
                .map(entry -> new Entry(entry.getKey(), new GameProfile(entry.getKey(), null), false, 0, GameType.byId(entry.getValue()), null, null))
                .collect(Collectors.toList()));
    }

    @Override
    public void addEntries(@NonNull Collection<TabListEntry> entries) {
        List<Entry> converted = new ArrayList<>();
        for (TabListEntry entry : entries) {
            GameProfile profile = new GameProfile(entry.getUniqueId(), entry.getName());
            if (entry.getSkin() != null) {
                Skin skin = entry.getSkin();
                profile.getProperties().put(TabList.TEXTURES_PROPERTY, new Property(TabList.TEXTURES_PROPERTY, skin.getValue(), skin.getSignature()));
            }
            Component displayName = FabricTAB.toComponent(entry.getDisplayName(), player.getVersion());
            converted.add(new Entry(entry.getUniqueId(), profile, entry.isListed(), entry.getLatency(), GameType.byId(entry.getGameMode()), displayName, null));
        }
        sendPacket(EnumSet.allOf(Action.class), converted);
    }

    private void sendPacket(EnumSet<Action> actions, Collection<Entry> entries) {
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(actions, Collections.emptyList());
        packet.entries().addAll(entries);
        player.sendPacket(packet);
    }
}
