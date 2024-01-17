package me.neznamy.tab.platforms.fabric;

import lombok.SneakyThrows;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.SharedConstants;

import java.util.Arrays;

/**
 * Main class for Fabric.
 */
public class FabricTAB implements DedicatedServerModInitializer {

    /** Minecraft version string */
    public static final String minecraftVersion = getServerVersion();

    @Override
    @SneakyThrows
    public void onInitializeServer() {
        for (String module : Arrays.asList("1_14_4", "1_20_4")) {
            Class.forName("me.neznamy.tab.platforms.fabric.loader.Loader_" + module)
                    .getConstructor(ProtocolVersion.class).newInstance(ProtocolVersion.fromFriendlyName(minecraftVersion));
        }
        if (ProtocolVersion.fromFriendlyName(minecraftVersion).getMinorVersion() >= 19) {
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

    /**
     * Returns {@code true} if fabric api contains entity events, {@code false} if not.
     *
     * @return  {@code true} if supports entity events, {@code false} if not
     */
    public static boolean supportsEntityEvents() {
        return ProtocolVersion.fromFriendlyName(minecraftVersion).getMinorVersion() >= 16;
    }
}
