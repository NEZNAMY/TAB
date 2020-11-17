package me.neznamy.tab.platforms.bukkit;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.neznamy.tab.platforms.bukkit.nms.NMSHook;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * Main class for Bukkit platform
 */
public class Main extends JavaPlugin {

	@Override
	public void onEnable(){
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.fromFriendlyName(Bukkit.getBukkitVersion().split("-")[0]);
		String serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		ProtocolVersion.UNKNOWN.setMinorVersion(Integer.parseInt(serverPackage.split("_")[1]));
		Bukkit.getConsoleSender().sendMessage("\u00a77[TAB] Server version: " + Bukkit.getBukkitVersion().split("-")[0] + " (" + serverPackage + ")");
		if (!NMSHook.isVersionSupported(serverPackage)){
			Shared.disabled = true;
			Bukkit.getConsoleSender().sendMessage("\u00a7c[TAB] Your server version is not supported. Disabling..");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		Shared.platform = new BukkitMethods(this);
		Bukkit.getPluginManager().registerEvents(new BukkitEventListener(), this);
		Bukkit.getPluginCommand("tab").setExecutor(new CommandExecutor() {
			public boolean onCommand(CommandSender sender, Command c, String cmd, String[] args){
				if (Shared.disabled) {
					for (String message : Shared.disabledCommand.execute(args, sender.hasPermission("tab.reload"), sender.hasPermission("tab.admin"))) {
						sender.sendMessage(Placeholders.color(message));
					}
				} else {
					Shared.cpu.runMeasuredTask("processing command", TabFeature.COMMAND_PROCESSING, UsageType.COMMAND_PREPROCESS, new Runnable() {

						@Override
						public void run() {
							Shared.command.execute(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args);
						}
					});
				}
				return false;
			}
		});
		Bukkit.getPluginCommand("tab").setTabCompleter(new TabCompleter() {
			public List<String> onTabComplete(CommandSender sender, Command c, String cmd, String[] args) {
				return Shared.command.complete(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args);
			}
		});
		Shared.load();
		BukkitMetrics.start(this);
	}

	@Override
	public void onDisable() {
		if (!Shared.disabled) {
			Shared.unload();
		}
	}

	public static void detectPlugins() {
		PluginHooks.placeholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
	}
}