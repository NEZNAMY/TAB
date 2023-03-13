package me.neznamy.tab.shared.command;

import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TAB;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handler for "/tab width" subcommand
 */
public class WidthCheckCommand extends SubCommand {

    private byte[] widths;

    /**
     * Constructs new instance
     */
    public WidthCheckCommand() {
        super("widthcheck", TabConstants.Permission.COMMAND_WIDTH);
    }

    private byte[] loadWidths() {
        byte[] widths = new byte[65536];
        InputStream file = getClass().getClassLoader().getResourceAsStream("widths.txt");
        if (file == null) {
            TAB.getInstance().getErrorManager().criticalError("Failed to load widths.txt file. Is it inside the jar? Aligned suffix will not work.", null);
            return widths;
        }
        int characterId = 1;
        for (String line : new BufferedReader(new InputStreamReader(file)).lines().collect(Collectors.toList())) {
            widths[characterId++] = (byte) Float.parseFloat(line);
        }
        Map<Integer, Integer> widthOverrides = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("tablist-name-formatting.character-width-overrides");
        for (Map.Entry<Integer, Integer> entry : widthOverrides.entrySet()) {
            widths[entry.getKey()] = entry.getValue().byteValue();
        }
        return widths;
    }

    @Override
    public void execute(TabPlayer sender, String[] args) {
        if (widths == null) widths = loadWidths();
        if (sender == null) {
            sendMessage(null, getMessages().getCommandOnlyFromGame());
            return;
        }
        if (args.length == 1) {
            int i = Integer.parseInt(args[0]);
            if (i < 0 || i > Character.MAX_VALUE) {
                sendMessage(sender, "&cCharacter ID out of range: 0-" + (int) Character.MAX_VALUE + " (was " + i + ")");
                return;
            }
            int ROWS = 15;
            int COLUMNS = 10;
            StringBuilder line = new StringBuilder();
            for (int row = 0; row < ROWS; row++) {
                line.append(".");
                for (int column = 0; column < COLUMNS; column++) {
                    int character = row*COLUMNS+column + i;
                    if (character >= Character.MAX_VALUE) continue;
                    line.append(buildSpaces(21-widths[character]));
                    line.append((char)character);
                    line.append("&e|&r");
                }
                line.append("   ").append(row * COLUMNS + i).append("-").append((row + 1) * COLUMNS + i - 1).append("\n");
            }
            IChatBaseComponent msg = IChatBaseComponent.fromColoredText(line + "§7§l[Previous]");
            msg.getModifier().onClickRunCommand("/tab widthcheck " + (i-ROWS*COLUMNS));
            IChatBaseComponent next = new IChatBaseComponent("                                    §7§l[Next]");
            next.getModifier().onClickRunCommand("/tab widthcheck " + (i+ROWS*COLUMNS));
            msg.addExtra(next);
            sender.sendMessage(msg);
        }
    }

    private String buildSpaces(int pixelWidth) {
        if (pixelWidth < 12) throw new IllegalArgumentException("Cannot build space lower than 12 pixels wide (" + pixelWidth + ")");
        int pixelsLeft = pixelWidth;
        StringBuilder output = new StringBuilder();
        while (pixelsLeft % 5 != 0) {
            pixelsLeft -= 4;
            output.append(' ');
        }
        output.append(EnumChatFormat.COLOR_CHAR);
        output.append('l');
        while (pixelsLeft > 0) {
            pixelsLeft -= 5;
            output.append(' ');
        }
        output.append(EnumChatFormat.COLOR_CHAR);
        output.append('r');
        return output.toString();
    }
} 