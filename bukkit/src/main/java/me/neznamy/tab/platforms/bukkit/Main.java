package me.neznamy.tab.platforms.bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.viaversion.viaversion.api.Via;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;

/**
 * Main class for Bukkit platform
 */
public class Main extends JavaPlugin {
	
	@Override
	public void onEnable(){
		ProtocolVersion.SERVER_VERSION = ProtocolVersion.fromFriendlyName(Bukkit.getBukkitVersion().split("-")[0]);
		Bukkit.getConsoleSender().sendMessage("\u00a77[TAB] Server version: " + Bukkit.getBukkitVersion().split("-")[0] + " (" + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ")");
		if (!isVersionSupported()){
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		if (ProtocolVersion.SERVER_VERSION == ProtocolVersion.UNKNOWN) {
			Bukkit.getConsoleSender().sendMessage("\u00a7c[TAB] Unknown server version: " + Bukkit.getBukkitVersion() + "! Plugin may not work correctly.");
		}
		TAB.setInstance(new TAB(new BukkitPlatform(this, NMSStorage.getInstance()), new BukkitPacketBuilder(NMSStorage.getInstance())));
		Bukkit.getPluginManager().registerEvents(new BukkitEventListener(), this);
		Bukkit.getPluginCommand("tab").setExecutor((sender, c, cmd, args) -> {
			if (TAB.getInstance().isDisabled()) {
				for (String message : TAB.getInstance().disabledCommand.execute(args, sender.hasPermission("tab.reload"), sender.hasPermission("tab.admin"))) {
					sender.sendMessage(message.replace('&', '\u00a7'));
				}
			} else {
				TabPlayer p = null;
				if (sender instanceof Player) {
					p = TAB.getInstance().getPlayer(((Player)sender).getUniqueId());
					if (p == null) return false; //player not loaded correctly
				}
				TAB.getInstance().command.execute(p, args);
			}
			return false;
		});
		Bukkit.getPluginCommand("tab").setTabCompleter((sender, c, cmd, args) -> {
			TabPlayer p = null;
			if (sender instanceof Player) {
				p = TAB.getInstance().getPlayer(((Player)sender).getUniqueId());
				if (p == null) return new ArrayList<String>(); //player not loaded correctly
			}
			return TAB.getInstance().command.complete(p, args);
		});
		TAB.getInstance().load();
		new BukkitMetrics(this);
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
		List<String> SUPPORTED_VERSIONS = Arrays.asList(
				"v1_5_R1", "v1_5_R2", "v1_5_R3", "v1_6_R1", "v1_6_R2", "v1_6_R3",
				"v1_7_R1", "v1_7_R2", "v1_7_R3", "v1_7_R4", "v1_8_R1", "v1_8_R2", "v1_8_R3",
				"v1_9_R1", "v1_9_R2", "v1_10_R1", "v1_11_R1", "v1_12_R1", "v1_13_R1", "v1_13_R2",
				"v1_14_R1", "v1_15_R1", "v1_16_R1", "v1_16_R2", "v1_16_R3", "v1_17_R1");
		String serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		try {
			NMSStorage.setInstance(new NMSStorage());
			if (SUPPORTED_VERSIONS.contains(serverPackage)) {
				return true;
			} else {
				Bukkit.getConsoleSender().sendMessage("\u00a7c[TAB] No compatibility issue was found, but this plugin version does not claim to support your server version. This jar has only been tested on 1.5.x - 1.17. Disabling just to stay safe.");
			}
		} catch (Throwable e) {
			if (SUPPORTED_VERSIONS.contains(serverPackage)) {
				Bukkit.getConsoleSender().sendMessage("\u00a7c[TAB] Your server version is marked as compatible, but a compatibility issue was found. Please report the error below (include your server version & fork too)");
				e.printStackTrace();
			} else {
				Bukkit.getConsoleSender().sendMessage("\u00a7c[TAB] Your server version is completely unsupported. This plugin version only supports 1.5.x - 1.17. Disabling.");
			}
		}
		return false;
	}
	
	/**
	 * Gets protocol version and returns it
	 * @return protocol version of this player
	 */
	public static int getProtocolVersion(Player player) {
		if (Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport")){
			int version = getProtocolVersionPS(player);
			//some PS versions return -1 on unsupported server versions instead of throwing exception
			if (version != -1 && version < ProtocolVersion.SERVER_VERSION.getNetworkId()) return version;
		}
		if (((BukkitPlatform)TAB.getInstance().getPlatform()).viaversion) {
			return getProtocolVersionVia(player);
		}
		return ProtocolVersion.SERVER_VERSION.getNetworkId();
	}

	/**
	 * Returns protocol version of this player using ProtocolSupport
	 * @return protocol version of this player using ProtocolSupport
	 */
	private static int getProtocolVersionPS(Player player){
		try {
			Object protocolVersion = Class.forName("protocolsupport.api.ProtocolSupportAPI").getMethod("getProtocolVersion", Player.class).invoke(null, player);
			int version = (int) protocolVersion.getClass().getMethod("getId").invoke(protocolVersion);
			TAB.getInstance().debug("ProtocolSupport returned protocol version " + version + " for " + player.getName() + "(online=" + player.isOnline() + ")");
			return version;
		} catch (Throwable e) {
			TAB.getInstance().getErrorManager().printError("Failed to get protocol version of " + player.getName() + " using ProtocolSupport", e);
			return ProtocolVersion.SERVER_VERSION.getNetworkId();
		}
	}

	/**
	 * Returns protocol version of this player using ViaVersion
	 * @return protocol version of this player using ViaVersion
	 */
	private static int getProtocolVersionVia(Player player){
		try {
			int version = Via.getAPI().getPlayerVersion(player.getUniqueId());
			TAB.getInstance().debug("ViaVersion returned protocol version " + version + " for " + player.getName() + "(online=" + player.isOnline() + ")");
			return version;
		} catch (Throwable e) {
			TAB.getInstance().getErrorManager().printError("Failed to get protocol version of " + player.getName() + " using ViaVersion v" + Bukkit.getPluginManager().getPlugin("ViaVersion").getDescription().getVersion(), e);
			return ProtocolVersion.SERVER_VERSION.getNetworkId();
		}
	}
}