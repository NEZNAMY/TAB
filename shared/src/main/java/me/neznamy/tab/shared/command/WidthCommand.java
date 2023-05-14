package me.neznamy.tab.shared.command;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Handler for "/tab width" subcommand
 */
public class WidthCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public WidthCommand() {
        super("width", TabConstants.Permission.COMMAND_WIDTH);
    }

    @Override
    public void execute(TabPlayer sender, String[] args) {
        if (sender == null) {
            sendMessage(null, getMessages().getCommandOnlyFromGame());
            return;
        }
        if (args.length == 1) {
            // /tab width <symbol / ID>
            showWidth(sender, args[0], 10);
        } else if (args.length == 2) {
            // /tab width <symbol / ID> <amount>
            try {
                showWidth(sender, args[0], Integer.parseInt(args[1]));
            } catch (NumberFormatException ignored) {
                sendUsage(sender);
            }
        } else if (args.length == 3) {
            // /tab width set <symbol / ID> <width>
            if (args[0].equalsIgnoreCase("set")) {
                int width;
                try {
                    width = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sendMessage(sender, "&cError: The last argument must be a number.");
                    sendUsage(sender);
                    return;
                }
                String symbol = args[1];
                try {
                    // Character ID
                    int i = Integer.parseInt(args[1]);
                    if (i > Character.MAX_VALUE) {
                        sendMessage(sender, "&cCharacter ID out of range: 0-" + (int) Character.MAX_VALUE);
                        return;
                    }
                    symbol = String.valueOf((char)i);
                } catch (NumberFormatException e) {
                    // Character
                }
                String[] idArray = new String[symbol.length()];
                for (int i = 0; i < symbol.length(); i++) {
                    idArray[i] = Integer.toString(symbol.charAt(i));
                }
                TAB.getInstance().getConfig().getConfigurationSection("tablist-name-formatting.character-width-overrides").put(String.join("+", idArray), width);
                TAB.getInstance().getConfig().save();
                sendMessage(sender, "&2[TAB] Successfully set width of &6" + symbol + " &2(&6" + String.join("+", idArray) + "&2) to &6" + width + "&2 pixels.");
            } else {
                sendUsage(sender);
            }
        } else {
            sendUsage(sender);
        }
    }

    /**
     * Handler for /tab width <character / ID>
     *
     * @param   sender
     *          Command sender
     * @param   input
     *          Input argument for character
     * @param   amount
     *          Amount of lines to send
     */
    public void showWidth(TabPlayer sender, String input, int amount) {
        try {
            // Character ID
            int i = Integer.parseInt(input);
            if (i > Character.MAX_VALUE) {
                sendMessage(sender, "&cCharacter ID out of range: 0-" + (int) Character.MAX_VALUE);
                return;
            }
            sendWidth(sender, String.valueOf((char) i), amount);
        } catch (NumberFormatException e) {
            // Character
            sendWidth(sender, input, amount);
        }
    }

    public void sendUsage(TabPlayer sender) {
        sendMessage(sender, "&cUsage 1: &f/tab width &7<character>");
        sendMessage(sender, "&cUsage 2: &f/tab width &7<character / character ID> <amount>");
        sendMessage(sender, "&cUsage 3: &f/tab width set &7<character / character ID> <new width>");
    }

    public void sendWidth(TabPlayer sender, String symbol, int amount) {
        List<IChatBaseComponent> messages = new ArrayList<>();
        IChatBaseComponent charMessage = new IChatBaseComponent(EnumChatFormat.color("|&2" + symbol + " &d|"));
        messages.add(new IChatBaseComponent(EnumChatFormat.color("&b[TAB] Use &7/tab width set " + symbol + " <width> &bwith the matching value")));
        for (int i = 1; i <= amount; i++) {
            messages.add(getText(i));
            if (i % 2 != 0) {
                messages.add(charMessage);
            }
        }
        for (IChatBaseComponent message : messages) {
            sender.sendMessage(message);
        }
    }

    /**
     * Returns line of text with characters that build specified text width
     *
     * @param   width
     *          with to display
     * @return  line of text with characters that build specified text width
     */
    private IChatBaseComponent getText(int width) {
        StringBuilder text = new StringBuilder();
        int pixelsRemaining = width + 1;
        while (pixelsRemaining % 2 != 0) {
            pixelsRemaining -= 3;
            text.append('l');
        }
        while (pixelsRemaining > 0) {
            pixelsRemaining -= 2;
            text.append('i');
        }
        return new IChatBaseComponent(EnumChatFormat.color("|&b&k" + text + " &e|&b (" + width + " pixels)"));
    }

    @Override
    public @NotNull List<String> complete(TabPlayer sender, String[] arguments) {
        List<String> suggestions = new ArrayList<>();
        if (arguments.length == 1) {
            suggestions.add("set");
            suggestions.add("<character/ID>");
        } else if (arguments.length == 2) {
            if (arguments[0].equalsIgnoreCase("set")) {
                suggestions.add("<character (ID)>");
            } else {
                suggestions.add("<amount>");
            }
        } else if (arguments.length == 3 && arguments[0].equalsIgnoreCase("set")) {
            suggestions.add("<new width>");
        }
        return getStartingArgument(suggestions, arguments[arguments.length - 1]);
    }
} 