package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

/**
 * Bukkit registry to register bukkit-only placeholders
 */
public class BukkitPlaceholderRegistry implements PlaceholderRegistry {

	//formatter for 2 decimal places
	public final DecimalFormat decimal2 = ((DecimalFormat)NumberFormat.getNumberInstance(Locale.US));

	//plugin instance
	private JavaPlugin plugin;

	//vault economy
	private Economy economy;

	//vault chat
	private Chat chat;

	//placeholder registry buffer
	private List<Placeholder> placeholders;

	//essentials methods
	private Method getUser;
	private Method getNickname;

	/**
	 * Constructs new instance with given parameter
	 * @param plugin - plugin instance
	 */
	public BukkitPlaceholderRegistry(JavaPlugin plugin) {
		decimal2.applyPattern("#.##");
		this.plugin = plugin;
		if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			RegisteredServiceProvider<Economy> rspEconomy = Bukkit.getServicesManager().getRegistration(Economy.class);
			if (rspEconomy != null) economy = rspEconomy.getProvider();
			RegisteredServiceProvider<Chat> rspChat = Bukkit.getServicesManager().getRegistration(Chat.class);
			if (rspChat != null) chat = rspChat.getProvider();
		}
		if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
			try {
				getUser = Class.forName("com.earth2me.essentials.Essentials").getMethod("getUser", Player.class);
				getNickname = Class.forName("com.earth2me.essentials.User").getMethod("getNickname");
			} catch (Exception e) {
				TAB.getInstance().getErrorManager().printError("Failed to load essentials methods", e);
			}
		}
	}

	@Override
	public List<Placeholder> registerPlaceholders() {
		placeholders = new ArrayList<Placeholder>();
		placeholders.add(new PlayerPlaceholder("%money%", 1000) {
			public String get(TabPlayer p) {
				if (economy != null) return decimal2.format(economy.getBalance((Player) p.getPlayer()));
				return "-";
			}
		});

		placeholders.add(new PlayerPlaceholder("%displayname%", 500) {
			public String get(TabPlayer p) {
				return ((Player) p.getPlayer()).getDisplayName();
			}
		});
		placeholders.add(new PlayerPlaceholder("%deaths%", 1000) {
			public String get(TabPlayer p) {
				return String.valueOf(((Player) p.getPlayer()).getStatistic(Statistic.DEATHS));
			}
		});
		if (NMSStorage.getInstance().minorVersion >= 6) {
			placeholders.add(new PlayerPlaceholder("%health%", 100) {
				public String get(TabPlayer p) {
					return String.valueOf((int) Math.ceil(((Player) p.getPlayer()).getHealth()));
				}
			});
		}
		placeholders.add(new ServerPlaceholder("%tps%", 1000) {
			public String get() {
				try {
					Object nmsServer = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
					double value = ((double[]) nmsServer.getClass().getField("recentTps").get(nmsServer))[0];
					return decimal2.format(Math.min(20, value));
				} catch (Throwable t) {
					return "-1";
				}
			}
		});
		placeholders.add(new PlayerPlaceholder("%canseeonline%", 2000) {
			public String get(TabPlayer p) {
				int var = 0;
				for (TabPlayer all : TAB.getInstance().getPlayers()){
					if (((Player) p.getPlayer()).canSee((Player) all.getPlayer())) var++;
				}
				return String.valueOf(var);
			}
		});
		placeholders.add(new PlayerPlaceholder("%canseestaffonline%", 2000) {
			public String get(TabPlayer p) {
				int var = 0;
				for (TabPlayer all : TAB.getInstance().getPlayers()){
					if (all.isStaff() && ((Player) p.getPlayer()).canSee((Player) all.getPlayer())) var++;
				}
				return String.valueOf(var);
			}
		});
		registerAFKPlaceholder();
		registerVaultPlaceholders();
		registerPositionPlaceholders();
		registerEssentialsPlaceholders();
		registerSyncPlaceholders();
		return placeholders;
	}

	/**
	 * Registers AFK placeholder
	 */
	private void registerAFKPlaceholder() {
		String noAfk = TAB.getInstance().getConfiguration().config.getString("placeholders.afk-no", "");
		String yesAfk = TAB.getInstance().getConfiguration().config.getString("placeholders.afk-yes", " &4*&4&lAFK&4*&r");
		placeholders.add(new PlayerPlaceholder("%afk%", 500) {
			public String get(TabPlayer p) {
				try {
					if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
						Object user = Bukkit.getPluginManager().getPlugin("Essentials").getClass().getMethod("getUser", Player.class).invoke(Bukkit.getPluginManager().getPlugin("Essentials"), p.getPlayer());
						if ((boolean) user.getClass().getMethod("isAfk").invoke(user)) return yesAfk;
					}
					if (Bukkit.getPluginManager().isPluginEnabled("AFKPlus")) {
						Object AFKPlus = Bukkit.getPluginManager().getPlugin("AFKPlus");
						Object AFKPlusPlayer = AFKPlus.getClass().getMethod("getPlayer", UUID.class).invoke(AFKPlus, p.getUniqueId());
						if ((boolean) AFKPlusPlayer.getClass().getMethod("isAFK").invoke(AFKPlusPlayer)) return yesAfk;
					}
					if (Bukkit.getPluginManager().isPluginEnabled("AntiAFKPlus")) {
						Object api = Class.forName("de.kinglol12345.AntiAFKPlus.api.AntiAFKPlusAPI").getDeclaredMethod("getAPI").invoke(null);
						if ((boolean) api.getClass().getMethod("isAFK", Player.class).invoke(api, p.getPlayer())) return yesAfk;
					}
					if (Bukkit.getPluginManager().isPluginEnabled("xAntiAFK")) {
						if ((boolean) Class.forName("ch.soolz.xantiafk.xAntiAFKAPI").getMethod("isAfk", Player.class).invoke(null, p.getPlayer())) return yesAfk;
					}
					if (Bukkit.getPluginManager().isPluginEnabled("AutoAFK")) {
						Object plugin = Bukkit.getPluginManager().getPlugin("AutoAFK");
						Field f = plugin.getClass().getDeclaredField("afkList");
						f.setAccessible(true);
						HashMap<?, ?> map = (HashMap<?, ?>) f.get(plugin);
						if (map.containsKey(p.getPlayer())) return yesAfk;
					}
				} catch (Throwable t) {
					return TAB.getInstance().getErrorManager().printError("", "Failed to check AFK status of " + p.getName(), t);
				}
				try {
					//purpur AFK API
					if ((boolean) p.getPlayer().getClass().getMethod("isAfk").invoke(p.getPlayer())) return yesAfk;
				} catch (Throwable t) {
					//not purpur
				}
				return noAfk;
			}
			@Override
			public String[] getNestedStrings(){
				return new String[] {yesAfk, noAfk};
			}
		});
	}

	/**
	 * Registers vault placeholders
	 */
	private void registerVaultPlaceholders() {
		if (Bukkit.getPluginManager().isPluginEnabled("Vault") && chat != null) {
			placeholders.add(new PlayerPlaceholder("%vault-prefix%", 500) {

				public String get(TabPlayer p) {
					try {
						String prefix = chat.getPlayerPrefix((Player) p.getPlayer());
						return prefix != null ? prefix : "";
					} catch (Exception e) {
						return TAB.getInstance().getErrorManager().printError("", "Placeholder %vault-prefix% threw an exception", e);
					}
				}
			});
			placeholders.add(new PlayerPlaceholder("%vault-suffix%", 500) {

				public String get(TabPlayer p) {
					try {
						String suffix = chat.getPlayerSuffix((Player) p.getPlayer());
						return suffix != null ? suffix : "";
					} catch (Exception e) {
						return TAB.getInstance().getErrorManager().printError("", "Placeholder %vault-suffix% threw an exception", e);
					}
				}
			});
		} else {
			placeholders.add(new ServerPlaceholder("%vault-prefix%", 1000000) {
				public String get() {
					return "";
				}
			});
			placeholders.add(new ServerPlaceholder("%vault-suffix%", 1000000) {
				public String get() {
					return "";
				}
			});
		}
	}

	/**
	 * Registers position placeholders
	 */
	private void registerPositionPlaceholders() {
		placeholders.add(new PlayerPlaceholder("%xPos%", 100) {
			public String get(TabPlayer p) {
				return String.valueOf(((Player) p.getPlayer()).getLocation().getBlockX());
			}
		});
		placeholders.add(new PlayerPlaceholder("%yPos%", 100) {
			public String get(TabPlayer p) {
				return String.valueOf(((Player) p.getPlayer()).getLocation().getBlockY());
			}
		});
		placeholders.add(new PlayerPlaceholder("%zPos%", 100) {
			public String get(TabPlayer p) {
				return String.valueOf(((Player) p.getPlayer()).getLocation().getBlockZ());
			}
		});
	}

	/**
	 * Registers essentials placeholders
	 */
	private void registerEssentialsPlaceholders() {
		if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
			placeholders.add(new PlayerPlaceholder("%essentialsnick%", 1000) {
				public String get(TabPlayer p) {
					if (getNickname == null) return "<Internal plugin error>";
					String name = null;
					try {
						name = (String) getNickname.invoke(getUser.invoke(Bukkit.getPluginManager().getPlugin("Essentials"), p.getPlayer()));
					} catch (Exception e) {
						TAB.getInstance().getErrorManager().printError("Failed to get Essentials nickname of " + p.getName(), e);
					}
					if (name == null || name.length() == 0) return p.getName();
					return TAB.getInstance().getConfiguration().essentialsNickPrefix + name;
				}
			});
		} else {
			placeholders.add(new PlayerPlaceholder("%essentialsnick%", 100000000) {
				public String get(TabPlayer p) {
					return p.getName();
				}
			});
		}
	}

	/**
	 * Registers synchronous placeholders
	 */
	private void registerSyncPlaceholders() {
		PlaceholderManager pl = TAB.getInstance().getPlaceholderManager();
		for (String identifier : pl.allUsedPlaceholderIdentifiers) {
			if (identifier.startsWith("%sync:")) {
				int refresh;
				if (pl.serverPlaceholderRefreshIntervals.containsKey(identifier)) {
					refresh = pl.serverPlaceholderRefreshIntervals.get(identifier);
				} else if (pl.playerPlaceholderRefreshIntervals.containsKey(identifier)) {
					refresh = pl.playerPlaceholderRefreshIntervals.get(identifier);
				} else {
					refresh = pl.defaultRefresh;
				}
				placeholders.add(new PlayerPlaceholder(identifier, refresh) {

					@Override
					public String get(TabPlayer p) {
						Bukkit.getScheduler().runTask(plugin, new Runnable() {

							@Override
							public void run() {
								long time = System.nanoTime();
								String syncedPlaceholder = identifier.substring(6, identifier.length()-1);
								String value = ((BukkitPlatform) TAB.getInstance().getPlatform()).setPlaceholders((Player) p.getPlayer(), "%" + syncedPlaceholder + "%");
								lastValue.put(p.getName(), value);
								if (!forceUpdate.contains(p.getName())) forceUpdate.add(p.getName());
								TAB.getInstance().getCPUManager().addPlaceholderTime(getIdentifier(), System.nanoTime()-time);
							}
						});
						return lastValue.get(p.getName());
					}
				});
			}
		}
	}
}