package me.neznamy.tab.platforms.bukkit;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import de.robingrether.idisguise.api.DisguiseAPI;
import me.neznamy.tab.platforms.bukkit.packets.NMSHook;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * Main class for Bukkit platform
 */
public class Main extends JavaPlugin {

	public static final String serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

	@Override
	public void onEnable(){
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.fromServerString(Bukkit.getBukkitVersion().split("-")[0]);
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
					if (args.length == 1 && args[0].toLowerCase().equals("reload")) {
						if (sender.hasPermission("tab.reload")) {
							Shared.unload();
							Shared.load(false);
							if (Shared.disabled) {
								if (sender instanceof Player) {
									sender.sendMessage(Placeholders.color(Configs.reloadFailed.replace("%file%", Shared.brokenFile)));
								}
							} else {
								sender.sendMessage(Placeholders.color(Configs.reloaded));
							}
						} else {
							sender.sendMessage(Placeholders.color(Configs.no_perm));
						}
					} else {
						if (sender.hasPermission("tab.admin")) {
							sender.sendMessage(Placeholders.color("&m                                                                                "));
							sender.sendMessage(Placeholders.color(" &c&lPlugin is disabled due to a broken configuration file (" + Shared.brokenFile + ")"));
							sender.sendMessage(Placeholders.color(" &8>> &3&l/tab reload"));
							sender.sendMessage(Placeholders.color("      - &7Reloads plugin and config"));
							sender.sendMessage(Placeholders.color("&m                                                                                "));
						}
					}
				} else {
					Shared.command.execute(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args);
				}
				return false;
			}
		});
		Bukkit.getPluginCommand("tab").setTabCompleter(new TabCompleter() {
			public List<String> onTabComplete(CommandSender sender, Command c, String cmd, String[] args) {
				return Shared.command.complete(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args);
			}
		});
		Shared.load(true);
		BukkitMetrics.start(this);
	}

	@Override
	public void onDisable() {
		if (!Shared.disabled) {
			for (ITabPlayer p : Shared.getPlayers()) {
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
					Injector.uninject(p.getUniqueId());
				}
			}
			Shared.unload();
		}
	}

	public static void inject(UUID player) {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			Injector.inject(player);
		}
	}

	public static void detectPlugins() {
		if (Bukkit.getPluginManager().isPluginEnabled("iDisguise")) {
			RegisteredServiceProvider<?> provider = Bukkit.getServicesManager().getRegistration(DisguiseAPI.class);
			if (provider != null) PluginHooks.idisguise = provider.getProvider();
		}
		PluginHooks.placeholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
	}
}