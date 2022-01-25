package me.neznamy.tab.platforms.bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.viaversion.viaversion.api.Via;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Main class for Bukkit platform
 */
public class Main extends JavaPlugin {
	
	private boolean viaVersion;
	private boolean protocolSupport;
	
	@Override
	public void onEnable(){
		Bukkit.getConsoleSender().sendMessage(EnumChatFormat.color("&7[TAB] Server version: " + Bukkit.getBukkitVersion().split("-")[0] + " (" + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ")"));
		if (!isVersionSupported()){
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		viaVersion = Bukkit.getPluginManager().isPluginEnabled("ViaVersion");
		protocolSupport = Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport");
		TAB.setInstance(new TAB(new BukkitPlatform(this), ProtocolVersion.fromFriendlyName(Bukkit.getBukkitVersion().split("-")[0])));
		if (TAB.getInstance().getServerVersion() == ProtocolVersion.UNKNOWN) {
			Bukkit.getConsoleSender().sendMessage(EnumChatFormat.color("&c[TAB] Unknown server version: " + Bukkit.getBukkitVersion() + "! Plugin may not work correctly."));
		}
		Bukkit.getPluginManager().registerEvents(new BukkitEventListener(this), this);
		TAB.getInstance().load();
		Metrics metrics = new Metrics(this, 5304);
		metrics.addCustomChart(new SimplePie("unlimited_nametag_mode_enabled", () -> TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.UNLIMITED_NAME_TAGS) ? "Yes" : "No"));
		metrics.addCustomChart(new SimplePie("placeholderapi", () -> Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ? "Yes" : "No"));
		metrics.addCustomChart(new SimplePie("permission_system", () -> TAB.getInstance().getGroupManager().getPlugin().getName()));
		metrics.addCustomChart(new SimplePie("server_version", () -> "1." + TAB.getInstance().getServerVersion().getMinorVersion() + ".x"));
		PluginCommand cmd = Bukkit.getPluginCommand("tab");
		if (cmd == null) return;
		TABCommand command = new TABCommand();
		cmd.setExecutor(command);
		cmd.setTabCompleter(command);
	}

	@Override
	public void onDisable() {
		//null check due to L23 return making L30 not run
		if (TAB.getInstance() != null) TAB.getInstance().unload();
	}
	
	/**
	 * Initializes all used NMS classes, constructors, fields and methods and returns true if everything went successfully and version is marked as compatible
	 * @return true if compatible, false if not
	 */
	private boolean isVersionSupported(){
		List<String> supportedVersions = Arrays.asList(
				"v1_5_R1", "v1_5_R2", "v1_5_R3", "v1_6_R1", "v1_6_R2", "v1_6_R3",
				"v1_7_R1", "v1_7_R2", "v1_7_R3", "v1_7_R4", "v1_8_R1", "v1_8_R2", "v1_8_R3",
				"v1_9_R1", "v1_9_R2", "v1_10_R1", "v1_11_R1", "v1_12_R1", "v1_13_R1", "v1_13_R2",
				"v1_14_R1", "v1_15_R1", "v1_16_R1", "v1_16_R2", "v1_16_R3", "v1_17_R1", "v1_18_R1");
		String serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		try {
			long time = System.currentTimeMillis();
			NMSStorage.setInstance(new NMSStorage());
			if (supportedVersions.contains(serverPackage)) {
				Bukkit.getConsoleSender().sendMessage(EnumChatFormat.color("&7[TAB] Loaded NMS hook in " + (System.currentTimeMillis()-time) + "ms"));
				return true;
			} else {
				Bukkit.getConsoleSender().sendMessage(EnumChatFormat.color("&c[TAB] No compatibility issue was found, but this plugin version does not claim to support your server version. This jar has only been tested on 1.5.x - 1.18. Disabling just to stay safe."));
			}
		} catch (Exception ex) {
			if (supportedVersions.contains(serverPackage)) {
				Bukkit.getConsoleSender().sendMessage(EnumChatFormat.color("&c[TAB] Your server version is marked as compatible, but a compatibility issue was found. Please report the error below (include your server version & fork too)"));
				Bukkit.getConsoleSender().sendMessage(ex.getClass().getName() + ": " + ex.getMessage());
				for (StackTraceElement e : ex.getStackTrace()) {
					Bukkit.getConsoleSender().sendMessage("\t" + e.toString());
				}
			} else {
				Bukkit.getConsoleSender().sendMessage(EnumChatFormat.color("&c[TAB] Your server version is completely unsupported. This plugin version only supports 1.5.x - 1.18. Disabling."));
			}
		}
		return false;
	}
	
	/**
	 * Gets protocol version and returns it
	 * @return protocol version of this player
	 */
	public int getProtocolVersion(Player player) {
		if (protocolSupport){
			int version = getProtocolVersionPS(player);
			//some PS versions return -1 on unsupported server versions instead of throwing exception
			if (version != -1 && version < TAB.getInstance().getServerVersion().getNetworkId()) return version;
		}
		if (viaVersion) {
			return getProtocolVersionVia(player, 0);
		}
		return TAB.getInstance().getServerVersion().getNetworkId();
	}

	/**
	 * Returns protocol version of this player using ProtocolSupport
	 * @return protocol version of this player using ProtocolSupport
	 */
	private int getProtocolVersionPS(Player player){
		try {
			Object protocolVersion = Class.forName("protocolsupport.api.ProtocolSupportAPI").getMethod("getProtocolVersion", Player.class).invoke(null, player);
			int version = (int) protocolVersion.getClass().getMethod("getId").invoke(protocolVersion);
			TAB.getInstance().debug("ProtocolSupport returned protocol version " + version + " for " + player.getName() + "(online=" + player.isOnline() + ")");
			return version;
		} catch (ReflectiveOperationException e) {
			TAB.getInstance().getErrorManager().printError(String.format("Failed to get protocol version of %s using ProtocolSupport", player.getName()), e);
			return TAB.getInstance().getServerVersion().getNetworkId();
		}
	}

	/**
	 * Returns protocol version of this player using ViaVersion
	 * @return protocol version of this player using ViaVersion
	 */
	private int getProtocolVersionVia(Player player, int retryLevel){
		try {
			if (retryLevel == 10) {
				TAB.getInstance().debug("Failed to get protocol version of " + player.getName() + " after 10 retries");
				return TAB.getInstance().getServerVersion().getNetworkId();
			}
			int version = Via.getAPI().getPlayerVersion(player.getUniqueId());
			if (version == -1) {
				if (!player.isOnline()) return TAB.getInstance().getServerVersion().getNetworkId();
				Thread.sleep(5);
				return getProtocolVersionVia(player, retryLevel + 1);
			}
			TAB.getInstance().debug("ViaVersion returned protocol version " + version + " for " + player.getName() + "(online=" + player.isOnline() + ")");
			return version;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return -1;
		} catch (Exception | LinkageError e) {
			Plugin via = Bukkit.getPluginManager().getPlugin("ViaVersion");
			TAB.getInstance().getErrorManager().printError(String.format("Failed to get protocol version of %s using ViaVersion v%s", player.getName(), via == null ? "" : via.getDescription().getVersion()), e);
			return TAB.getInstance().getServerVersion().getNetworkId();
		}
	}
	
	public static class TABCommand implements CommandExecutor, TabCompleter {

		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
			if (TAB.getInstance().isDisabled()) {
				for (String message : TAB.getInstance().getDisabledCommand().execute(args, sender.hasPermission(TabConstants.Permission.COMMAND_RELOAD), sender.hasPermission(TabConstants.Permission.COMMAND_ALL))) {
					sender.sendMessage(EnumChatFormat.color(message));
				}
			} else {
				TabPlayer p = null;
				if (sender instanceof Player) {
					p = TAB.getInstance().getPlayer(((Player)sender).getUniqueId());
					if (p == null) return true; //player not loaded correctly
				}
				TAB.getInstance().getCommand().execute(p, args);
			}
			return false;
		}

		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
			TabPlayer p = null;
			if (sender instanceof Player) {
				p = TAB.getInstance().getPlayer(((Player)sender).getUniqueId());
				if (p == null) return new ArrayList<>(); //player not loaded correctly
			}
			return TAB.getInstance().getCommand().complete(p, args);
		}
	}
}