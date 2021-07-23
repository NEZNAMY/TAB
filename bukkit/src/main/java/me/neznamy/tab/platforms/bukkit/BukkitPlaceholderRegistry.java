package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.neznamy.tab.api.PlaceholderManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;
import net.milkbowl.vault.chat.Chat;

/**
 * Bukkit registry to register bukkit-only placeholders
 */
public class BukkitPlaceholderRegistry implements PlaceholderRegistry {

	//formatter for 2 decimal places
	public final DecimalFormat decimal2 = ((DecimalFormat)NumberFormat.getNumberInstance(Locale.US));

	//plugin instance
	private JavaPlugin plugin;

	//vault chat
	private Chat chat;

	private Object essentials;
	
	//essentials methods
	private Method essGetUser;
	private Method essIsAfk;
	
	private Method purpurIsAfk;

	/**
	 * Constructs new instance with given parameter
	 * @param plugin - plugin instance
	 */
	public BukkitPlaceholderRegistry(JavaPlugin plugin) {
		decimal2.applyPattern("#.##");
		this.plugin = plugin;
		essentials = Bukkit.getPluginManager().getPlugin("Essentials");
		if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			RegisteredServiceProvider<Chat> rspChat = Bukkit.getServicesManager().getRegistration(Chat.class);
			if (rspChat != null) chat = rspChat.getProvider();
		}
		if (essentials != null) {
			try {
				essGetUser = Class.forName("com.earth2me.essentials.Essentials").getMethod("getUser", Player.class);
				essIsAfk = Class.forName("com.earth2me.essentials.User").getMethod("isAfk");
			} catch (Exception e) {
				TAB.getInstance().getErrorManager().printError("Failed to load essentials methods", e);
			}
		}
	}

	@Override
	public void registerPlaceholders(PlaceholderManager manager) {
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%displayname%", 500) {
			public String get(TabPlayer p) {
				return ((Player) p.getPlayer()).getDisplayName();
			}
		});
		manager.registerServerPlaceholder(new ServerPlaceholder("%tps%", 1000) {
			public String get() {
				try {
					Object nmsServer = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
					double value = ((double[]) nmsServer.getClass().getField("recentTps").get(nmsServer))[0];
					return decimal2.format(Math.min(20, value));
				} catch (Exception t) {
					return "-1";
				}
			}
		});
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%online%", 2000) {
			public String get(TabPlayer p) {
				int count = 0;
				for (TabPlayer all : TAB.getInstance().getPlayers()){
					if (((Player) p.getPlayer()).canSee((Player) all.getPlayer())) count++;
				}
				return String.valueOf(count);
			}
		});
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%staffonline%", 2000) {
			public String get(TabPlayer p) {
				int count = 0;
				for (TabPlayer all : TAB.getInstance().getPlayers()){
					if (all.hasPermission("tab.staff") && ((Player) p.getPlayer()).canSee((Player) all.getPlayer())) count++;
				}
				return String.valueOf(count);
			}
		});
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%nonstaffonline%", 2000) {
			public String get(TabPlayer p) {
				int count = TAB.getInstance().getPlayers().size();
				for (TabPlayer all : TAB.getInstance().getPlayers()){
					if (all.hasPermission("tab.staff") && ((Player) p.getPlayer()).canSee((Player) all.getPlayer())) count--;
				}
				return String.valueOf(count);
			}
		});
		if (NMSStorage.getInstance().getMinorVersion() >= 6) {
			manager.registerPlayerPlaceholder(new PlayerPlaceholder("%health%", 100) {
				public String get(TabPlayer p) {
					return String.valueOf((int) Math.ceil(((Player) p.getPlayer()).getHealth()));
				}
			});
		}
		registerAFKPlaceholder(manager);
		registerVaultPlaceholders(manager);
		registerSyncPlaceholders(manager);
	}

	/**
	 * Registers AFK placeholder
	 */
	private void registerAFKPlaceholder(PlaceholderManager manager) {
		Plugin afkplus = Bukkit.getPluginManager().getPlugin("AFKPlus");
		boolean antiafkplus = Bukkit.getPluginManager().isPluginEnabled("AntiAFKPlus");
		try {
			purpurIsAfk = Player.class.getMethod("isAfk");
		} catch (NoSuchMethodException | SecurityException e1) {
			//not purpur
		}
		String noAfk = "no";
		String yesAfk = "yes";
		manager.registerPlayerPlaceholder(new PlayerPlaceholder("%afk%", 500) {
			public String get(TabPlayer p) {
				try {
					if (essentials != null) {
						Object user = essGetUser.invoke(Bukkit.getPluginManager().getPlugin("Essentials"), p.getPlayer());
						if ((boolean) essIsAfk.invoke(user)) return yesAfk;
					}
					if (afkplus != null) {
						Object afkplusplayer = afkplus.getClass().getMethod("getPlayer", UUID.class).invoke(afkplus, p.getUniqueId());
						if ((boolean) afkplusplayer.getClass().getMethod("isAFK").invoke(afkplusplayer)) return yesAfk;
					}
					if (antiafkplus) {
						Object api = Class.forName("de.kinglol12345.AntiAFKPlus.api.AntiAFKPlusAPI").getDeclaredMethod("getAPI").invoke(null);
						if ((boolean) api.getClass().getMethod("isAFK", Player.class).invoke(api, p.getPlayer())) return yesAfk;
					}
				} catch (Exception e) {
					return TAB.getInstance().getErrorManager().printError("", "Failed to check AFK status of " + p.getName(), e);
				}
				try {
					if (purpurIsAfk != null && (boolean) purpurIsAfk.invoke(p.getPlayer())) return yesAfk;
				} catch (Exception e) {
					//not purpur
				}
				return noAfk;
			}
		});
	}

	/**
	 * Registers vault placeholders
	 */
	private void registerVaultPlaceholders(PlaceholderManager manager) {
		if (Bukkit.getPluginManager().isPluginEnabled("Vault") && chat != null) {
			manager.registerPlayerPlaceholder(new PlayerPlaceholder("%vault-prefix%", 500) {

				public String get(TabPlayer p) {
					return chat.getPlayerPrefix((Player) p.getPlayer());
				}
			});
			manager.registerPlayerPlaceholder(new PlayerPlaceholder("%vault-suffix%", 500) {

				public String get(TabPlayer p) {
					return chat.getPlayerSuffix((Player) p.getPlayer());
				}
			});
		} else {
			manager.registerServerPlaceholder(new ServerPlaceholder("%vault-prefix%", 1000000) {
				public String get() {
					return "";
				}
			});
			manager.registerServerPlaceholder(new ServerPlaceholder("%vault-suffix%", 1000000) {
				public String get() {
					return "";
				}
			});
		}
	}

	/**
	 * Registers synchronous placeholders
	 */
	private void registerSyncPlaceholders(PlaceholderManager manager) {
		PlaceholderManagerImpl pl = (PlaceholderManagerImpl) TAB.getInstance().getPlaceholderManager();
		for (String identifier : pl.getPlaceholderUsage().keySet()) {
			if (identifier.startsWith("%sync:")) {
				int refresh;
				if (pl.getServerPlaceholderRefreshIntervals().containsKey(identifier)) {
					refresh = pl.getServerPlaceholderRefreshIntervals().get(identifier);
				} else if (pl.getPlayerPlaceholderRefreshIntervals().containsKey(identifier)) {
					refresh = pl.getPlayerPlaceholderRefreshIntervals().get(identifier);
				} else {
					refresh = pl.getDefaultRefresh();
				}
				manager.registerPlayerPlaceholder(new PlayerPlaceholder(identifier, refresh) {

					@Override
					public String get(TabPlayer p) {
						Bukkit.getScheduler().runTask(plugin, () -> {

							long time = System.nanoTime();
							String syncedPlaceholder = identifier.substring(6, identifier.length()-1);
							String value = ((BukkitPlatform) TAB.getInstance().getPlatform()).setPlaceholders((Player) p.getPlayer(), "%" + syncedPlaceholder + "%");
							getLastValues().put(p.getName(), value);
							if (!getForceUpdate().contains(p.getName())) getForceUpdate().add(p.getName());
							TAB.getInstance().getCPUManager().addPlaceholderTime(getIdentifier(), System.nanoTime()-time);
						});
						return getLastValues().get(p.getName());
					}
				});
			}
		}
	}
}