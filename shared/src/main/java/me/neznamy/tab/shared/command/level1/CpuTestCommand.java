package me.neznamy.tab.shared.command.level1;

import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.command.SubCommand;

/**
 * Handler for "/tab cputest" subcommand
 */
public class CpuTestCommand extends SubCommand {
	
	/**
	 * Constructs new instance
	 */
	public CpuTestCommand() {
		super("cputest", "tab.cputest");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		new Thread(() -> {

			sendMessage(sender, "&9Performing CPU test");
			long time = System.currentTimeMillis();
			test();
			sendMessage(sender, "&9Task took &6" + (System.currentTimeMillis()-time) + "ms &9to process");
		}).start();
	}
	
	/**
	 * Performs the test
	 */
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