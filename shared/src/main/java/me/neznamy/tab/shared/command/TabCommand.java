package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PropertyUtils;
import me.neznamy.tab.shared.TAB;

/**
 * The core command handler
 */
public class TabCommand extends SubCommand {

	//tab instance
	private TAB tab;
	
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
		List<String> properties = Lists.newArrayList(PropertyUtils.TABPREFIX, PropertyUtils.TABSUFFIX, PropertyUtils.TAGPREFIX, PropertyUtils.TAGSUFFIX, PropertyUtils.CUSTOMTABNAME, PropertyUtils.ABOVENAME, PropertyUtils.BELOWNAME, PropertyUtils.CUSTOMTAGNAME);
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
					sendMessage(sender, getTranslation("no_permission"));
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
		if (sender == null) tab.getPlatform().sendConsoleMessage("&3TAB v" + TAB.PLUGIN_VERSION, true);
		if ((sender == null || sender.hasPermission("tab.admin"))) {
			String command = !tab.getPlatform().isProxy() ? "/tab" : "/btab";
			String prefix = " &8>> &3&l";
			sendMessage(sender, "&m                                                                                ");
			sendMessage(sender, prefix + command + " reload");
			sendMessage(sender, "      - &7Reloads plugin and config");
			sendMessage(sender, prefix + command + " &9group&3/&9player &3<name> &9<property> &3<value...>");
			sendMessage(sender, "      - &7Do &8/tab group/player &7to show properties");
			sendMessage(sender, prefix + command + " ntpreview");
			sendMessage(sender, "      - &7Shows your nametag for yourself, for testing purposes");
			sendMessage(sender, prefix + command + " announce bar &3<name> &9<seconds>");
			sendMessage(sender, "      - &7Temporarily displays bossbar to all players");
			sendMessage(sender, prefix + command + " parse <placeholder> ");
			sendMessage(sender, "      - &7Test if a placeholder works");
			sendMessage(sender, prefix + command + " debug [player]");
			sendMessage(sender, "      - &7displays debug information about player");
			sendMessage(sender, prefix + command + " cpu");
			sendMessage(sender, "      - &7shows CPU usage of the plugin");
			sendMessage(sender, prefix + command + " group/player <name> remove");
			sendMessage(sender, "      - &7Clears all data about player/group");
			sendMessage(sender, "&m                                                                                ");
		}
	}
	
	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		if (!hasPermission(sender, "tab.tabcomplete")) return new ArrayList<>();
		return super.complete(sender, arguments);
	}
}