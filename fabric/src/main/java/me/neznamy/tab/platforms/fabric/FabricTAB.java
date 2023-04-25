package me.neznamy.tab.platforms.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.NonNull;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ComponentCache;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

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
        File folder = FabricLoader.getInstance().getConfigDir().resolve("tab").toFile();
        TAB.setInstance(new TAB(new FabricPlatform(), protocolVersion, version, folder, null));

        new FabricEventListener().register();

        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> TAB.getInstance().unload());
        FabricMultiVersion.registerCommand.run();
    }

    private void onServerStart(MinecraftServer server) {
        this.server = server;
        TAB.getInstance().load();
    }

    public Component toComponent(@Nullable IChatBaseComponent component, @NonNull ProtocolVersion clientVersion) {
        return componentCache.get(component, clientVersion);
    }

    public boolean hasPermission(CommandSourceStack source, String permission) {
        if (source.hasPermission(4)) return true;
        return fabricPermissionsApi && Permissions.check(source, permission);
    }

    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> command = Commands.literal("tab")
                .executes(context -> executeCommand(context.getSource(), new String[0]))
                .build();
        ArgumentCommandNode<CommandSourceStack, String> args = Commands.argument("args", StringArgumentType.greedyString())
                .suggests((context, builder) -> getSuggestions(context.getSource(), getArguments(context), builder))
                .executes(context -> executeCommand(context.getSource(), getArguments(context)))
                .build();
        command.addChild(args);
        dispatcher.getRoot().addChild(command);
    }

    private String[] getArguments(CommandContext<CommandSourceStack> context) {
        String input = context.getInput();
        int firstSpace = input.indexOf(' ');
        if (firstSpace == -1) return new String[0];
        String rawArgs = input.substring(firstSpace + 1);
        return rawArgs.split(" ");
    }

    private int executeCommand(CommandSourceStack source, String[] args) {
        if (TAB.getInstance().isPluginDisabled()) {
            boolean hasReloadPermission = hasPermission(source, TabConstants.Permission.COMMAND_RELOAD);
            boolean hasAdminPermission = hasPermission(source, TabConstants.Permission.COMMAND_ALL);
            for (String message : TAB.getInstance().getDisabledCommand().execute(args, hasReloadPermission, hasAdminPermission)) {
                FabricMultiVersion.sendMessage2.accept(source, Component.Serializer.fromJson(IChatBaseComponent.optimizedComponent(message).toString()));
            }
            return 0;
        }

        TabPlayer player = null;
        if (source.getEntity() != null) {
            player = TAB.getInstance().getPlayer(source.getEntity().getUUID());
            if (player == null) return 0;
        }

        TAB.getInstance().getCommand().execute(player, args);
        return 0;
    }

    private CompletableFuture<Suggestions> getSuggestions(CommandSourceStack source, String[] args, SuggestionsBuilder builder) {
        TabPlayer player = null;
        if (source.getEntity() != null) {
            player = TAB.getInstance().getPlayer(source.getEntity().getUUID());
            if (player == null) return Suggestions.empty();
        }

        for (String suggestion : TAB.getInstance().getCommand().complete(player, args)) {
            builder.suggest(suggestion);
        }
        return builder.buildFuture();
    }
}
