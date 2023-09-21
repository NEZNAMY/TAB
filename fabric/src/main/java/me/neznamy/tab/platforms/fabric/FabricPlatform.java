package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.platforms.fabric.features.FabricNameTagX;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Platform implementation for Fabric
 */
public record FabricPlatform(MinecraftServer server) implements BackendPlatform<Component> {

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, -1, () -> identifier);
    }

    @Override
    public void loadPlayers() {
        for (ServerPlayer player : PlayerLookup.all(server)) {
            TAB.getInstance().addPlayer(new FabricTabPlayer(this, player));
        }
    }

    @Override
    @NotNull
    public PipelineInjector createPipelineInjector() {
        return new FabricPipelineInjector();
    }

    @Override
    @NotNull
    public NameTag getUnlimitedNameTags() {
        return new FabricNameTagX();
    }

    @Override
    @NotNull
    public TabExpansion createTabExpansion() {
        return new EmptyTabExpansion();
    }

    @Override
    @Nullable
    public TabFeature getPerWorldPlayerList() {
        return null;
    }

    @Override
    public void logInfo(@NotNull IChatBaseComponent message) {
        MinecraftServer.LOGGER.info("[TAB] " + message.toRawText());
    }

    @Override
    public void logWarn(@NotNull IChatBaseComponent message) {
        MinecraftServer.LOGGER.warn("[TAB] " + message.toRawText()); // Fabric console does not support colors
    }

    @Override
    @NotNull
    public String getServerVersionInfo() {
        return "[Fabric] " + SharedConstants.getCurrentVersion().getName();
    }

    @Override
    public void registerListener() {
        new FabricEventListener().register();
    }

    @Override
    public void registerCommand() {
        // Event listener must be registered in main class
    }

    @Override
    public void startMetrics() {
        // Not available
    }

    @Override
    @NotNull
    public ProtocolVersion getServerVersion() {
        return ProtocolVersion.fromFriendlyName(SharedConstants.getCurrentVersion().getName());
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return FabricLoader.getInstance().getConfigDir().resolve(TabConstants.PLUGIN_ID).toFile();
    }

    @Override
    public Component toComponent(@NotNull IChatBaseComponent component, @NotNull ProtocolVersion version) {
        // Text
        MutableComponent comp = MutableComponent.create(new LiteralContents(component.getText()));

        // Color and style
        List<ChatFormatting> formats = new ArrayList<>();
        if (component.getModifier().isUnderlined())     formats.add(ChatFormatting.UNDERLINE);
        if (component.getModifier().isObfuscated())     formats.add(ChatFormatting.OBFUSCATED);
        if (component.getModifier().isStrikethrough())  formats.add(ChatFormatting.STRIKETHROUGH);
        if (component.getModifier().isItalic())         formats.add(ChatFormatting.ITALIC);
        if (component.getModifier().isBold())           formats.add(ChatFormatting.BOLD);
        Style style = comp.getStyle().applyFormats(formats.toArray(new ChatFormatting[0]));
        if (component.getModifier().getColor() != null) {
            if (version.getMinorVersion() >= 16) {
                style = style.withColor(component.getModifier().getColor().getRgb());
            } else {
                style = style.withColor(ChatFormatting.valueOf(component.getModifier().getColor().getLegacyColor().name()));
            }
        }
        if (component.getModifier().getFont() != null)
            style = style.withFont(new ResourceLocation(component.getModifier().getFont()));
        comp.withStyle(style);

        // Extra
        comp.getSiblings().addAll(component.getExtra().stream().map(c -> toComponent(c, version)).toList());

        return comp;
    }

    @Override
    public double getTPS() {
        return -1; // Not available
    }

    @Override
    public double getMSPT() {
        return server.getAverageTickTime();
    }
}
