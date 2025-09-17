package me.neznamy.tab.platforms.paper_1_20_5;

import io.netty.channel.Channel;
import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.platforms.bukkit.provider.ImplementationProvider;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation provider using direct Mojang-mapped NMS code for versions 1.20.5 - 1.21.1.
 */
@Getter
public class PaperImplementationProvider implements ImplementationProvider {

    @NotNull
    private final ComponentConverter<?> componentConverter = new PaperComponentConverter();
    
    @Override
    @NotNull
    public Scoreboard newScoreboard(@NotNull BukkitTabPlayer player) {
        return new PaperPacketScoreboard(player);
    }

    @Override
    @NotNull
    public TabList newTabList(@NotNull BukkitTabPlayer player) {
        return new PaperPacketTabList(player);
    }

    @Override
    @NotNull
    public FunctionWithException<BukkitTabPlayer, Channel> getChannelFunction() {
        return player -> ((CraftPlayer)player.getPlayer()).getHandle().connection.connection.channel;
    }

    @Override
    public int getPing(@NotNull BukkitTabPlayer player) {
        return player.getPlayer().getPing();
    }
}