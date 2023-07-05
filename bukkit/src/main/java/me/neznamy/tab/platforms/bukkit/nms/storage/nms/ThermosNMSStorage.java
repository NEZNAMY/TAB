package me.neznamy.tab.platforms.bukkit.nms.storage.nms;

import org.jetbrains.annotations.NotNull;

/**
 * NMS loader for Thermos 1.7.10.
 */
public class ThermosNMSStorage extends BukkitLegacyNMSStorage {

    @Override
    public Class<?> getLegacyClass(@NotNull String name) throws ClassNotFoundException {
        try {
            return getClass().getClassLoader().loadClass("net.minecraft.server." + serverPackage + "." + name);
        } catch (NullPointerException e) {
            // nested class not found
            throw new ClassNotFoundException(name);
        }
    }
}
