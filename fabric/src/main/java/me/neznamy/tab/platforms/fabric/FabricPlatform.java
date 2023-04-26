package me.neznamy.tab.platforms.fabric;

import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FabricPlatform extends BackendPlatform {

    @Override
    public void registerUnknownPlaceholder(@NonNull String identifier) {
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, -1, () -> identifier);
    }

    @Override
    public void loadPlayers() {
        for (ServerPlayer player : PlayerLookup.all(FabricTAB.getInstance().getServer())) {
            TAB.getInstance().addPlayer(new FabricTabPlayer(player));
        }
    }

    @Override
    public void registerPlaceholders() {
        new FabricPlaceholderRegistry().registerPlaceholders(TAB.getInstance().getPlaceholderManager());
    }

    @Override
    public @NotNull NameTag getUnlimitedNametags() {
        return new NameTag();
    }

    @Override
    public @Nullable PipelineInjector getPipelineInjector() {
        return null;
    }

    @Override
    public @NonNull TabExpansion getTabExpansion() {
        return new EmptyTabExpansion();
    }

    @Override
    public @Nullable TabFeature getPerWorldPlayerlist() {
        return null;
    }

    @Override
    public void sendConsoleMessage(@NonNull String message, boolean translateColors) {
        MinecraftServer.LOGGER.info(Component.Serializer.fromJson(IChatBaseComponent.optimizedComponent(
                            "[TAB] " + (translateColors ? EnumChatFormat.color(message) : message)).toString()).getString());
    }
}
