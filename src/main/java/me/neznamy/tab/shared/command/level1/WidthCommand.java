package me.neznamy.tab.shared.command.level1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.packets.IChatBaseComponent;

/**
 * Handler for "/tab width" subcommand
 */
public class WidthCommand extends SubCommand {

	/**
	 * Constructs new instance
	 */
	public WidthCommand() {
		super("width", "tab.width");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		if (sender == null) {
			sendMessage(sender, "&cThis command must be ran from the game");
			return;
		}
		if (args.length == 1) {
			char c;
			if (args[0].length() == 1) {
				c = args[0].charAt(0);
			} else {
				try {
					int i = Integer.parseInt(args[0]);
					if (i > Character.MAX_VALUE) {
						sendMessage(sender, "&cCharacter ID out of range: 0-" + (int)Character.MAX_VALUE);
						return;
					}
					c = (char) i;
				} catch (NumberFormatException e) {
					sendMessage(sender, "&c" + args[0] + " is not a valid number/character!");
					return;
				}
			}
			List<IChatBaseComponent> messages = new ArrayList<IChatBaseComponent>();
			IChatBaseComponent charMessage = new IChatBaseComponent("\u00a72" + c + " \u00a7d|");
			messages.add(new IChatBaseComponent("\u00a7b[TAB] Click the line with closest width"));
			messages.add(getText(1, c));
			messages.add(charMessage);
			messages.add(getText(2, c));
			messages.add(getText(3, c));
			messages.add(charMessage);
			messages.add(getText(4, c));
			messages.add(getText(5, c));
			messages.add(charMessage);
			messages.add(getText(6, c));
			messages.add(getText(7, c));
			messages.add(charMessage);
			messages.add(getText(8, c));
			messages.add(getText(9, c));
			messages.add(charMessage);
			messages.add(getText(10, c));
			for (IChatBaseComponent message : messages) {
				sender.sendMessage(message);
			}
		} else if (args.length == 2) {
			int c = Integer.parseInt(args[0]);
			int width = Integer.parseInt(args[1]);
			if (!TAB.getInstance().getConfiguration().premiumconfig.hasConfigOption("extra-character-widths")) TAB.getInstance().getConfiguration().premiumconfig.set("extra-character-widths", new HashMap<Integer, Integer>());
			TAB.getInstance().getConfiguration().premiumconfig.getConfigurationSection("extra-character-widths").put(c, width);
			TAB.getInstance().getConfiguration().premiumconfig.save();
			sendMessage(sender, "&2[TAB] Successfully set width of &6" + (char)c + " &2(&6" + c + "&2) to &6" + width + "&2 pixels.");
			if (TAB.getInstance().debugMode) {
				execute(sender, new String[] {String.valueOf(c+1)});
			}
		} else {
			sendMessage(sender, "Usage: /tab width <character>");
		}
	}
	
	/**
	 * Returns line of text with characters that build specified text width
	 * @param width - with to display
	 * @param c - character to set click action to
	 * @return line of text with characters that build specified text width
	 */
	private IChatBaseComponent getText(int width, int c) {
		String text = "";
		int pixelsRemaining = width + 1;
		while (pixelsRemaining % 2 != 0) {
			pixelsRemaining -= 3;
			text += "l";
		}
		while (pixelsRemaining > 0) {
			pixelsRemaining -= 2;
			text += "i";
		}
		return new IChatBaseComponent(("&b&k" + text + " &e|&b (" + width + " pixels) &7&l[Click to apply]").replace('&', '\u00a7')).onClickRunCommand("/tab width " + (int)c + " " + width).onHoverShowText("Click to set width to " + width + " pixels");
	}
}