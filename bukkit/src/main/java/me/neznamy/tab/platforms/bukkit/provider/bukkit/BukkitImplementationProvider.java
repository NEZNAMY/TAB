package me.neznamy.tab.platforms.bukkit.provider.bukkit;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.platforms.bukkit.provider.ImplementationProvider;
import me.neznamy.tab.platforms.bukkit.provider.viaversion.ViaScoreboard;
import me.neznamy.tab.platforms.bukkit.provider.viaversion.ViaTabList;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.impl.DummyScoreboard;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Implementation provider using Bukkit API.
 */
@Getter
public class BukkitImplementationProvider implements ImplementationProvider {

    @NotNull
    private final Function<BukkitTabPlayer, Integer> pingProvider = ReflectionUtils.methodExists(Player.class, "getPing") ? p -> p.getPlayer().getPing() : p -> -1;

    @Override
    @NotNull
    public Scoreboard newScoreboard(@NotNull BukkitTabPlayer player) {
        if (PaperScoreboard.isAvailable()) {
            return new PaperScoreboard(player);
        } else if (BukkitScoreboard.isAvailable()) {
            return new BukkitScoreboard(player);
        } else {
            return new DummyScoreboard(player);
        }
    }

    @Override
    public void onPacketSend(@NonNull Object packet, @NonNull ViaScoreboard scoreboard) {
        // Do nothing
    }

    @Override
    public void onPacketSend(@NonNull Object packet, @NonNull ViaTabList tabList) {
        // Do nothing
    }

    @Override
    @NotNull
    public TabList newTabList(@NotNull BukkitTabPlayer player) {
        return new BukkitTabList(player);
    }

    @Override
    @Nullable
    public TabList.Skin getSkin(@NotNull BukkitTabPlayer player) {
        return null; // Shrug
    }

    @Override
    @Nullable
    public ComponentConverter getComponentConverter() {
        return null;
    }

    @Override
    @Nullable
    public FunctionWithException<BukkitTabPlayer, Channel> getChannelFunction() {
        return null;
    }

    @Override
    public int getPing(@NotNull BukkitTabPlayer player) {
        return pingProvider.apply(player);
    }
}
