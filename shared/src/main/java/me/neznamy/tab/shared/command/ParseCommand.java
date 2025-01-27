package me.neznamy.tab.shared.command;

import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.StructuredComponent;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.chat.TextColor;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        String textToParse = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (!textToParse.contains("%")) {
            sendMessage(sender, "&cThe provided input (" + textToParse + ") does not contain any placeholders, therefore there's nothing to test.");
            return;
        }
        // Do it this way to avoid sending the "§" symbol to the console to try to color the text (does not work on Velocity)
        sendMessage(sender, new StructuredComponent("", Arrays.asList(
                new StructuredComponent("Replacing placeholder ", TextColor.legacy(EnumChatFormat.GOLD)),
                new StructuredComponent(textToParse, TextColor.legacy(EnumChatFormat.YELLOW)),
                new StructuredComponent(" for player ", TextColor.legacy(EnumChatFormat.GOLD)),
                new StructuredComponent(target.getName(), TextColor.legacy(EnumChatFormat.YELLOW))
        )));
        try {
            String replaced = new Property(null, null, target, textToParse, null).get();
            TabComponent colored = TabComponent.fromColoredText("&3Colored output: &e\"&r" + replaced + "&e\"");
            if (sender != null) {
                sender.sendMessage(colored);
            } else {
                TAB.getInstance().getPlatform().logInfo(colored);
            }
            sendMessage(sender, new StructuredComponent("", Arrays.asList(
                    new StructuredComponent("Raw colors: ", TextColor.legacy(EnumChatFormat.DARK_AQUA)),
                    new StructuredComponent("\"", TextColor.legacy(EnumChatFormat.YELLOW)),
                    new StructuredComponent(replaced.replace('§', '&'), TextColor.legacy(EnumChatFormat.WHITE)),
                    new StructuredComponent("\"", TextColor.legacy(EnumChatFormat.YELLOW))
            )));
            sendMessage(sender, "&3Output length: &e" + replaced.length() + " &3characters");
        } catch (Exception e) {
            sendMessage(sender, "&cThe placeholder threw an exception when parsing. Check console for more info.");
            TAB.getInstance().getErrorManager().parseCommandError(textToParse, target, e);
        }
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