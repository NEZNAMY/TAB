package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;

public abstract class PropertyCommand extends SubCommand {

	protected PropertyCommand(String name) {
		super(name, null);
	}
	
	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		if (arguments.length != 2) return new ArrayList<>();
		return getStartingArgument(Arrays.asList(getAllProperties()), arguments[1]);
	}
	
	protected void help(TabPlayer sender) {
		sendMessage(sender, "&cSyntax&8: &3&l/tab &9group&3/&9player &3<name> &9<property> &3<value...>");
		sendMessage(sender, "&7Valid Properties are:");
		sendMessage(sender, " - &9tabprefix&3/&9tabsuffix&3/&9customtabname");
		sendMessage(sender, " - &9tagprefix&3/&9tagsuffix&3/&9customtagname");
		sendMessage(sender, " - &9belowname&3/&9abovename");
	}
}