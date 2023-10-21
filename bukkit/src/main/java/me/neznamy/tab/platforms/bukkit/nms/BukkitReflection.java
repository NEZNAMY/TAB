package me.neznamy.tab.platforms.bukkit.nms;

import lombok.Getter;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class BukkitReflection {

    /** Server's NMS/CraftBukkit package */
    @Getter
    private static final String serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    /** Server's minor version */
    @Getter
    private static final int minorVersion = Integer.parseInt(serverPackage.split("_")[1]);

    /** Flag determining whether this server jar is mojang mapped or not */
    @Getter
    private static final boolean mojangMapped = checkMojangMapped();

    /** Flag determining whether the server version is at least 1.19.3 or not */
    @Getter
    private static final boolean is1_19_3Plus = ReflectionUtils.classExists("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");

    /** Flag determining whether the server version is at least 1.19.4 or not */
    @Getter
    private static final boolean is1_19_4Plus = is1_19_3Plus && !serverPackage.equals("v1_19_R2");

    /** Flag determining whether the server version is at least 1.20.2 or not */
    @Getter
    private static final boolean is1_20_2Plus = minorVersion >= 20 && !serverPackage.equals("v1_20_R1");

    private static boolean checkMojangMapped() {
        try {
            Class.forName("net.minecraft.network.syncher.SynchedEntityData").getMethod("define",
                    Class.forName("net.minecraft.network.syncher.EntityDataAccessor"), Object.class);
            return true;
        } catch (ReflectiveOperationException | NullPointerException ex) {
            return false;
        }
    }

    /**
     * Returns class with given potential names in same order
     *
     * @param   names
     *          possible class names
     * @return  class for specified names
     * @throws  ClassNotFoundException
     *          if class does not exist
     */
    public static Class<?> getLegacyClass(@NotNull String... names) throws ClassNotFoundException {
        for (String name : names) {
            try {
                return getLegacyClass(name);
            } catch (ClassNotFoundException e) {
                //not the first class name in array
            }
        }
        throw new ClassNotFoundException("No class found with possible names " + Arrays.toString(names));
    }

    /**
     * Returns class from given name. Supports modded servers, such as Thermos.
     *
     * @param   name
     *          class name
     * @return  class from given name
     * @throws  ClassNotFoundException
     *          if class was not found
     */
    public static Class<?> getLegacyClass(@NotNull String name) throws ClassNotFoundException {
        try {
            return BukkitReflection.class.getClassLoader().loadClass("net.minecraft.server." + serverPackage + "." + name);
        } catch (NullPointerException e) {
            // nested class not found
            throw new ClassNotFoundException(name);
        }
    }
}
