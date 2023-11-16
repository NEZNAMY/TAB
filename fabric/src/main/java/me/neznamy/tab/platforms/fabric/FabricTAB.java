package me.neznamy.tab.platforms.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;
import me.neznamy.tab.shared.TAB;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.NotNull;

public class FabricTAB implements DedicatedServerModInitializer {

    private static final boolean fabricPermissionsApi = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");

    @Override
    public void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register((dispatcher, $, $$) -> new FabricTabCommand().onRegisterCommands(dispatcher));
        ServerLifecycleEvents.SERVER_STARTING.register(server -> TAB.create(new FabricPlatform(server)));
        ServerLifecycleEvents.SERVER_STOPPING.register($ -> TAB.getInstance().unload());
    }

    public static boolean hasPermission(@NotNull CommandSourceStack source, @NotNull String permission) {
        if (source.hasPermission(4)) return true;
        return fabricPermissionsApi && Permissions.check(source, permission);
    }

    public static String getWorldName(Level level) {
        String path = level.dimension().location().getPath();
        String dimensionSuffix = switch (path) {
            case "overworld" -> ""; // No suffix for overworld
            case "the_nether" -> "_nether";
            default -> "_" + path; // End + default behavior for other dimensions created by mods
        };
        return ((ServerLevelData)level.getLevelData()).getLevelName() + dimensionSuffix;
    }
}
