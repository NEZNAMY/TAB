package me.neznamy.tab.api.tablist;

import lombok.NonNull;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface TabList {

    String TEXTURES_PROPERTY = "textures";

    void removeEntry(@NonNull UUID entry);

    void removeEntries(@NonNull Collection<UUID> entries);

    void updateDisplayName(@NonNull UUID entry, @Nullable IChatBaseComponent displayName);

    void updateDisplayNames(@NonNull Map<UUID, IChatBaseComponent> entries);

    void updateLatency(@NonNull UUID entry, int latency);

    void updateLatencies(@NonNull Map<UUID, Integer> entries);

    void updateGameMode(@NonNull UUID entry, int gameMode);

    void updateGameModes(@NonNull Map<UUID, Integer> entries);

    void addEntry(@NonNull TabListEntry entry);

    void addEntries(@NonNull Collection<TabListEntry> entries);
}
