package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;

public abstract class PropertyCommand extends SubCommand {

	protected PropertyCommand(String name, String permission) {
		super(name, permission);
	}
	
	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		List<String> suggestions = new ArrayList<>();
		if (arguments.length != 2) return suggestions;
		for (String property : getAllProperties()) {
			if (property.startsWith(arguments[1].toLowerCase())) suggestions.add(property);
		}
		return suggestions;
	}
}