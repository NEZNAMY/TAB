package me.neznamy.tab.platforms.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;
import me.neznamy.tab.shared.TAB;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class FabricTAB implements DedicatedServerModInitializer {

    private static final boolean fabricPermissionsApi = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            try {
                TAB.create(new FabricPlatform(server));
                ServerLifecycleEvents.SERVER_STOPPING.register($ -> TAB.getInstance().unload());
            } catch (Error e) {
                System.out.println("[TAB] Your server version is not supported! This jar only supports Fabric 1.20.1");
            }
        });
    }

    public static boolean hasPermission(@NotNull CommandSourceStack source, @NotNull String permission) {
        if (source.hasPermission(4)) return true;
        return fabricPermissionsApi && Permissions.check(source, permission);
    }
}
