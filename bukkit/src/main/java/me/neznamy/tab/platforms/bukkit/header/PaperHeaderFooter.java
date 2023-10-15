package me.neznamy.tab.platforms.bukkit.header;

import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.hook.AdventureHook;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Header/footer sender using Paper's API added in 1.16.5.
 * Due to lack of component cache for adventure components in TAB,
 * there is a performance loss compared to using NMS. However, we do not
 * need to serialize/deserialize anything ourselves thanks to direct component
 * conversion, so in the end it is roughly the same.
 */
public class PaperHeaderFooter extends HeaderFooter {

    /** Flag tracking whether this class can be used on current server */
    @Getter
    private static final boolean available = ReflectionUtils.classExists("net.kyori.adventure.text.Component") &&
            ReflectionUtils.methodExists(Player.class, "sendPlayerListHeaderAndFooter", Component.class, Component.class);

    @Override
    public void set(@NotNull BukkitTabPlayer player, @NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        player.getPlayer().sendPlayerListHeaderAndFooter(
                AdventureHook.toAdventureComponent(header, player.getVersion()),
                AdventureHook.toAdventureComponent(footer, player.getVersion())
        );
    }
}
