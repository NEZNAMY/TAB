package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.level1.AnnounceCommand;
import me.neznamy.tab.shared.command.level1.BossBarCommand;
import me.neznamy.tab.shared.command.level1.CpuCommand;
import me.neznamy.tab.shared.command.level1.DebugCommand;
import me.neznamy.tab.shared.command.level1.GroupCommand;
import me.neznamy.tab.shared.command.level1.NTPreviewCommand;
import me.neznamy.tab.shared.command.level1.ParseCommand;
import me.neznamy.tab.shared.command.level1.PlayerCommand;
import me.neznamy.tab.shared.command.level1.PlayerUUIDCommand;
import me.neznamy.tab.shared.command.level1.ReloadCommand;
import me.neznamy.tab.shared.command.level1.ScoreboardCommand;
import me.neznamy.tab.shared.command.level1.WidthCommand;
import me.neznamy.tab.shared.config.Configs;

public class TabCommand extends SubCommand {
	
	public TabCommand() {
		super("tab", null);
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
		if (Premium.is()) {
			registerSubCommand(new ScoreboardCommand());
			registerSubCommand(new WidthCommand());
		}
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		if (args.length > 0) {
			String arg0 = args[0];
			SubCommand command = subcommands.get(arg0.toLowerCase());
			if (command != null) {
				if (command.hasPermission(sender)) {
					command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
				} else {
					sendMessage(sender, Configs.no_perm);
				}
			} else {
				help(sender);
			}
		} else {
			help(sender);
		}
	}
	private void help(ITabPlayer sender){
		if (sender == null) Shared.mainClass.sendConsoleMessage("&3TAB v" + Shared.pluginVersion);
		if (sender == null || sender.hasPermission("tab.admin") && !Shared.disabled) {
			  sendMessage(sender, "&m                                                                                ");
			  sendMessage(sender, " &8>> &3&l/tab reload");
			  sendMessage(sender, "      - &7Reloads plugin and config");
			  sendMessage(sender, " &8>> &3&l/tab &9group&3/&9player &3<name> &9<property> &3<value...>");
			  sendMessage(sender, "      - &7Do &8/tab group/player &7to show properties");
			  sendMessage(sender, " &8>> &3&l/tab ntpreview");
			  sendMessage(sender, "      - &7Shows your nametag for yourself, for testing purposes");
			  sendMessage(sender, " &8>> &3&l/tab announce bar &3<name> &9<seconds>");
			  sendMessage(sender, "      - &7Temporarily displays bossbar to all players");
			  sendMessage(sender, " &8>> &3&l/tab parse <placeholder> ");
			  sendMessage(sender, "      - &7Test if a placeholder works");
			  sendMessage(sender, " &8>> &3&l/tab debug [player]");
			  sendMessage(sender, "      - &7displays debug information about player");
			  sendMessage(sender, " &8>> &3&l/tab cpu");
			  sendMessage(sender, "      - &7shows CPU usage of the plugin");
			  sendMessage(sender, " &8>> &4&l/tab group/player <name> remove");
			  sendMessage(sender, "      - &7Clears all data about player/group");
			  sendMessage(sender, "&m                                                                                ");
		}
	}
	public List<String> complete(ITabPlayer sender, String[] arguments) {
		if (!Configs.SECRET_autoComplete) return new ArrayList<String>();
		return super.complete(sender, arguments);
	}
}