package me.neznamy.tab.platforms.bukkit.header;

import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.hook.AdventureHook;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Header/footer sender using Paper's API added in 1.16.5.
 * Thanks to both cache and direct component conversion, this
 * implementation is even faster than using NMS components
 * and is therefore preferred if available.
 */
public class PaperHeaderFooter extends HeaderFooter {

    /** Flag tracking whether this class can be used on current server */
    @Getter
    private static final boolean available = ReflectionUtils.classExists("net.kyori.adventure.text.Component") &&
            ReflectionUtils.methodExists(Player.class, "sendPlayerListHeaderAndFooter", Component.class, Component.class);

    @Override
    public void set(@NotNull BukkitTabPlayer player, @NotNull TabComponent header, @NotNull TabComponent footer) {
        player.getPlayer().sendPlayerListHeaderAndFooter(
                AdventureHook.toAdventureComponent(header, player.getVersion()),
                AdventureHook.toAdventureComponent(footer, player.getVersion())
        );
    }
}
