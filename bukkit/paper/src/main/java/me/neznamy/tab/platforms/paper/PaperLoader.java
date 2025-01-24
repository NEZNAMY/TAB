package me.neznamy.tab.platforms.paper;

import io.netty.channel.Channel;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import org.bukkit.craftbukkit.entity.CraftPlayer;

/**
 * Loader for 1.20.5+ Paper to speed up functions by using direct NMS code instead of reflection.
 */
@SuppressWarnings("unused") // Via reflection
public class PaperLoader {

    /** Channel getter using direct NMS code */
    public static final FunctionWithException<BukkitTabPlayer, Channel> getChannel =
            player -> ((CraftPlayer)player.getPlayer()).getHandle().connection.connection.channel;
}
