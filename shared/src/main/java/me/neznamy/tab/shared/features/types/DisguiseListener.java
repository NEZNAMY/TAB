package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/** Listener for platform disguise status changes. */
public interface DisguiseListener {

    void onDisguiseStatusChange(@NotNull TabPlayer player);
}
