package me.neznamy.tab.platforms.fabric;

import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.TAB;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class FabricTAB implements DedicatedServerModInitializer {

    @Getter
    private static VersionLoader version;

    @Override
    @SneakyThrows
    public void onInitializeServer() {
        String[] modules = {
                "v1_14_4",
                "v1_15_2",
                "v1_16_5",
                "v1_17",
                "v1_17_1",
                "v1_18_2", // 1.18, 1.18.1, 1.18.2
                "v1_19_2", // 1.19, 1.19.1, 1.19.2
                "v1_19_3",
                "v1_20_1", // 1.19.4, 1.20, 1.20.1
                "v1_20_2",
                "v1_20_3"
        };
        for (String module : modules) {
            try {
                version = (VersionLoader) Class.forName("me.neznamy.tab.platforms.fabric." + module + ".FabricTAB").getConstructor().newInstance();
                if (!version.getSupportedVersions().contains(version.getServerVersion())) continue;
                version.registerCommandCallback();
                ServerLifecycleEvents.SERVER_STARTING.register(server -> TAB.create(new FabricPlatform(server)));
                ServerLifecycleEvents.SERVER_STOPPING.register($ -> TAB.getInstance().unload());
                return;
            } catch (Throwable ignored) {}
        }
        throw new IllegalStateException("Your server version is marked as compatible, but a compatibility issue was found.");
    }
}
