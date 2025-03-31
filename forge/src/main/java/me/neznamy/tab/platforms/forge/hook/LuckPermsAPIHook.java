package me.neznamy.tab.platforms.forge.hook;

import net.luckperms.api.LuckPermsProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

/**
 * Class hooking into LuckPerms mod for permission nodes instead
 * of only using the integrated OP level.
 */
public class LuckPermsAPIHook {

    /** Flag tracking presence of LuckPerms API */
    private static final boolean luckPerms = ModList.get().isLoaded("luckperms");

    /**
     * Checks for permission and returns the result.
     *
     * @param   source
     *          Source to check permission of
     * @param   permission
     *          Permission node to check
     * @return  {@code true} if has permission, {@code false} if not
     */
    public static boolean hasPermission(@NotNull CommandSourceStack source, @NotNull String permission) {
        if (source.hasPermission(4)) return true;
        return luckPerms && LuckPermsProvider.get().getUserManager().getUser(source.getPlayer().getUUID()).getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }
}
