package me.neznamy.tab.api.tablist.layout;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.Nullable;

public interface LayoutManager {

    Layout createNewLayout(String name);

    void sendLayout(@NonNull TabPlayer player, @Nullable Layout layout);

    void resetLayout(@NonNull TabPlayer player);
}
