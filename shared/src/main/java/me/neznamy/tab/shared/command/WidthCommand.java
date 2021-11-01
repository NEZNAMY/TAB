package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TAB;

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
			try {
				int i = Integer.parseInt(args[0]);
				if (i > Character.MAX_VALUE) {
					sendMessage(sender, "&cCharacter ID out of range: 0-" + (int)Character.MAX_VALUE);
					return;
				}
				c = (char) i;
			} catch (NumberFormatException e) {
				if (args[0].length() == 1) {
					c = args[0].charAt(0);
				} else {
					sendMessage(sender, "The provided input consists of following characters:");
					char[] chars = args[0].toCharArray();
					int[] ints = new int[chars.length];
					for (int i=0; i<chars.length; i++) {
						ints[i] = chars[i];
					}
					sendMessage(sender, Arrays.toString(ints));
					return;
				}
			}
			List<IChatBaseComponent> messages = new ArrayList<>();
			IChatBaseComponent charMessage = new IChatBaseComponent(EnumChatFormat.color("&2" + c + " &d|"));
			messages.add(new IChatBaseComponent(EnumChatFormat.color("&b[TAB] Click the line with closest width")));
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
			TAB.getInstance().getConfig().getConfigurationSection("tablist-name-formatting.character-width-overrides").put(c, width);
			TAB.getInstance().getConfig().save();
			sendMessage(sender, "&2[TAB] Successfully set width of &6" + (char)c + " &2(&6" + c + "&2) to &6" + width + "&2 pixels.");
			if (TAB.getInstance().isDebugMode()) {
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
		IChatBaseComponent component = new IChatBaseComponent(EnumChatFormat.color("&b&k" + text + " &e|&b (" + width + " pixels) &7&l[Click to apply]"));
		component.getModifier().onClickRunCommand("/tab width " + c + " " + width);
		component.getModifier().onHoverShowText(new IChatBaseComponent("Click to set width to " + width + " pixels"));
		return component;
	}
} 