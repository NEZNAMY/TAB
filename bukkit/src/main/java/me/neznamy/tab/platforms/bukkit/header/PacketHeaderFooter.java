package me.neznamy.tab.platforms.bukkit.header;

import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.PacketSender;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.util.BiFunctionWithException;
import me.neznamy.tab.shared.util.ComponentCache;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Header/footer sender that uses NMS to send packets. Available
 * on all versions since 1.8 when the feature was added into the game.
 */
public class PacketHeaderFooter extends HeaderFooter {

    private Method ChatSerializer_DESERIALIZE;
    private final PacketSender packetSender = new PacketSender();
    private final ComponentCache<TabComponent, Object> componentCache = new ComponentCache<>(1000,
            (component, clientVersion) -> ChatSerializer_DESERIALIZE.invoke(null, component.toString(clientVersion)));
    private final BiFunctionWithException<Object, Object, Object> createPacket;

    /**
     * Constructs new instance and loads all NMS content. Throws exception if something went wrong.
     *
     * @throws  ReflectiveOperationException
     *          If something went wrong
     */
    public PacketHeaderFooter() throws ReflectiveOperationException {
        Class<?> Component = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent", "IChatBaseComponent");
        Class<?> HeaderFooterClass = BukkitReflection.getClass("network.protocol.game.ClientboundTabListPacket",
                "network.protocol.game.PacketPlayOutPlayerListHeaderFooter", "PacketPlayOutPlayerListHeaderFooter");
        Class<?> ChatSerializer = BukkitReflection.getClass("network.chat.Component$Serializer",
                "network.chat.IChatBaseComponent$ChatSerializer", "IChatBaseComponent$ChatSerializer", "ChatSerializer");
        ChatSerializer_DESERIALIZE = ReflectionUtils.getMethods(ChatSerializer, Object.class, String.class).get(0);
        if (BukkitReflection.getMinorVersion() >= 17) {
            Constructor<?> newHeaderFooter = HeaderFooterClass.getConstructor(Component, Component);
            createPacket = newHeaderFooter::newInstance;
        } else {
            Constructor<?> newHeaderFooter = HeaderFooterClass.getConstructor();
            Field HEADER = ReflectionUtils.getFields(HeaderFooterClass, Component).get(0);
            Field FOOTER = ReflectionUtils.getFields(HeaderFooterClass, Component).get(1);
            createPacket = (header, footer) -> {
                Object packet = newHeaderFooter.newInstance();
                HEADER.set(packet, header);
                FOOTER.set(packet, footer);
                return packet;
            };
        }
    }

    @SneakyThrows
    @Override
    public void set(@NotNull BukkitTabPlayer player, @NotNull TabComponent header, @NotNull TabComponent footer) {
        packetSender.sendPacket(player.getPlayer(), createPacket.apply(
                componentCache.get(header, player.getVersion()),
                componentCache.get(footer, player.getVersion())
        ));
    }
}
