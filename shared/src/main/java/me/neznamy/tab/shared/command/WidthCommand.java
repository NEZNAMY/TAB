package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;

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
			sendWidth(sender,c ,  10);
		} else if (args.length == 2) {
			int c, width;
			try{
				c = Integer.parseInt(args[0]);
				width = Integer.parseInt(args[1]);
			}catch (NumberFormatException e){
				//Sender probably tried to enter the character as the first argument and the amount of width lines displayed as the second argument:
				int amount;
				try{
					amount = Integer.parseInt(args[1]);
				}catch (NumberFormatException ignored){
					sendMessage(sender, "&cUsage 1: &f/tab width &7<character>");
					sendMessage(sender, "&cUsage 2: &f/tab width &7<character> <amount>");
					sendMessage(sender, "&cUsage 3: &f/tab width &7<character ID> <new width>");
					return;
				}

				sendWidth(sender, args[0].charAt(0), amount);
				return;
			}
			TAB.getInstance().getConfig().getConfigurationSection("tablist-name-formatting.character-width-overrides").put(c, width);
			TAB.getInstance().getConfig().save();
			sendMessage(sender, "&2[TAB] Successfully set width of &6" + (char)c + " &2(&6" + c + "&2) to &6" + width + "&2 pixels.");
			if (TAB.getInstance().isDebugMode()) {
				execute(sender, new String[] {String.valueOf(c+1)});
			}
		} else {
			sendMessage(sender, "&cUsage 1: &f/tab width &7<character>");
			sendMessage(sender, "&cUsage 2: &f/tab width &7<character> <amount>");
			sendMessage(sender, "&cUsage 3: &f/tab width &7<character ID> <new width>");
		}
	}

	public void sendWidth(TabPlayer sender, char c, int amount){
		List<IChatBaseComponent> messages = new ArrayList<>();
		IChatBaseComponent charMessage = new IChatBaseComponent(EnumChatFormat.color("&2" + c + " &d|"));

		messages.add(new IChatBaseComponent(EnumChatFormat.color("&b[TAB] Click the line with closest width &7(ID: &f" + (int)c + "&7)" )));

		for(int i=1; i<=amount; i++){
			messages.add(getText(i, c));
			if(i % 2 != 0){
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
		component.getModifier().onClickRunCommand("/tab width " + c + " " + width);
		component.getModifier().onHoverShowText(new IChatBaseComponent("Click to set width to " + width + " pixels"));
		return component;
	}
} 