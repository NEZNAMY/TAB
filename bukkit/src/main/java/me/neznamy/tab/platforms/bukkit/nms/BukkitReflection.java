package me.neznamy.tab.platforms.bukkit.nms;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Class containing main NMS-related information and methods.
 */
public class BukkitReflection {

    /** CraftBukkit package */
    private static final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

    /** CraftPlayer#getHandle method */
    public static final Method CraftPlayer_getHandle = getHandle();

    /** Server version data */
    private static final ServerVersion serverVersion = detectServerVersion();

    /** Flag determining whether the server version is at least 1.20.2 or not */
    @Getter
    private static final boolean is1_20_2Plus = ReflectionUtils.classExists("net.minecraft.world.scores.DisplaySlot");

    /** Flag determining whether the server version is at least 1.20.3 or not */
    @Getter
    private static final boolean is1_20_3Plus = ReflectionUtils.classExists("net.minecraft.network.protocol.game.ClientboundResetScorePacket");

    /** Flag determining whether the server version is at least 1.21.4 or not */
    @Getter
    private static final boolean is1_21_4Plus = serverVersion.minorVersion >= 21 &&
            ReflectionUtils.getFields(getClass("network.chat.Style", "network.chat.ChatModifier"), Integer.class).size() == 1;

    @NotNull
    @SneakyThrows
    private static Method getHandle() {
        return Class.forName(CRAFTBUKKIT_PACKAGE + ".entity.CraftPlayer").getMethod("getHandle");
    }

    private static ServerVersion detectServerVersion() {
        FunctionWithException<String, Class<?>> classFunction = name -> Class.forName("net.minecraft." + name);
        String[] array = CRAFTBUKKIT_PACKAGE.split("\\.");
        int minorVersion;
        if (array.length > 3) {
            // Normal packaging
            String serverPackage = array[3];
            minorVersion = Integer.parseInt(serverPackage.split("_")[1]);
            if (minorVersion < 17) {
                ClassLoader loader = BukkitReflection.class.getClassLoader();
                classFunction = name -> loader.loadClass("net.minecraft.server." + serverPackage + "." + name);
            }
        } else {
            // Paper without CB relocation
            minorVersion = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);
        }
        return new ServerVersion(classFunction, minorVersion);
    }

    /**
     * Returns server's minor version, such as 20 for 1.20.4
     *
     * @return  server's minor version
     */
    public static int getMinorVersion() {
        return serverVersion.getMinorVersion();
    }

    /**
     * Returns class with given potential names in same order. For 1.17+ it takes packaged class names
     * without "net.minecraft." prefix, for <1.17 it takes class name only.
     *
     * @param   names
     *          possible class names
     * @return  class for specified names
     * @throws  ClassNotFoundException
     *          if class does not exist
     */
    @SneakyThrows
    public static Class<?> getClass(@NotNull String... names) {
        for (String name : names) {
            try {
                return serverVersion.getClass.apply(name);
            } catch (ClassNotFoundException | NullPointerException ignored) {
                // not the first class name in array
            }
        }
        throw new ClassNotFoundException("No class found with possible names " + Arrays.toString(names));
    }

    /**
     * Class with server version information.
     */
    @RequiredArgsConstructor
    @Getter
    private static class ServerVersion {

        private final FunctionWithException<String, Class<?>> getClass;
        private final int minorVersion;
    }
}
