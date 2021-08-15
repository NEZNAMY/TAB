package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;

public abstract class PropertyCommand extends SubCommand {

	protected PropertyCommand(String name, String permission) {
		super(name, permission);
	}
	
	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		if (arguments.length != 2) return new ArrayList<>();
		return getStartingArgument(Arrays.asList(getAllProperties()), arguments[1]);
	}
}