package me.neznamy.tab.platforms.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.neznamy.tab.platforms.bukkit.nms.NMSHook;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.DisabledCommand;

/**
 * Main class for Bukkit platform
 */
public class Main extends JavaPlugin {

	private DisabledCommand disabledCommand = new DisabledCommand();
	
	@Override
	public void onEnable(){
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.fromFriendlyName(Bukkit.getBukkitVersion().split("-")[0]);
		Bukkit.getConsoleSender().sendMessage("\u00a77[TAB] Server version: " + Bukkit.getBukkitVersion().split("-")[0] + " (" + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ")");
		if (!NMSHook.isVersionSupported()){
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		TAB.setInstance(new TAB(new BukkitPlatform(this, NMSHook.nms), new BukkitPacketBuilder(NMSHook.nms)));
		Bukkit.getPluginManager().registerEvents(new BukkitEventListener(), this);
		Bukkit.getPluginCommand("tab").setExecutor((sender, c, cmd, args) -> {
			if (TAB.getInstance() == null) {
				for (String message : disabledCommand.execute(args, sender.hasPermission("tab.reload"), sender.hasPermission("tab.admin"))) {
					sender.sendMessage(TAB.getInstance().getPlaceholderManager().color(message));
				}
			} else {
				TAB.getInstance().command.execute(sender instanceof Player ? TAB.getInstance().getPlayer(((Player)sender).getUniqueId()) : null, args);
			}
			return false;
		});
		Bukkit.getPluginCommand("tab").setTabCompleter((sender, c, cmd, args) -> TAB.getInstance().command.complete(sender instanceof Player ? TAB.getInstance().getPlayer(((Player)sender).getUniqueId()) : null, args));
		TAB.getInstance().load();
		new BukkitMetrics(this);
	}

	@Override
	public void onDisable() {
		if (TAB.getInstance() != null) TAB.getInstance().unload();
	}
}