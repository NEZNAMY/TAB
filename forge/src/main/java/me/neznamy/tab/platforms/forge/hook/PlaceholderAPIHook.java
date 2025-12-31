package me.neznamy.tab.platforms.forge.hook;

import lombok.SneakyThrows;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * PlaceholderAPI hook for Forge.
 */
public class PlaceholderAPIHook {

    /** PlaceholderAPI method for replacing placeholders. Using reflection due to dependency issues (different server version). */
    @Nullable
    private static Method replaceIdentifiers;

    static {
        try {
            if (ModList.get().isLoaded("forgeplaceholderapi")) {
                replaceIdentifiers = Class.forName("com.envyful.papi.api.util.UtilPlaceholder").getMethod("replaceIdentifiers", Object.class, String.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if PlaceholderAPI is installed.
     *
     * @return  {@code true} if installed, {@code false} if not
     */
    public static boolean isInstalled() {
        return replaceIdentifiers != null;
    }

    /**
     * Replaces PlaceholderAPI placeholders in the given string for the specified player.
     *
     * @param   player
     *          Player to replace placeholders for
     * @param   identifier
     *          String containing placeholders to be replaced
     * @return  String with replaced placeholders
     */
    @NotNull
    @SneakyThrows
    public static String setPlaceholders(@NotNull ServerPlayer player, @NotNull String identifier) {
        if (replaceIdentifiers == null) return identifier;
        return (String) replaceIdentifiers.invoke(null, player, identifier);
    }
}
