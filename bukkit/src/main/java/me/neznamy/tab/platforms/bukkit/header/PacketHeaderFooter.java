package me.neznamy.tab.platforms.bukkit.header;

import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
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

    private static Constructor<?> newHeaderFooter;
    private static Field HEADER;
    private static Field FOOTER;
    private static Method ChatSerializer_DESERIALIZE;
    private static final ComponentCache<IChatBaseComponent, Object> componentCache = new ComponentCache<>(1000,
            (component, clientVersion) -> ChatSerializer_DESERIALIZE.invoke(null, component.toString(clientVersion)));

    @Getter
    private static boolean available;

    static {
        try {
            if (BukkitReflection.getMinorVersion() >= 8) {
                Class<?> IChatBaseComponent = BukkitReflection.getClass("network.chat.Component",
                        "network.chat.IChatBaseComponent", "IChatBaseComponent");
                Class<?> HeaderFooterClass = BukkitReflection.getClass("network.protocol.game.ClientboundTabListPacket",
                        "network.protocol.game.PacketPlayOutPlayerListHeaderFooter", "PacketPlayOutPlayerListHeaderFooter");
                Class<?> ChatSerializer = BukkitReflection.getClass("network.chat.Component$Serializer",
                        "network.chat.IChatBaseComponent$ChatSerializer", "IChatBaseComponent$ChatSerializer", "ChatSerializer");
                ChatSerializer_DESERIALIZE = ReflectionUtils.getMethods(ChatSerializer, Object.class, String.class).get(0);
                if (BukkitReflection.getMinorVersion() >= 17) {
                    newHeaderFooter = HeaderFooterClass.getConstructor(IChatBaseComponent, IChatBaseComponent);
                } else {
                    newHeaderFooter = HeaderFooterClass.getConstructor();
                    HEADER = ReflectionUtils.getFields(HeaderFooterClass, IChatBaseComponent).get(0);
                    FOOTER = ReflectionUtils.getFields(HeaderFooterClass, IChatBaseComponent).get(1);
                }
                available = true;
            }
        } catch (Exception ignored) {
            // Print exception to find out what went wrong
        }
    }

    @SneakyThrows
    @Override
    public void set(@NotNull BukkitTabPlayer player, @NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        if (BukkitReflection.getMinorVersion() >= 17) {
            player.sendPacket(newHeaderFooter.newInstance(convert(header, player), convert(footer, player)));
        } else {
            Object packet = newHeaderFooter.newInstance();
            HEADER.set(packet, convert(header, player));
            FOOTER.set(packet, convert(footer, player));
            player.sendPacket(packet);
        }
    }

    private Object convert(IChatBaseComponent component, TabPlayer player) {
        return componentCache.get(component, player.getVersion());
    }
}
