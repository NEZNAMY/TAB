package me.neznamy.tab.platforms.bukkit.provider;

import io.netty.channel.Channel;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.converter.ComponentConverter;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ImplementationProvider {

    @NotNull
    Scoreboard newScoreboard(@NotNull BukkitTabPlayer player);

    @NotNull
    TabList newTabList(@NotNull BukkitTabPlayer player);

    @Nullable
    ComponentConverter getComponentConverter();

    @Nullable
    FunctionWithException<BukkitTabPlayer, Channel> getChannelFunction();
}
