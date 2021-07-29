package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
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

	private Object essentials;
	
	//essentials methods
	private Method essGetUser;
	private Method essGetNickname;
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
			RegisteredServiceProvider<Economy> rspEconomy = Bukkit.getServicesManager().getRegistration(Economy.class);
			if (rspEconomy != null) economy = rspEconomy.getProvider();
			RegisteredServiceProvider<Chat> rspChat = Bukkit.getServicesManager().getRegistration(Chat.class);
			if (rspChat != null) chat = rspChat.getProvider();
		}
		if (essentials != null) {
			try {
				essGetUser = Class.forName("com.earth2me.essentials.Essentials").getMethod("getUser", Player.class);
				essGetNickname = Class.forName("com.earth2me.essentials.User").getMethod("getNickname");
				essIsAfk = Class.forName("com.earth2me.essentials.User").getMethod("isAfk");
			} catch (Exception e) {
				TAB.getInstance().getErrorManager().printError("Failed to load essentials methods", e);
			}
		}
	}

	@Override
	public List<Placeholder> registerPlaceholders() {
		NumberFormat roundDown = NumberFormat.getInstance();
		roundDown.setRoundingMode(RoundingMode.DOWN);
		roundDown.setMaximumFractionDigits(2);
		placeholders = new ArrayList<>();
		placeholders.add(new PlayerPlaceholder("%money%", 1000) {
			public String get(TabPlayer p) {
				if (economy != null) return decimal2.format(economy.getBalance((Player) p.getPlayer()));
				return "-";
			}
		});

		placeholders.add(new PlayerPlaceholder("%displayname%", 500) {
			@SuppressWarnings("deprecation")
			public String get(TabPlayer p) {
				return ((Player) p.getPlayer()).getDisplayName();
			}
		});
		placeholders.add(new PlayerPlaceholder("%deaths%", 1000) {
			public String get(TabPlayer p) {
				return String.valueOf(((Player) p.getPlayer()).getStatistic(Statistic.DEATHS));
			}
		});
		placeholders.add(new ServerPlaceholder("%tps%", 1000) {
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

		try{
			Class.forName("com.destroystokyo.paper.PaperConfig");
			placeholders.add(new ServerPlaceholder("%mspt%", 1000) {
				public String get() {
						return String.valueOf(roundDown.format(Bukkit.getAverageTickTime()));
				}
			});
		}catch(Exception e){
			Bukkit.getConsoleSender().sendMessage("\u00a77[TAB] \u00A76Warning: %mspt% won't work because you are not running PaperSpigot! Using: "+Bukkit.getServer().getVersion());
		}
		placeholders.add(new PlayerPlaceholder("%canseeonline%", 2000) {
			public String get(TabPlayer p) {
				int count = 0;
				for (TabPlayer all : TAB.getInstance().getPlayers()){
					if (((Player) p.getPlayer()).canSee((Player) all.getPlayer())) count++;
				}
				return String.valueOf(count);
			}
		});
		placeholders.add(new PlayerPlaceholder("%canseestaffonline%", 2000) {
			public String get(TabPlayer p) {
				int count = 0;
				for (TabPlayer all : TAB.getInstance().getPlayers()){
					if (all.isStaff() && ((Player) p.getPlayer()).canSee((Player) all.getPlayer())) count++;
				}
				return String.valueOf(count);
			}
		});
		if (NMSStorage.getInstance().getMinorVersion() >= 6) {
			placeholders.add(new PlayerPlaceholder("%health%", 100) {
				public String get(TabPlayer p) {
					return String.valueOf((int) Math.ceil(((Player) p.getPlayer()).getHealth()));
				}
			});
		}
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
		Plugin afkplus = Bukkit.getPluginManager().getPlugin("AFKPlus");
		boolean antiafkplus = Bukkit.getPluginManager().isPluginEnabled("AntiAFKPlus");
		try {
			purpurIsAfk = Player.class.getMethod("isAfk");
		} catch (NoSuchMethodException | SecurityException e1) {
			//not purpur
		}
		String noAfk = TAB.getInstance().getConfiguration().getConfig().getString("placeholders.afk-no", "");
		String yesAfk = TAB.getInstance().getConfiguration().getConfig().getString("placeholders.afk-yes", " &4*&4&lAFK&4*&r");
		placeholders.add(new PlayerPlaceholder("%afk%", 500) {
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
					return chat.getPlayerPrefix((Player) p.getPlayer());
				}
			});
			placeholders.add(new PlayerPlaceholder("%vault-suffix%", 500) {

				public String get(TabPlayer p) {
					return chat.getPlayerSuffix((Player) p.getPlayer());
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
		if (essentials != null) {
			placeholders.add(new PlayerPlaceholder("%essentialsnick%", 1000) {
				public String get(TabPlayer p) {
					if (essGetNickname == null) return "<Internal plugin error>";
					String name = null;
					try {
						name = (String) essGetNickname.invoke(essGetUser.invoke(essentials, p.getPlayer()));
					} catch (Exception e) {
						TAB.getInstance().getErrorManager().printError("Failed to get Essentials nickname of " + p.getName(), e);
					}
					if (name == null || name.length() == 0) return p.getName();
					return TAB.getInstance().getConfiguration().getEssentialsNickPrefix() + name;
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
		for (String identifier : pl.getAllUsedPlaceholderIdentifiers()) {
			if (identifier.startsWith("%sync:")) {
				int refresh;
				if (pl.getServerPlaceholderRefreshIntervals().containsKey(identifier)) {
					refresh = pl.getServerPlaceholderRefreshIntervals().get(identifier);
				} else if (pl.getPlayerPlaceholderRefreshIntervals().containsKey(identifier)) {
					refresh = pl.getPlayerPlaceholderRefreshIntervals().get(identifier);
				} else {
					refresh = pl.getDefaultRefresh();
				}
				placeholders.add(new PlayerPlaceholder(identifier, refresh) {

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