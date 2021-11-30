package me.neznamy.tab.shared.command;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
			char character;
			try {
				int i = Integer.parseInt(args[0]);
				if (i > Character.MAX_VALUE) {
					sendMessage(sender, "&cCharacter ID out of range: 0-" + (int) Character.MAX_VALUE);
					return;
				}
				if (i < 10) { //Use number
					character = args[0].charAt(0);
				} else { //Use character ID
					character = (char) i;
				}

			} catch (NumberFormatException e) {
				if (args[0].length() == 1) {
					character = args[0].charAt(0);
				} else {
					sendInputCharacterIDs(sender, args[0]);
					return;
				}
			}
			sendWidth(sender, character, 10);
		} else if (args.length == 2) {
			int amount;
			try {
				amount = Integer.parseInt(args[1]);
			} catch (NumberFormatException ignored) {
				sendUsage(sender);
				return;
			}

			char character;
			try {
				int i = Integer.parseInt(args[0]);
				if (i > Character.MAX_VALUE) {
					sendMessage(sender, "&cCharacter ID out of range: 0-" + (int) Character.MAX_VALUE);
					return;
				}
				if (i < 10) { //Use number
					character = args[0].charAt(0);
				} else { //Use character ID
					character = (char) i;
				}

			} catch (NumberFormatException e) {
				if (args[0].length() == 1) {
					character = args[0].charAt(0);
				} else {
					sendInputCharacterIDs(sender, args[0]);
					return;
				}
			}
			sendWidth(sender, character, amount);


		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("set")) {

				int character;
				int width;


				try {
					int i = Integer.parseInt(args[1]);
					if (i > Character.MAX_VALUE) {
						sendMessage(sender, "&cCharacter ID out of range: 0-" + (int) Character.MAX_VALUE);
						return;
					}
					if (i < 10) { //Use number
						character = args[1].charAt(0);
					} else { //Use character ID
						character = (char) i;
					}

				} catch (NumberFormatException e) {
					if (args[1].length() == 1) {
						character = args[1].charAt(0);
					} else {
						sendInputCharacterIDs(sender, args[1]);
						return;
					}
				}

				try {
					width = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					sendMessage(sender, "&cError: The last argument must be a number.");
					sendUsage(sender);
					return;
				}

				TAB.getInstance().getConfig().getConfigurationSection("tablist-name-formatting.character-width-overrides").put(character, width);
				TAB.getInstance().getConfig().save();
				sendMessage(sender, "&2[TAB] Successfully set width of &6" + (char) character + " &2(&6" + character + "&2) to &6" + width + "&2 pixels.");
				if (TAB.getInstance().isDebugMode()) {
					execute(sender, new String[]{String.valueOf(character + 1)});
				}
			} else {
				sendUsage(sender);
			}
		} else {
			sendUsage(sender);
		}
	}

	public void sendInputCharacterIDs(TabPlayer sender, String input) {
		sendMessage(sender, "The provided input consists of following characters:");
		char[] chars = input.toCharArray();
		int[] ints = new int[chars.length];
		for (int i = 0; i < chars.length; i++) {
			ints[i] = chars[i];
		}
		sendMessage(sender, Arrays.toString(ints));
	}

	public void sendUsage(TabPlayer sender) {
		sendMessage(sender, "&cUsage 1: &f/tab width &7<character>");
		sendMessage(sender, "&cUsage 2: &f/tab width &7<character / character ID if > 10 > <amount>");
		sendMessage(sender, "&cUsage 3: &f/tab width set &7<character / character ID  if > 10> <new width>");
	}

	public void sendWidth(TabPlayer sender, char character, int amount) {
		List<IChatBaseComponent> messages = new ArrayList<>();
		IChatBaseComponent charMessage = new IChatBaseComponent(EnumChatFormat.color("&2" + character + " &d|"));

		messages.add(new IChatBaseComponent(EnumChatFormat.color("&b[TAB] Click the line with closest width &7(ID: &f" + (int) character + "&7)")));

		for (int i = 1; i <= amount; i++) {
			messages.add(getText(i, character));
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
	 * @param width - with to display
	 * @param c - character to set click action to
	 * @return line of text with characters that build specified text width
	 */
	private IChatBaseComponent getText(int width, int c) {
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
		IChatBaseComponent component = new IChatBaseComponent(EnumChatFormat.color("&b&k" + text + " &e|&b (" + width + " pixels) &7&l[Click to apply]"));
		component.getModifier().onClickRunCommand("/tab width set " + c + " " + width);
		component.getModifier().onHoverShowText(new IChatBaseComponent("Click to set width to " + width + " pixels"));
		return component;
	}


	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		List<String> suggestions = new ArrayList<>();
		if (arguments.length == 1) {
			suggestions.add("set");
			suggestions.add("<character (ID)>");
		} else if (arguments.length == 2) {
			if (arguments[0].equalsIgnoreCase("set")) {
				suggestions.add("<character (ID)>");
			} else {
				suggestions.add("<amount>");
			}
		} else if (arguments.length == 3) {
			if (arguments[0].equalsIgnoreCase("set")) {
				suggestions.add("<new width>");
			}
		}
		return getStartingArgument(suggestions, arguments[arguments.length - 1]);
	}
} 