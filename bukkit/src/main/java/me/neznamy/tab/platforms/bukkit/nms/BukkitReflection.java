package me.neznamy.tab.platforms.bukkit.nms;

import lombok.Getter;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Class containing main NMS-related information and methods.
 */
public class BukkitReflection {

    /** CraftBukkit package */
    private static final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

    /** Server's NMS package */
    @Getter
    private static final String serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    /** Server's minor version */
    @Getter
    private static final int minorVersion = Integer.parseInt(serverPackage.split("_")[1]);

    /** Server version since which minecraft code uses mojang packaging */
    private static final int MOJANG_PACKAGING_VERSION_START = 17;

    /** Flag determining whether the server version is at least 1.19.3 or not */
    @Getter
    private static final boolean is1_19_3Plus = ReflectionUtils.classExists("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");

    /** Flag determining whether the server version is at least 1.19.4 or not */
    @Getter
    private static final boolean is1_19_4Plus = ReflectionUtils.classExists("net.minecraft.network.protocol.game.ClientboundBundlePacket");

    /** Flag determining whether the server version is at least 1.20.2 or not */
    @Getter
    private static final boolean is1_20_2Plus = ReflectionUtils.classExists("net.minecraft.world.scores.DisplaySlot");

    /** Flag determining whether the server version is at least 1.20.3 or not */
    @Getter
    private static final boolean is1_20_3Plus = ReflectionUtils.classExists("net.minecraft.network.protocol.game.ClientboundResetScorePacket");

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
    public static Class<?> getClass(@NotNull String... names) throws ClassNotFoundException {
        ClassLoader loader = BukkitReflection.class.getClassLoader();
        for (String name : names) {
            try {
                if (minorVersion >= MOJANG_PACKAGING_VERSION_START) {
                    return Class.forName("net.minecraft." + name);
                } else {
                    return loader.loadClass("net.minecraft.server." + serverPackage + "." + name);
                }
            } catch (ClassNotFoundException | NullPointerException ignored) {
                // not the first class name in array
            }
        }
        throw new ClassNotFoundException("No class found with possible names " + Arrays.toString(names));
    }

    /**
     * Returns CraftBukkit class with given package and name.
     *
     * @param   name
     *          Package and name of the class
     * @return  CraftBukkit class
     * @throws  ClassNotFoundException
     *          If class does not exist
     */
    public static Class<?> getBukkitClass(@NotNull String name) throws ClassNotFoundException {
        return Class.forName(CRAFTBUKKIT_PACKAGE + "." + name);
    }
}
