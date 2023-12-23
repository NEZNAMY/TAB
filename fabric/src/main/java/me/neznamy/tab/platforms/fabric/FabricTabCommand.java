package me.neznamy.tab.platforms.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Command handler for plugin's command for Fabric.
 */
public class FabricTabCommand {

    /**
     * Handles command register request from server and registers own command.
     *
     * @param   dispatcher
     *          Dispatcher to register command to
     */
    public void onRegisterCommands(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> command = Commands.literal(TabConstants.COMMAND_BACKEND)
                .executes(context -> executeCommand(context.getSource(), new String[0]))
                .build();
        ArgumentCommandNode<CommandSourceStack, String> args = Commands.argument("args", StringArgumentType.greedyString())
                .suggests((context, builder) -> getSuggestions(context.getSource(), getArguments(context), builder))
                .executes(context -> executeCommand(context.getSource(), getArguments(context)))
                .build();
        command.addChild(args);
        dispatcher.getRoot().addChild(command);
    }

    @NotNull
    private String[] getArguments(@NotNull CommandContext<CommandSourceStack> context) {
        String input = context.getInput();
        int firstSpace = input.indexOf(' ');
        if (firstSpace == -1) return new String[0];
        String rawArgs = input.substring(firstSpace + 1);
        String[] args = rawArgs.split(" ");
        if (rawArgs.endsWith(" ")) {
            args = Arrays.copyOf(args, args.length+1);
            args[args.length-1] = "";
        }
        return args;
    }

    @SuppressWarnings("SameReturnValue") // Unused by plugin
    private int executeCommand(@NotNull CommandSourceStack source, @NotNull String[] args) {
        if (TAB.getInstance().isPluginDisabled()) {
            boolean hasReloadPermission = ((FabricPlatform)TAB.getInstance().getPlatform()).hasPermission(source, TabConstants.Permission.COMMAND_RELOAD);
            boolean hasAdminPermission = ((FabricPlatform)TAB.getInstance().getPlatform()).hasPermission(source, TabConstants.Permission.COMMAND_ALL);
            for (String message : TAB.getInstance().getDisabledCommand().execute(args, hasReloadPermission, hasAdminPermission)) {
                FabricMultiVersion.sendMessage2.accept(source, ((FabricPlatform)TAB.getInstance().getPlatform()).toComponent(
                        IChatBaseComponent.optimizedComponent(message), TAB.getInstance().getServerVersion()));
            }
        } else {
            if (source.getEntity() == null) {
                TAB.getInstance().getCommand().execute(null, args);
            } else {
                TabPlayer player = TAB.getInstance().getPlayer(source.getEntity().getUUID());
                if (player != null) TAB.getInstance().getCommand().execute(player, args);
            }
        }
        return 0;
    }

    @NotNull
    private CompletableFuture<Suggestions> getSuggestions(@NotNull CommandSourceStack source, @NotNull String[] args,
                                                          @NotNull SuggestionsBuilder builder) {
        TabPlayer player = null;
        if (source.getEntity() != null) {
            player = TAB.getInstance().getPlayer(source.getEntity().getUUID());
            if (player == null) return Suggestions.empty();
        }

        SuggestionsBuilder newBuilder = builder;
        int lastSpace = newBuilder.getRemaining().lastIndexOf(' ');
        if (lastSpace != -1) {
            newBuilder = newBuilder.createOffset(lastSpace + 1 + newBuilder.getStart());
        }
        for (String suggestion : TAB.getInstance().getCommand().complete(player, args)) {
            newBuilder.suggest(suggestion);
        }
        return newBuilder.buildFuture();
    }
}
