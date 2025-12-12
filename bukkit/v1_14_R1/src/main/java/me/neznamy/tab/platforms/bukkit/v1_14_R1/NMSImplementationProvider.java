package me.neznamy.tab.platforms.bukkit.v1_14_R1;

import io.netty.channel.Channel;
import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.platforms.bukkit.provider.ImplementationProvider;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabListEntryTracker;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation provider using direct NMS code for 1.14.x.
 */
@Getter
public class NMSImplementationProvider implements ImplementationProvider {

    @NotNull
    private final ComponentConverter<?> componentConverter = new NMSComponentConverter();
    
    @Override
    @NotNull
    public Scoreboard newScoreboard(@NotNull BukkitTabPlayer player) {
        return new NMSPacketScoreboard(player);
    }

    @Override
    @NotNull
    public TabList newTabList(@NotNull BukkitTabPlayer player) {
        return new NMSPacketTabList(player);
    }

    @Override
    @NotNull
    public Channel getChannel(@NotNull Player player) {
        return ((CraftPlayer)player).getHandle().playerConnection.networkManager.channel;
    }

    @Override
    @NotNull
    public TabListEntryTracker newTabListEntryTracker() {
        return new NMSTabListEntryTracker();
    }

    @Override
    public int getPing(@NotNull BukkitTabPlayer player) {
        return ((CraftPlayer)player.getPlayer()).getHandle().ping;
    }
}