package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.earth2me.essentials.Essentials;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
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

	private final Plugin plugin;

	private Object chat;
	private final Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
	private Object server;
	private Field recentTps;
	private boolean paperTps;
	private boolean paperMspt;
	private boolean purpur;
	private Method playerIsAfk;

	private Listener healthListener = null;

	/**
	 * Constructs new instance with given parameter
	 * @param plugin - plugin instance
	 */
	public BukkitPlaceholderRegistry(Plugin plugin) {
		this.plugin = plugin;
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
			playerIsAfk = Player.class.getMethod("isAfk");
			purpur = true;
		} catch (NoSuchMethodException e) {
			//not purpur
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void registerPlaceholders(PlaceholderManager manager) {
		NumberFormat roundDown = NumberFormat.getNumberInstance(Locale.ENGLISH);
		roundDown.setRoundingMode(RoundingMode.DOWN);
		roundDown.setMaximumFractionDigits(2);
		manager.registerPlayerPlaceholder("%displayname%", 500, p -> ((Player) p.getPlayer()).getDisplayName());
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
			manager.registerServerPlaceholder("%tps%", -1, () -> "-1").enableTriggerMode();
		}
		if (paperMspt) {
			manager.registerServerPlaceholder("%mspt%", 1000, () -> roundDown.format(Bukkit.getAverageTickTime()));
		}
		manager.registerPlayerPlaceholder("%afk%", 500, p -> {
			if (essentials != null && ((Essentials)essentials).getUser(p.getUniqueId()).isAfk()) return true;
			if (!purpur || playerIsAfk == null) return false;
			try {
				return playerIsAfk.invoke(p.getPlayer());
			} catch (final IllegalAccessException | InvocationTargetException exception) {
				TAB.getInstance().getErrorManager().printError("Failed to invoke isAfk!", exception);
			}
			return false;
		});
		manager.registerPlayerPlaceholder("%essentialsnick%", 1000, p -> {
			String nickname = null;
			if (essentials != null)
				nickname = ((Essentials)essentials).getUser(p.getUniqueId()).getNickname();
			return nickname == null || nickname.length() == 0 ? p.getName() : nickname;
		});
		if (chat != null) {
			manager.registerPlayerPlaceholder("%vault-prefix%", 500, p -> ((Chat) chat).getPlayerPrefix((Player) p.getPlayer()));
			manager.registerPlayerPlaceholder("%vault-suffix%", 500, p -> ((Chat) chat).getPlayerSuffix((Player) p.getPlayer()));
		} else {
			manager.registerServerPlaceholder("%vault-prefix%", -1, () -> "").enableTriggerMode();
			manager.registerServerPlaceholder("%vault-suffix%", -1, () -> "").enableTriggerMode();
		}
		registerOnlinePlaceholders(manager);
		registerHealthPlaceholder(manager);
	}

	private String formatTPS(double tps) {
		return decimal2.format(Math.min(20, tps));
	}

	private void registerOnlinePlaceholders(PlaceholderManager manager) {
		manager.registerPlayerPlaceholder("%online%", 1000, p -> {
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

	@SuppressWarnings("deprecation")
	private void registerHealthPlaceholder(PlaceholderManager manager) {
		if (TAB.getInstance().getServerVersion().getMinorVersion() >= 6) {
			PlayerPlaceholder health = manager.registerPlayerPlaceholder("%health%", -1, p -> (int) Math.ceil(((Player) p.getPlayer()).getHealth()));
			health.enableTriggerMode(() -> {
				healthListener = new Listener() {

					@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
					public void onDamage(EntityDamageEvent e) {
						if (e.getEntity() instanceof Player) {
							Player p = (Player) e.getEntity();
							TabPlayer tabp = TAB.getInstance().getPlayer(e.getEntity().getUniqueId());
							if (tabp == null) return;
							health.updateValue(tabp, (int) Math.ceil(Math.max(p.getHealth() - e.getFinalDamage(), 0)));
						}
					}

					@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
					public void onRegain(EntityRegainHealthEvent e) {
						if (e.getEntity() instanceof Player) {
							Player p = (Player) e.getEntity();
							TabPlayer tabp = TAB.getInstance().getPlayer(e.getEntity().getUniqueId());
							if (tabp == null) return;
							health.updateValue(tabp, (int) Math.ceil(Math.min(p.getHealth() + e.getAmount(), p.getMaxHealth())));
						}
					}

					@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
					public void onRespawn(PlayerRespawnEvent e) {
						health.updateValue(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId()), (int) e.getPlayer().getMaxHealth());
					}
				};
				Bukkit.getPluginManager().registerEvents(healthListener, plugin);
			}, () -> HandlerList.unregisterAll(healthListener));
		}
	}
}
