package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;

/**
 * The core command handler
 */
public class TabCommand extends SubCommand {

	//tab instance
	private final TAB tab;
	
	/**
	 * Constructs new instance with given parameter and registers all subcommands
	 * @param tab - tab instance
	 */
	public TabCommand(TAB tab) {
		super("tab", null);
		this.tab = tab;
		registerSubCommand(new AnnounceCommand());
		registerSubCommand(new BossBarCommand());
		registerSubCommand(new CpuCommand());
		registerSubCommand(new DebugCommand());
		registerSubCommand(new GroupCommand());
		registerSubCommand(new NTPreviewCommand());
		registerSubCommand(new ParseCommand());
		registerSubCommand(new PlayerCommand());
		registerSubCommand(new PlayerUUIDCommand());
		registerSubCommand(new ReloadCommand());
		registerSubCommand(new SendCommand());
		registerSubCommand(new SetCollisionCommand());
		registerSubCommand(new ScoreboardCommand());
		registerSubCommand(new WidthCommand());
		List<String> properties = Lists.newArrayList(TabConstants.Property.TABPREFIX, TabConstants.Property.TABSUFFIX, TabConstants.Property.TAGPREFIX, TabConstants.Property.TAGSUFFIX, TabConstants.Property.CUSTOMTABNAME, TabConstants.Property.ABOVENAME, TabConstants.Property.BELOWNAME, TabConstants.Property.CUSTOMTAGNAME);
		for (Object line : ((DebugCommand)getSubcommands().get("debug")).getExtraLines()) {
			properties.add(line.toString());
		}
		SubCommand.setAllProperties(properties.toArray(new String[0]));
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		if (args.length > 0) {
			String arg0 = args[0];
			SubCommand command = getSubcommands().get(arg0.toLowerCase());
			if (command != null) {
				if (command.hasPermission(sender)) {
					command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
				} else {
					sendMessage(sender, getMessages().getNoPermission());
				}
			} else {
				help(sender);
			}
		} else {
			help(sender);
		}
	}
	
	/**
	 * Sends help menu to the sender
	 * @param sender - player who ran command or null if from console
	 */
	private void help(TabPlayer sender){
		if (hasPermission(sender, TabConstants.Permission.COMMAND_ALL)) {
			if (sender != null) {
				IChatBaseComponent component = new IChatBaseComponent(EnumChatFormat.color("&3TAB v") + TAB.PLUGIN_VERSION);
				component.getModifier().onHoverShowText(new IChatBaseComponent(EnumChatFormat.color("&aClick to visit plugin's page")));
				component.getModifier().onClickOpenUrl("https://www.mc-market.org/resources/14009/");
				component.addExtra(new IChatBaseComponent(EnumChatFormat.color("&0 by _NEZNAMY_")));
				sender.sendMessage(component);
			} else {
				tab.getPlatform().sendConsoleMessage("&3TAB v" + TAB.PLUGIN_VERSION, true);
			}
			for (String message : getMessages().getHelpMenu()) {
				if (tab.getPlatform().isProxy()) message = message.replace("/tab", "/btab");
				sendMessage(sender, message);
			}
		}
	}
	
	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		if (!hasPermission(sender, TabConstants.Permission.COMMAND_AUTOCOMPLETE)) return new ArrayList<>();
		return super.complete(sender, arguments);
	}
}