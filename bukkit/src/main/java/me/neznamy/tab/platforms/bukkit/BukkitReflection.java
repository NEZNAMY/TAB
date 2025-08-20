package me.neznamy.tab.platforms.bukkit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @Getter
    private static final ServerVersion serverVersion = detectServerVersion();

    @NotNull
    @SneakyThrows
    private static Method getHandle() {
        return Class.forName(CRAFTBUKKIT_PACKAGE + ".entity.CraftPlayer").getMethod("getHandle");
    }

    private static ServerVersion detectServerVersion() {
        String[] array = CRAFTBUKKIT_PACKAGE.split("\\.");
        if (array.length > 3) {
            // Normal packaging
            return new ServerVersion(Integer.parseInt(array[3].split("_")[1]), array[3]);
        } else {
            // Paper without CB relocation
            return new ServerVersion(Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]), null);
        }
    }

    /**
     * Returns class with given potential names in same order. It takes packaged class names
     * without "net.minecraft." prefix.
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
                return Class.forName("net.minecraft." + name);
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
    public static class ServerVersion {

        private final int minorVersion;

        @Nullable
        private final String serverPackage;
    }
}
