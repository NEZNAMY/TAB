package me.neznamy.tab.platforms.paper;

import me.neznamy.tab.platforms.bukkit.BukkitPipelineInjector;
import me.neznamy.tab.platforms.bukkit.nms.converter.ComponentConverter;
import me.neznamy.tab.platforms.bukkit.scoreboard.ScoreboardLoader;
import me.neznamy.tab.platforms.bukkit.tablist.TabListBase;
import org.bukkit.craftbukkit.entity.CraftPlayer;

/**
 * Loader for 1.20.5+ Paper to speed up functions by using direct NMS code instead of reflection.
 */
@SuppressWarnings("unused") // Via reflection
public class PaperLoader {

    /**
     * Sets instances of all plugin components.
     */
    public static void load() {
        ScoreboardLoader.setInstance(PaperPacketScoreboard::new);
        TabListBase.setInstance(PaperPacketTabList::new);
        ComponentConverter.INSTANCE = new PaperComponentConverter();
        BukkitPipelineInjector.setGetChannel(player -> ((CraftPlayer)player.getPlayer()).getHandle().connection.connection.channel);
    }
}
