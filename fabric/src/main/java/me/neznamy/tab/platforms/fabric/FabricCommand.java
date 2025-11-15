package me.neznamy.tab.platforms.fabric;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command handler for plugin's command for Fabric.
 */
@RequiredArgsConstructor
public abstract class FabricCommand {

    @NotNull
    private final String commandName;

    /**
     * Builds and returns the command node.
     *
     * @return  Command node
     */
    @NotNull
    public LiteralCommandNode<CommandSourceStack> getCommand() {
        LiteralCommandNode<CommandSourceStack> command = Commands.literal(commandName)
                .executes(context -> execute(context.getSource(), new String[0]))
                .build();
        ArgumentCommandNode<CommandSourceStack, String> args = Commands.argument("args", StringArgumentType.greedyString())
                .suggests((context, builder) -> getSuggestions(context.getSource(), getArguments(context), builder))
                .executes(context -> execute(context.getSource(), getArguments(context)))
                .build();
        command.addChild(args);
        return command;
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

    @NotNull
    private CompletableFuture<Suggestions> getSuggestions(@NotNull CommandSourceStack source, @NotNull String[] args,
                                                          @NotNull SuggestionsBuilder builder) {
        SuggestionsBuilder newBuilder = builder;
        int lastSpace = newBuilder.getRemaining().lastIndexOf(' ');
        if (lastSpace != -1) {
            newBuilder = newBuilder.createOffset(lastSpace + 1 + newBuilder.getStart());
        }
        for (String suggestion : complete(source, args)) {
            newBuilder.suggest(suggestion);
        }
        return newBuilder.buildFuture();
    }

    /**
     * Executes the command with given arguments.
     *
     * @param   source
     *          Command source
     * @param   args
     *          Command arguments
     * @return  Command result
     */
    public abstract int execute(@NotNull CommandSourceStack source, @NotNull String[] args);

    /**
     * Returns a list of possible completions for given arguments.
     *
     * @param   sender
     *          Command sender, {@code null} if console
     * @param   args
     *          Command arguments
     * @return  List of possible completions
     */
    @NotNull
    public List<String> complete(@NotNull CommandSourceStack sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
