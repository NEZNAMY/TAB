package me.neznamy.tab.shared.command.level1;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class ParseCommand extends SubCommand{

	public ParseCommand() {
		super("parse", "tab.parse");
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		if (args.length > 0) {
			String replaced = args[0];
			List<Placeholder> list = new ArrayList<Placeholder>();
			list.addAll(Placeholders.myServerPlaceholders.values());
			list.addAll(Placeholders.myPlayerPlaceholders.values());
			list.addAll(Placeholders.myServerConstants.values());
			for (Placeholder p : list) 
				if (replaced.contains(p.getIdentifier())) replaced = replaced.replace(p.getIdentifier(), p.getValue(sender));
			if (PluginHooks.placeholderAPI) replaced = PluginHooks.PlaceholderAPI_setPlaceholders(sender, replaced);
			sendMessage(sender, "&6Replacing placeholder &e" + args[0] + (sender == null ? "" : "&6 for player &e" + sender.getName()));
			sendMessage(sender, "&6Result: &r" + replaced);
		} else {
			sendMessage(sender, "Usage: /tab parse <placeholder>");
		}
	}
	@Override
	public List<String> complete(ITabPlayer sender, String[] arguments) {
		return new ArrayList<String>();
	}
}