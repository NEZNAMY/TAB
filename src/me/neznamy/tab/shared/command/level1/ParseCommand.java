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
			String replaced = "";
			for (int i=0; i<args.length; i++){
				if (i>0) replaced += " ";
				replaced += args[i];
			}
			sendMessage(sender, "&6Replacing placeholder &e" + replaced + (sender == null ? "" : "&6 for player &e" + sender.getName()));
			for (Placeholder p : Placeholders.getAllPlaceholders()) {
				if (replaced.contains(p.getIdentifier())) replaced = p.set(replaced, sender);
			}
			if (PluginHooks.placeholderAPI) replaced = PluginHooks.PlaceholderAPI_setPlaceholders(sender.getUniqueId(), replaced);
			
			sendMessage(sender, "With colors: " + replaced);
			sendRawMessage(sender, "Without colors: " + replaced.replace(Placeholders.colorChar, '&'));
		} else {
			sendMessage(sender, "Usage: /tab parse <placeholder>");
		}
	}
	@Override
	public List<String> complete(ITabPlayer sender, String[] arguments) {
		return new ArrayList<String>();
	}
}