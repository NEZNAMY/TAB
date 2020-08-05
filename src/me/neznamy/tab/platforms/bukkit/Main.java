package me.neznamy.tab.platforms.bukkit;

import java.util.Collection;
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
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class Main extends JavaPlugin {

	public static Main INSTANCE;
	public final static String serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

	@Override
	public void onEnable(){
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.fromServerString(Bukkit.getBukkitVersion().split("-")[0]);
		Shared.print('7', "Server version: " + Bukkit.getBukkitVersion().split("-")[0] + " (" + serverPackage + ")");
		if (NMSHook.isVersionSupported(serverPackage)){
			INSTANCE = this;
			Shared.platform = new BukkitMethods();
			Bukkit.getPluginManager().registerEvents(new BukkitEventListener(), this);
			Bukkit.getPluginCommand("tab").setExecutor(new CommandExecutor() {
				public boolean onCommand(CommandSender sender, Command c, String cmd, String[] args){
					if (Configs.bukkitBridgeMode || Shared.disabled) {
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
								if (Configs.bukkitBridgeMode) sender.sendMessage(Placeholders.color(" &6&lBukkit bridge mode activated"));
								if (Shared.disabled) sender.sendMessage(Placeholders.color(" &c&lPlugin is disabled due to a broken configuration file (" + Shared.brokenFile + ")"));
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
					if (Configs.bukkitBridgeMode) {
						return null;
					}
					return Shared.command.complete(sender instanceof Player ? Shared.getPlayer(((Player)sender).getUniqueId()) : null, args);
				}
			});
			Shared.load(true);
			Metrics.start(this);
		} else {
			Shared.disabled = true;
			Shared.platform.sendConsoleMessage("&c[TAB] Your server version is not supported. Disabling..");
			Bukkit.getPluginManager().disablePlugin(this);
		}
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
			if (Configs.bukkitBridgeMode) Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
		}
	}

	
	public static void inject(UUID player) {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			Injector.inject(player);
		}
	}

	@SuppressWarnings("unchecked")
	public boolean killPacket(Object packetPlayOutScoreboardTeam) throws Exception{
		if (PacketPlayOutScoreboardTeam.SIGNATURE.getInt(packetPlayOutScoreboardTeam) != 69) {
			Collection<String> players = (Collection<String>) PacketPlayOutScoreboardTeam.PLAYERS.get(packetPlayOutScoreboardTeam);
			for (ITabPlayer p : Shared.getPlayers()) {
				if (players.contains(p.getName()) && !p.disabledNametag) {
					return true;
				}
			}
		}
		return false;
	}
	public static void detectPlugins() {
		if (Bukkit.getPluginManager().isPluginEnabled("iDisguise")) {
			RegisteredServiceProvider<?> provider = Bukkit.getServicesManager().getRegistration(DisguiseAPI.class);
			if (provider != null) PluginHooks.idisguise = provider.getProvider();
		}
		PluginHooks.placeholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
	}
	@SuppressWarnings("unchecked")
	public static Player[] getOnlinePlayers(){
		try {
			Object players = Bukkit.class.getMethod("getOnlinePlayers").invoke(null);
			if (players instanceof Player[]) {
				//1.5.x - 1.6.x
				return (Player[]) players;
			} else {
				//1.7+
				return ((Collection<Player>)players).toArray(new Player[0]); 
			}
		} catch (Exception e) {
			return Shared.errorManager.printError(new Player[0], "Failed to get online players");
		}
	}
}