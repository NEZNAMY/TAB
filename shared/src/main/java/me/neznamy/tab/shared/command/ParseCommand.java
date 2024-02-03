package me.neznamy.tab.shared.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handler for "/tab parse &lt;player&gt; &lt;placeholder&gt;" subcommand
 */
public class ParseCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public ParseCommand() {
        super("parse", TabConstants.Permission.COMMAND_PARSE);
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        if (args.length < 2) {
            sendMessage(sender, getMessages().getParseCommandUsage());
            return;
        }
        TabPlayer target;
        if (args[0].equals("me")) {
            if (sender != null) {
                target = sender;
            } else {
                sendMessage(null, "&cThe \"me\" argument instead of player name is only available in-game " +
                        "and parses the placeholder for player who ran the command. If you wish to use the parse command " +
                        "from the console, use name of an online player instead of \"me\".");
                return;
            }
        } else {
            target = TAB.getInstance().getPlayer(args[0]);
            if (target == null) {
                sendMessage(sender, getMessages().getPlayerNotFound(args[0]));
                return;
            }
        }
        String replaced = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (!replaced.contains("%")) {
            sendMessage(sender, "&cThe provided input (" + replaced + ") does not contain any placeholders, therefore there's nothing to test.");
            return;
        }
        String message = EnumChatFormat.color("&6Replacing placeholder &e%placeholder% &6for player &e" + target.getName()).replace("%placeholder%", replaced);
        sendRawMessage(sender, message);
        try {
            replaced = new Property(null, null, target, replaced, null).get();
        } catch (Exception e) {
            sendMessage(sender, "&cThe placeholder threw an exception when parsing. Check console for more info.");
            TAB.getInstance().getErrorManager().parseCommandError(replaced, target, e);
            return;
        }
        TabComponent colored = TabComponent.optimized(EnumChatFormat.color("&3Colored output: &e\"&r" + replaced + "&e\""));
        if (sender != null) {
            sender.sendMessage(colored);
        } else {
            TAB.getInstance().getPlatform().logInfo(colored);
        }
        sendRawMessage(sender, EnumChatFormat.color("&3Raw colors: &e\"&r") + EnumChatFormat.decolor(replaced) + EnumChatFormat.color("&e\""));
        sendRawMessage(sender, EnumChatFormat.color("&3Output length: &e" + replaced.length() + " &3characters"));
    }

    @Override
    @NotNull
    public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        if (arguments.length == 1) {
            List<String> suggestions = getOnlinePlayers(arguments[0]);
            if ("me".startsWith(arguments[0].toLowerCase())) suggestions.add("me");
            return suggestions;
        }
        if (arguments.length == 2) {
            return TAB.getInstance().getPlaceholderManager().getAllPlaceholders().stream().map(Placeholder::getIdentifier)
                    .filter(placeholder -> placeholder.toLowerCase().startsWith(arguments[1].toLowerCase())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}