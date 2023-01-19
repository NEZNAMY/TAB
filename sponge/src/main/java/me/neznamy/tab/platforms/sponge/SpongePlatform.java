package me.neznamy.tab.platforms.sponge;

import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.permission.PermissionPlugin;

public final class SpongePlatform extends Platform {

    private final Main plugin;

    public SpongePlatform(final Main plugin) {
        super(new PacketBuilder());
        this.plugin = plugin;
    }

    @Override
    public PermissionPlugin detectPermissionPlugin() {
        return null;
    }

    @Override
    public void loadFeatures() {
    }

    @Override
    public String getPluginVersion(String plugin) {
        return null;
    }

    @Override
    public void registerUnknownPlaceholder(String identifier) {
    }
}
