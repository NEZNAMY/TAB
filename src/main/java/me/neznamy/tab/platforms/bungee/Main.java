package me.neznamy.tab.platforms.bungee;

import java.util.ArrayList;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PluginMessageHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

/**
 * Main class for BungeeCord platform
 */
public class Main extends Plugin {

	//plugin message handler
	public static PluginMessageHandler plm;

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable(){
		if (!isVersionSupported()) {
			ProxyServer.getInstance().getConsole().sendMessage("\u00a7c[TAB] The plugin requires BungeeCord build #1330 and up to work. Get it at https://ci.md-5.net/job/BungeeCord/");
			return;
		}
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.values()[1];
		TAB.setInstance(new TAB(new BungeePlatform(this), new BungeePacketBuilder()));
		getProxy().getPluginManager().registerListener(this, new BungeeEventListener());
		if (getProxy().getPluginManager().getPlugin("PremiumVanish") != null) getProxy().getPluginManager().registerListener(this, new PremiumVanishListener());
		getProxy().getPluginManager().registerCommand(this, new BTABCommand());
		plm = new BungeePluginMessageHandler(this);
		TAB.getInstance().load();
		new BungeeMetrics(this);
	}
	
	/**
	 * Checks for compatibility and returns true if version is supported, false if not
	 * @return true if version is compatible, false if not
	 */
	private boolean isVersionSupported() {
		try {
			Class.forName("net.md_5.bungee.protocol.packet.ScoreboardObjective$HealthDisplay");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	@Override
	public void onDisable() {
		TAB.getInstance().unload();
	}
	
	public class BTABCommand extends Command implements TabExecutor {

		public BTABCommand() {
			super("btab", null);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void execute(CommandSender sender, String[] args) {
			if (TAB.getInstance().isDisabled()) {
				for (String message : TAB.getInstance().disabledCommand.execute(args, sender.hasPermission("tab.reload"), sender.hasPermission("tab.admin"))) {
					sender.sendMessage(TAB.getInstance().getPlaceholderManager().color(message));
				}
			} else {
				TAB.getInstance().command.execute(sender instanceof ProxiedPlayer ? TAB.getInstance().getPlayer(((ProxiedPlayer)sender).getUniqueId()) : null, args);
			}
		}

		@Override
		public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
			if (TAB.getInstance().isDisabled()) return new ArrayList<String>();
			return TAB.getInstance().command.complete(sender instanceof ProxiedPlayer ? TAB.getInstance().getPlayer(((ProxiedPlayer)sender).getUniqueId()) : null, args);
		}
	}
}