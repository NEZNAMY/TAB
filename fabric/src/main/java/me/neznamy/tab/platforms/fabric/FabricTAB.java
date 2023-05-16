package me.neznamy.tab.platforms.fabric;

import lombok.Getter;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.util.ComponentCache;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FabricTAB implements DedicatedServerModInitializer {

    private final ComponentCache<IChatBaseComponent, Component> componentCache = new ComponentCache<>(1000,
            (text, version) -> Component.Serializer.fromJson(text.toString(version)));

    @Getter private static FabricTAB instance;
    private final boolean fabricPermissionsApi = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");

    @Getter private MinecraftServer server;

    @Override
    public void onInitializeServer() {
        instance = this;
        ProtocolVersion protocolVersion = ProtocolVersion.fromNetworkId(SharedConstants.getCurrentVersion().getProtocolVersion());
        String version = SharedConstants.getCurrentVersion().getName();
        File folder = FabricLoader.getInstance().getConfigDir().resolve(TabConstants.PLUGIN_ID).toFile();
        TAB.setInstance(new TAB(new FabricPlatform(), protocolVersion, version, folder, null));
        new FabricEventListener().register();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            this.server = server;
            TAB.getInstance().load();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> TAB.getInstance().unload());
        FabricMultiVersion.registerCommand.run();
    }

    public Component toComponent(@NotNull IChatBaseComponent component, @NotNull ProtocolVersion clientVersion) {
        return componentCache.get(component, clientVersion);
    }

    public boolean hasPermission(@NotNull CommandSourceStack source, @NotNull String permission) {
        if (source.hasPermission(4)) return true;
        return fabricPermissionsApi && Permissions.check(source, permission);
    }
}
