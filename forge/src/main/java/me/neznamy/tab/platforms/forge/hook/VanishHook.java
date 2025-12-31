package me.neznamy.tab.platforms.forge.hook;

import lombok.SneakyThrows;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Vanish hook for Forge.
 */
public class VanishHook {

    /** Method for getting vanish status for a player. */
    @Nullable
    private static Method isVanished;

    static {
        try {
            if (ModList.get().isLoaded("vmod")) {
                isVanished = Class.forName("redstonedubstep.mods.vanishmod.VanishUtil").getMethod("isVanished", Player.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if Vanish mod is installed.
     *
     * @return  {@code true} if installed, {@code false} if not
     */
    public static boolean isInstalled() {
        return isVanished != null;
    }

    /**
     * Check if the player is vanished.
     *
     * @param   player
     *          Player to check
     * @return  {@code true} if vanished, {@code false} if not
     */
    @SneakyThrows
    public static boolean isVanished(@NotNull ServerPlayer player) {
        if (isVanished == null) return false;
        return (boolean) isVanished.invoke(null, player);
    }
}
