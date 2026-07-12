package me.neznamy.tab.platforms.fand;

import io.fand.api.command.Arguments;
import io.fand.api.command.CommandContext;
import io.fand.api.command.CommandRegistration;
import io.fand.api.command.CommandRegistry;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/** Shared Fand command adapter for TAB commands. */
public abstract class FandCommand {

    private final String commandName;

    protected FandCommand(@NotNull String commandName) {
        this.commandName = commandName;
    }

    @NotNull
    public CommandRegistration register(@NotNull CommandRegistry registry) {
        return registry.register(commandName, command -> {
            command.executes(context -> execute(context, new String[0]));
            command.argument("arguments", Arguments.greedyString(), arguments -> arguments
                    .suggests(context -> complete(context, arguments(context)))
                    .executes(context -> execute(context, arguments(context))));
        });
    }

    private static String[] arguments(CommandContext context) {
        return context.args().toArray(String[]::new);
    }

    protected abstract void execute(@NotNull CommandContext context, @NotNull String[] arguments);

    @NotNull
    protected List<String> complete(@NotNull CommandContext context, @NotNull String[] arguments) {
        return Collections.emptyList();
    }
}
