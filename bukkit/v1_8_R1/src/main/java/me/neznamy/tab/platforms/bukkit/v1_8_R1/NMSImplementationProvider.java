package me.neznamy.tab.platforms.bukkit.v1_8_R1;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.platforms.bukkit.provider.ImplementationProvider;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabListEntryTracker;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.server.v1_8_R1.NetworkManager;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * Implementation provider using direct NMS code for 1.8.
 */
@Getter
public class NMSImplementationProvider implements ImplementationProvider {

    private static final Field channel = ReflectionUtils.getOnlyField(NetworkManager.class, Channel.class);

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
    @SneakyThrows
    public Channel getChannel(@NotNull Player player) {
        return (Channel) channel.get(((CraftPlayer)player).getHandle().playerConnection.networkManager);
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