package me.neznamy.tab.platforms.bukkit.v1_20_R1;

import io.netty.channel.Channel;
import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.platforms.bukkit.provider.ImplementationProvider;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * Implementation provider using direct NMS code for 1.20 - 1.20.1.
 */
@Getter
public class NMSImplementationProvider implements ImplementationProvider {

    /** Field is somehow private */
    private static final Field networkManager = ReflectionUtils.getOnlyField(PlayerConnection.class, NetworkManager.class);

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
    public FunctionWithException<BukkitTabPlayer, Channel> getChannelFunction() {
        return player -> ((NetworkManager)networkManager.get(((CraftPlayer)player.getPlayer()).getHandle().c)).m;
    }

    @Override
    public int getPing(@NotNull BukkitTabPlayer player) {
        return player.getPlayer().getPing();
    }
}