package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.shared.TAB;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.NotNull;

/**
 * Main class for Fabric.
 */
public class FabricTAB implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext, commandSelection) -> new FabricTabCommand().onRegisterCommands(dispatcher));
        ServerLifecycleEvents.SERVER_STARTING.register(server -> TAB.create(new FabricPlatform(server)));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> TAB.getInstance().unload());
    }

    @NotNull
    public static String getLevelName(@NotNull Level level) {
        String path = level.dimension().location().getPath();
        return ((ServerLevelData)level.getLevelData()).getLevelName() + switch (path) {
            case "overworld" -> ""; // No suffix for overworld
            case "the_nether" -> "_nether";
            default -> "_" + path; // End + default behavior for other dimensions created by mods
        };
    }
}
