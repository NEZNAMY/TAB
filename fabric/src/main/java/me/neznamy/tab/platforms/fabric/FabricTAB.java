package me.neznamy.tab.platforms.fabric;

import lombok.SneakyThrows;
import me.neznamy.bossbar.fabric.FabricBossBarAPI;
import me.neznamy.bossbar.shared.BossBarAPI;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.SharedConstants;

/**
 * Main class for Fabric.
 */
public class FabricTAB implements DedicatedServerModInitializer {

    /** Minecraft version string */
    public static final String minecraftVersion = getServerVersion();

    @Override
    public void onInitializeServer() {
        BossBarAPI.setInstance(new FabricBossBarAPI());
        if (ReflectionUtils.classExists("net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback")) {
            net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((dispatcher, $, $$) -> new FabricTabCommand().onRegisterCommands(dispatcher));
        } else {
            net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback.EVENT.register((dispatcher, $) -> new FabricTabCommand().onRegisterCommands(dispatcher));
        }
        ServerLifecycleEvents.SERVER_STARTING.register(server -> TAB.create(new FabricPlatform(server)));
        ServerLifecycleEvents.SERVER_STOPPING.register($ -> TAB.getInstance().unload());
    }

    @SneakyThrows
    private static String getServerVersion() {
        try {
            // 1.19.4+
            return SharedConstants.getCurrentVersion().getName();
        } catch (Throwable e) {
            // 1.19.3-
            @SuppressWarnings("JavaReflectionMemberAccess") // Fabric-mapped method name
            Object gameVersion = SharedConstants.class.getMethod("method_16673").invoke(null);
            return (String) gameVersion.getClass().getMethod("getName").invoke(gameVersion);
        }
    }
}
