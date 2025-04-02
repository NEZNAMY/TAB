package me.neznamy.tab.platforms.bukkit.header;

import lombok.SneakyThrows;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.PacketSender;
import me.neznamy.tab.shared.util.function.BiFunctionWithException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * Header/footer sender that uses NMS to send packets. Available
 * on all versions since 1.8 when the feature was added into the game.
 */
public class PacketHeaderFooter extends HeaderFooter {

    private final PacketSender packetSender = new PacketSender();
    private final BiFunctionWithException<Object, Object, Object> createPacket;

    /**
     * Constructs new instance and loads all NMS content. Throws exception if something went wrong.
     *
     * @throws  ReflectiveOperationException
     *          If something went wrong
     */
    public PacketHeaderFooter() throws ReflectiveOperationException {
        Class<?> Component = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent");
        Class<?> HeaderFooterClass = BukkitReflection.getClass("network.protocol.game.ClientboundTabListPacket",
                "network.protocol.game.PacketPlayOutPlayerListHeaderFooter");
        Constructor<?> newHeaderFooter = HeaderFooterClass.getConstructor(Component, Component);
        createPacket = newHeaderFooter::newInstance;
    }

    @Override
    @SneakyThrows
    public void set(@NotNull BukkitTabPlayer player, @NotNull TabComponent header, @NotNull TabComponent footer) {
        packetSender.sendPacket(player, createPacket.apply(header.convert(), footer.convert()));
    }
}
