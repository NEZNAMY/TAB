package me.neznamy.tab.platforms.bukkit.paper_26_2;

import io.netty.channel.Channel;
import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.platforms.bukkit.provider.ImplementationProvider;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabListEntryTracker;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation provider using direct Mojang-mapped NMS code for Paper 26.2.
 */
@Getter
public class NMSImplementationProvider implements ImplementationProvider {

    /** Flag tracking if this server is Canvas 26.1.2+ with new scoreboard checks */
    private final boolean isCanvas = ReflectionUtils.classExists("io.canvasmc.canvas.world.scores.TeamData") ||
            ReflectionUtils.classExists("io.canvasmc.canvas.threadedregions.scores.TeamData");

    @NotNull
    private final ComponentConverter<?> componentConverter = new NMSComponentConverter();
    
    @Override
    @NotNull
    public Scoreboard newScoreboard(@NotNull BukkitTabPlayer player) {
        if (isCanvas) {
            return new CanvasPacketScoreboard(player);
        } else {
            return new NMSPacketScoreboard(player);
        }
    }

    @Override
    @NotNull
    public TabList newTabList(@NotNull BukkitTabPlayer player) {
        return new NMSPacketTabList(player);
    }

    @Override
    @NotNull
    public Channel getChannel(@NotNull Player player) {
        return ((CraftPlayer)player).getHandle().connection.connection.channel;
    }

    @Override
    @NotNull
    public TabListEntryTracker newTabListEntryTracker(@NotNull Player player) {
        return new NMSTabListEntryTracker(getChannel(player));
    }

    @Override
    public int getPing(@NotNull BukkitTabPlayer player) {
        return player.getPlayer().getPing();
    }
}