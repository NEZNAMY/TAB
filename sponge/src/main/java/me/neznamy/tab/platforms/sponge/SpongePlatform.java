package me.neznamy.tab.platforms.sponge;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PipelineInjector;
import me.neznamy.tab.shared.features.TabExpansion;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.None;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;

public final class SpongePlatform extends Platform {

    public SpongePlatform() {
        super(new PacketBuilder());
    }

    @Override
    public PermissionPlugin detectPermissionPlugin() {
        if (Sponge.getPluginManager().isLoaded("luckperms")) {
            return new LuckPerms(getPluginVersion("luckperms"));
        }
        return new None();
    }

    @Override
    public String getPluginVersion(final String plugin) {
        return Sponge.getPluginManager().getPlugin(plugin).flatMap(PluginContainer::getVersion).orElse(null);
    }

    @Override
    public void registerUnknownPlaceholder(final String identifier) {
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, -1, () -> identifier);
    }

    @Override
    public void loadPlayers() {
        for (final Player player : Sponge.getServer().getOnlinePlayers()) {
            TAB.getInstance().addPlayer(new SpongeTabPlayer(player));
        }
    }

    @Override
    public void registerPlaceholders() {
        new SpongePlaceholderRegistry().registerPlaceholders(TAB.getInstance().getPlaceholderManager());
    }

    @Override
    public @Nullable PipelineInjector getPipelineInjector() {
        return null;
    }

    @Override
    public NameTag getUnlimitedNametags() {
        return new NameTag();
    }

    @Override
    public TabExpansion getTabExpansion() {
        return null;
    }

    @Override
    public @Nullable TabFeature getPetFix() {
        return null;
    }

    @Override
    public @Nullable TabFeature getGlobalPlayerlist() {
        return null;
    }

    @Override
    public @Nullable RedisSupport getRedisSupport() {
        return null;
    }

    @Override
    public @Nullable TabFeature getPerWorldPlayerlist() {
        return null;
    }
}
