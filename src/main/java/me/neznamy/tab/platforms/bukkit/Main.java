package me.neznamy.tab.platforms.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.neznamy.tab.platforms.bukkit.nms.NMSHook;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.PlaceholderManager;

/**
 * Main class for Bukkit platform
 */
public class Main extends JavaPlugin {

	@Override
	public void onEnable(){
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.fromFriendlyName(Bukkit.getBukkitVersion().split("-")[0]);
		Bukkit.getConsoleSender().sendMessage("\u00a77[TAB] Server version: " + Bukkit.getBukkitVersion().split("-")[0] + " (" + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ")");
		if (!NMSHook.isVersionSupported()){
			Shared.disabled = true;
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		Shared.platform = new BukkitPlatform(this);
		Bukkit.getPluginManager().registerEvents(new BukkitEventListener(), this);
		Bukkit.getPluginCommand("tab").setExecutor((sender, c, cmd, args) -> {
			if (Shared.disabled) {
				for (String message : Shared.disabledCommand.execute(args, sender.hasPermission("tab.reload"), sender.hasPermission("tab.admin"))) {
					sender.sendMessage(PlaceholderManager.color(message));
				}
			} else {
				Shared.command.execute(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args);
			}
			return false;
		});
		Bukkit.getPluginCommand("tab").setTabCompleter((sender, c, cmd, args) -> Shared.command.complete(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args));
		Shared.load();
		BukkitMetrics.start(this);
	}

	@Override
	public void onDisable() {
		if (!Shared.disabled) {
			Shared.unload();
		}
	}
}