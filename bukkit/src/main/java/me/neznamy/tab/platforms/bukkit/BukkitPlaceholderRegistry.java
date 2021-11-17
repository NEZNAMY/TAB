package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.Field;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.earth2me.essentials.Essentials;

import me.neznamy.tab.api.PlaceholderManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;
import net.milkbowl.vault.chat.Chat;

/**
 * Bukkit registry to register bukkit-only placeholders
 */
public class BukkitPlaceholderRegistry implements PlaceholderRegistry {

	//formatter for 2 decimal places
	public final DecimalFormat decimal2 = ((DecimalFormat)NumberFormat.getNumberInstance(Locale.US));

	private Object chat;
	private Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
	private Object server;
	private Field recentTps;
	private boolean paperTps;
	private boolean paperMspt;
	private boolean purpur;

	/**
	 * Constructs new instance with given parameter
	 * @param plugin - plugin instance
	 */
	public BukkitPlaceholderRegistry() {
		decimal2.applyPattern("#.##");
		try {
			if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
				RegisteredServiceProvider<?> rspChat = Bukkit.getServicesManager().getRegistration(Class.forName("net.milkbowl.vault.chat.Chat"));
				if (rspChat != null) chat = rspChat.getProvider();
			}
		} catch (ClassNotFoundException e) {
			//modded server without vault
		}
		try {
			server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
			recentTps = server.getClass().getField("recentTps");
		} catch (ReflectiveOperationException e) {
			//not spigot
		}
		try {
			Bukkit.class.getMethod("getTPS");
			paperTps = true;
		} catch (NoSuchMethodException e) {
			//not paper
		}
		try {
			Bukkit.class.getMethod("getAverageTickTime");
			paperMspt = true;
		} catch (NoSuchMethodException e) {
			//not paper
		}
		try {
			Player.class.getMethod("isAfk");
			purpur = true;
		} catch (NoSuchMethodException e) {
			//not purpur
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void registerPlaceholders(PlaceholderManager manager) {
		NumberFormat roundDown = NumberFormat.getInstance();
		roundDown.setRoundingMode(RoundingMode.DOWN);
		roundDown.setMaximumFractionDigits(2);
		manager.registerPlayerPlaceholder("%displayname%", 500, p -> ((Player) p.getPlayer()).getDisplayName());
		manager.registerPlayerPlaceholder("%vanished%", 1000, TabPlayer::isVanished);
		if (paperTps) {
			manager.registerServerPlaceholder("%tps%", 1000, () -> formatTPS(Bukkit.getTPS()[0]));
		} else if (recentTps != null) {
			manager.registerServerPlaceholder("%tps%", 1000, () -> {
				try {
					return formatTPS(((double[]) recentTps.get(server))[0]);
				} catch (IllegalAccessException e) {
					return "-1";
				}
			});
		} else {
			manager.registerServerPlaceholder("%tps%", 1000000000, () -> "-1");
		}
		if (paperMspt) {
			manager.registerServerPlaceholder("%mspt%", 1000, () -> roundDown.format(Bukkit.getAverageTickTime()));
		}
		if (NMSStorage.getInstance().getMinorVersion() >= 6) {
			manager.registerPlayerPlaceholder("%health%", 100, p -> (int) Math.ceil(((Player) p.getPlayer()).getHealth()));
		}
		manager.registerPlayerPlaceholder("%afk%", 500, p -> {
			if (essentials != null && ((Essentials)essentials).getUser(p.getUniqueId()).isAfk()) return true;
			return purpur && ((Player)p.getPlayer()).isAfk();
		});
		if (chat != null) {
			manager.registerPlayerPlaceholder("%vault-prefix%", 500, p -> ((Chat) chat).getPlayerPrefix((Player) p.getPlayer()));
			manager.registerPlayerPlaceholder("%vault-suffix%", 500, p -> ((Chat) chat).getPlayerSuffix((Player) p.getPlayer()));
		} else {
			manager.registerServerPlaceholder("%vault-prefix%", 1000000, () -> "");
			manager.registerServerPlaceholder("%vault-suffix%", 1000000, () -> "");
		}
		registerOnlinePlaceholders(manager);
	}
	
	private String formatTPS(double tps) {
		return decimal2.format(Math.min(20, tps));
	}

	private void registerOnlinePlaceholders(PlaceholderManager manager) {
		manager.registerPlayerPlaceholder("%online%", 2000, p -> {
			int count = 0;
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
				if (((Player) p.getPlayer()).canSee((Player) all.getPlayer())) count++;
			}
			return count;
		});
		manager.registerPlayerPlaceholder("%staffonline%", 2000, p -> {
			int count = 0;
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
				if (all.hasPermission(TabConstants.Permission.STAFF) && ((Player) p.getPlayer()).canSee((Player) all.getPlayer())) count++;
			}
			return count;
		});
		manager.registerPlayerPlaceholder("%nonstaffonline%", 2000, p -> {
			int count = 0;
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
				if (!all.hasPermission(TabConstants.Permission.STAFF) && ((Player) p.getPlayer()).canSee((Player) all.getPlayer())) count++;
			}
			return count;
		});
	}
}