package me.neznamy.tab.platforms.sponge;

import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.platforms.sponge.features.PetFix;
import me.neznamy.tab.platforms.sponge.features.unlimitedtags.SpongeNameTagX;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PipelineInjector;
import me.neznamy.tab.shared.features.TabExpansion;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.None;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public final class SpongePlatform extends Platform {

    private final Main plugin;

    public SpongePlatform(final Main plugin) {
        super(new SpongePacketBuilder());
        this.plugin = plugin;
    }

    @Override
    public PermissionPlugin detectPermissionPlugin() {
        if (Sponge.pluginManager().plugin(TabConstants.Plugin.LUCKPERMS.toLowerCase()).isPresent()) {
            return new LuckPerms(getPluginVersion(TabConstants.Plugin.LUCKPERMS.toLowerCase()));
        }
        return new None();
    }

    @Override
    public String getPluginVersion(final String plugin) {
        return Sponge.pluginManager().plugin(plugin).map(container -> container.metadata().version().toString()).orElse(null);
    }

    @Override
    public void registerUnknownPlaceholder(final String identifier) {
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, -1, () -> identifier);
    }

    @Override
    public void loadPlayers() {
        for (ServerPlayer player : Sponge.server().onlinePlayers()) {
            TAB.getInstance().addPlayer(new SpongeTabPlayer(player));
        }
    }

    @Override
    public void registerPlaceholders() {
        new SpongePlaceholderRegistry().registerPlaceholders(TAB.getInstance().getPlaceholderManager());
    }

    @Override
    public PipelineInjector getPipelineInjector() {
        return new SpongePipelineInjector();
    }

    @Override
    public NameTag getUnlimitedNametags() {
        return new SpongeNameTagX(plugin);
    }

    @Override
    public TabExpansion getTabExpansion() {
        return null;
    }

    @Override
    public TabFeature getPetFix() {
        return new PetFix();
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

    @Override
    public void sendConsoleMessage(String message, boolean translateColors) {
        if (translateColors) message = EnumChatFormat.color(message);
        final Component actualMessage = Component.text()
                .append(Component.text("[TAB] "))
                .append(LegacyComponentSerializer.legacySection().deserialize(message))
                .build();
        Sponge.systemSubject().sendMessage(actualMessage);
    }
}
