package me.neznamy.tab.shared.command.level1;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;

public class ReloadCommand extends SubCommand{

	public ReloadCommand() {
		super("reload", "tab.reload");
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		Shared.unload();
		Shared.load(false);
		if (!Shared.disabled) sendMessage(sender, Configs.reloaded);
	}
	@Override
	public List<String> complete(ITabPlayer sender, String[] arguments) {
		return new ArrayList<String>();
	}
}