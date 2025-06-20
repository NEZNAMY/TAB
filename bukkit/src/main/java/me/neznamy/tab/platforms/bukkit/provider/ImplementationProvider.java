package me.neznamy.tab.platforms.bukkit.provider;

import io.netty.channel.Channel;
import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.provider.viaversion.ViaScoreboard;
import me.neznamy.tab.platforms.bukkit.provider.viaversion.ViaTabList;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ImplementationProvider {

    @NotNull
    Scoreboard newScoreboard(@NotNull BukkitTabPlayer player);

    void onPacketSend(@NonNull Object packet, @NonNull ViaScoreboard scoreboard);

    void onPacketSend(@NonNull Object packet, @NonNull ViaTabList tabList);

    @NotNull
    TabList newTabList(@NotNull BukkitTabPlayer player);

    @Nullable
    TabList.Skin getSkin(@NotNull BukkitTabPlayer player);

    @Nullable
    ComponentConverter getComponentConverter();

    @Nullable
    FunctionWithException<BukkitTabPlayer, Channel> getChannelFunction();

    int getPing(@NotNull BukkitTabPlayer player);
}
