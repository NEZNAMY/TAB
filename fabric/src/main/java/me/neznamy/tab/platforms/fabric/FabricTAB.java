package me.neznamy.tab.platforms.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FabricTAB implements DedicatedServerModInitializer {

    private static final boolean fabricPermissionsApi = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");

    @Override
    public void onInitializeServer() {
        new FabricEventListener().register();
        CommandRegistrationCallback.EVENT.register((dispatcher, $, $$) -> new FabricTabCommand().onRegisterCommands(dispatcher));
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            TAB.setInstance(new TAB(
                    new FabricPlatform(server),
                    ProtocolVersion.fromNetworkId(SharedConstants.getCurrentVersion().getProtocolVersion()),
                    FabricLoader.getInstance().getConfigDir().resolve(TabConstants.PLUGIN_ID).toFile())
            );
            TAB.getInstance().load();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> TAB.getInstance().unload());
    }

    public static boolean hasPermission(@NotNull CommandSourceStack source, @NotNull String permission) {
        if (source.hasPermission(4)) return true;
        return fabricPermissionsApi && Permissions.check(source, permission);
    }

    public static @NotNull Component toComponent(@NotNull IChatBaseComponent component, @NotNull ProtocolVersion clientVersion) {
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
            if (clientVersion.getMinorVersion() >= 16) {
                style = style.withColor(component.getModifier().getColor().getRgb());
            } else {
                style = style.withColor(ChatFormatting.valueOf(component.getModifier().getColor().getLegacyColor().name()));
            }
        }
        comp.withStyle(style);

        // Extra
        comp.getSiblings().addAll(component.getExtra().stream().map(c -> toComponent(c, clientVersion)).toList());

        return comp;
    }
}
