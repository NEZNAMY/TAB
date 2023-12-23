package me.neznamy.tab.platforms.bukkit.header;

import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Header/footer sender using Bukkit API. Despite this feature getting
 * added into the game in 1.8, Bukkit team took 4 entire years to add
 * it to the API, which eventually made it there in 1.13. <p>
 * The input is just a String, meaning Bukkit will have to deserialize it
 * into components, which is a massive performance loss. It also does not
 * seem to support fonts.
 */
public class BukkitHeaderFooter extends HeaderFooter {

    /** Flag tracking whether this class can be used on current server */
    @Getter
    private static final boolean available = ReflectionUtils.methodExists(Player.class, "setPlayerListHeaderFooter", String.class, String.class);

    @Override
    @SuppressWarnings("deprecation") // Marked as deprecated by Paper to make us use their methods instead
    public void set(@NotNull BukkitTabPlayer player, @NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        player.getPlayer().setPlayerListHeaderFooter(
                BukkitUtils.toBukkitFormat(header, player.getVersion().supportsRGB()),
                BukkitUtils.toBukkitFormat(footer, player.getVersion().supportsRGB())
        );
    }
}
