package me.neznamy.tab.shared.command;

import java.util.Arrays;

import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.command.level1.*;

public class TabCommand extends SubCommand {

	public TabCommand() {
		super("tab", null);
		subcommands.put("announce", new AnnounceCommand());
		subcommands.put("cpu", new CpuCommand());
		subcommands.put("debug", new DebugCommand());
		subcommands.put("group", new GroupCommand());
		subcommands.put("ntpreview", new NTPreviewCommand());
		subcommands.put("parse", new ParseCommand());
		subcommands.put("player", new PlayerCommand());
		subcommands.put("playeruuid", new PlayerUUIDCommand());
		subcommands.put("reload", new ReloadCommand());
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		if (Shared.disabled) {
			if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
				Shared.unload();
				Shared.load(true, false);
				if (!Shared.disabled) sendMessage(sender, Configs.reloaded);
			} else {
				sendMessage(sender, Configs.plugin_disabled);
			}
			return;
		}
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
	@Override
	public Object complete(ITabPlayer sender, String currentArgument) {
		return null;
	}
}