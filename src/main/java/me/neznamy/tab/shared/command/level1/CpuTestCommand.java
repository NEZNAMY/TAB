package me.neznamy.tab.shared.command.level1;

import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;

/**
 * Handler for "/tab cputest" subcommand
 */
public class CpuTestCommand extends SubCommand {
	
	public CpuTestCommand() {
		super("cputest", "tab.cputest");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		Shared.cpu.runTask("testing cpu power", new Runnable() {

			@Override
			public void run() {
				sendMessage(sender, "&9Performing CPU test");
				long time = System.currentTimeMillis();
				test();
				sendMessage(sender, "&9Task took &6" + (System.currentTimeMillis()-time) + "ms &9to process");
			}
		});
	}
	
	public void test() {
		String test = UUID.randomUUID().toString();
		int i=0;
		while (i < 100000000) {
			i++;
			if (test.contains("test")) {
				test = "x";
			}
		}
	}
}