package me.neznamy.tab.platforms.krypton;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kryptonmc.api.auth.GameProfile;
import org.kryptonmc.api.auth.ProfileProperty;
import org.kryptonmc.api.entity.player.TabListEntry;
import org.kryptonmc.api.world.GameMode;
import java.util.Collections;
import java.util.UUID;

@RequiredArgsConstructor
public class KryptonTabList implements TabList {

    private final KryptonTabPlayer player;

    @Override
    public void removeEntry(@NonNull UUID entryId) {
        player.getPlayer().getTabList().removeEntry(entryId);
    }

    @Override
    public void updateDisplayName(@NonNull UUID entryId, @Nullable IChatBaseComponent displayName) {
        TabListEntry entry = player.getPlayer().getTabList().getEntry(entryId);
        if (entry != null) entry.setDisplayName(displayName == null ? null : displayName.toAdventureComponent());
    }

    @Override
    public void updateLatency(@NonNull UUID entryId, int latency) {
        TabListEntry entry = player.getPlayer().getTabList().getEntry(entryId);
        if (entry != null) entry.setLatency(latency);
    }

    @Override
    public void updateGameMode(@NonNull UUID entryId, int gameMode) {
        TabListEntry entry = player.getPlayer().getTabList().getEntry(entryId);
        if (entry != null) entry.setGameMode(GameMode.values()[gameMode]);
    }

    @Override
    public void addEntry(@NonNull me.neznamy.tab.shared.platform.TabList.Entry entry) {
        GameProfile profile = createGameProfile(entry.getUniqueId(), entry.getName(), entry.getSkin());
        player.getPlayer().getTabList().createEntryBuilder(entry.getUniqueId(), profile)
                .displayName(entry.getDisplayName() == null ? null : entry.getDisplayName().toAdventureComponent())
                .gameMode(GameMode.values()[entry.getGameMode()])
                .latency(entry.getLatency())
                .listed(true)
                .buildAndRegister();
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull IChatBaseComponent header, @NonNull IChatBaseComponent footer) {
        player.getPlayer().getTabList().setHeaderAndFooter(header.toAdventureComponent(), footer.toAdventureComponent());
    }

    private @NotNull GameProfile createGameProfile(@NonNull UUID uuid, @Nullable String name, @Nullable Skin skin) {
        String newName = name == null ? "" : name;
        if (skin == null) return GameProfile.of(newName, uuid);
        ProfileProperty property = ProfileProperty.of("textures", skin.getValue(), skin.getSignature());
        return GameProfile.of(newName, uuid, Collections.singletonList(property));
    }
}