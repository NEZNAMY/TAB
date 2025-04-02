package me.neznamy.tab.platforms.bukkit.header;

import lombok.SneakyThrows;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.PacketSender;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * Header/footer sender that uses NMS to send packets.
 */
public class PacketHeaderFooter extends HeaderFooter {

    private final PacketSender packetSender = new PacketSender();
    private final Constructor<?> newHeaderFooter;

    /**
     * Constructs new instance and loads all NMS content. Throws exception if something went wrong.
     *
     * @throws  ReflectiveOperationException
     *          If something went wrong
     */
    public PacketHeaderFooter() throws ReflectiveOperationException {
        Class<?> Component = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent");
        newHeaderFooter = BukkitReflection.getClass(
                "network.protocol.game.ClientboundTabListPacket",
                "network.protocol.game.PacketPlayOutPlayerListHeaderFooter"
        ).getConstructor(Component, Component);
    }

    @Override
    @SneakyThrows
    public void set(@NotNull BukkitTabPlayer player, @NotNull TabComponent header, @NotNull TabComponent footer) {
        packetSender.sendPacket(player, newHeaderFooter.newInstance(header.convert(), footer.convert()));
    }
}
