package me.neznamy.tab.platforms.bukkit.placeholders;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.neznamy.tab.api.AFKProvider;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitMethods;
import me.neznamy.tab.platforms.bukkit.placeholders.afk.AFKPlus;
import me.neznamy.tab.platforms.bukkit.placeholders.afk.AntiAFKPlus;
import me.neznamy.tab.platforms.bukkit.placeholders.afk.AutoAFK;
import me.neznamy.tab.platforms.bukkit.placeholders.afk.Essentials;
import me.neznamy.tab.platforms.bukkit.placeholders.afk.None;
import me.neznamy.tab.platforms.bukkit.placeholders.afk.xAntiAFK;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
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

	public static final DecimalFormat decimal2 = new DecimalFormat("#.##");
	
	private JavaPlugin plugin;
	private Economy economy;
	private Chat chat;
	private static List<Placeholder> placeholders;

	public BukkitPlaceholderRegistry(JavaPlugin plugin) {
		this.plugin = plugin;
		if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			RegisteredServiceProvider<Economy> rspEconomy = Bukkit.getServicesManager().getRegistration(Economy.class);
			if (rspEconomy != null) economy = rspEconomy.getProvider();
			RegisteredServiceProvider<Chat> rspChat = Bukkit.getServicesManager().getRegistration(Chat.class);
			if (rspChat != null) chat = rspChat.getProvider();
		}
	}

	@Override
	public List<Placeholder> registerPlaceholders() {
		placeholders = new ArrayList<Placeholder>();
		placeholders.add(new PlayerPlaceholder("%money%", 1000) {
			public String get(TabPlayer p) {
				if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) return decimal2.format(((com.earth2me.essentials.Essentials)Bukkit.getPluginManager().getPlugin("Essentials")).getUser((Player) p.getPlayer()).getMoney().doubleValue());
				if (economy != null) return decimal2.format(economy.getBalance((Player) p.getPlayer()));
				return "-";
			}
		});

		placeholders.add(new PlayerPlaceholder("%displayname%", 500) {
			public String get(TabPlayer p) {
				return ((Player) p.getPlayer()).getDisplayName();
			}
		});
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 7) placeholders.add(new PlayerPlaceholder("%deaths%", 1000) {
			public String get(TabPlayer p) {
				return ((Player) p.getPlayer()).getStatistic(Statistic.DEATHS)+"";
			}
		});
		placeholders.add(new PlayerPlaceholder("%health%", 100) {
			public String get(TabPlayer p) {
				return (int) Math.ceil(((Player) p.getPlayer()).getHealth())+"";
			}
		});
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
				for (TabPlayer all : Shared.getPlayers()){
					if (((Player) p.getPlayer()).canSee((Player) all.getPlayer())) var++;
				}
				return var+"";
			}
		});
		placeholders.add(new PlayerPlaceholder("%canseestaffonline%", 2000) {
			public String get(TabPlayer p) {
				int var = 0;
				for (TabPlayer all : Shared.getPlayers()){
					if (all.isStaff() && ((Player) p.getPlayer()).canSee((Player) all.getPlayer())) var++;
				}
				return var+"";
			}
		});
		placeholders.add(new ServerPlaceholder("%maxplayers%", -1) {
			public String get() {
				return Bukkit.getMaxPlayers()+"";
			}
		});
		registerAFKPlaceholder();
		registerVaultPlaceholders();
		registerPositionPlaceholders();
		registerEssentialsPlaceholders();
		registerSyncPlaceholders();
		return placeholders;
	}

	private void registerAFKPlaceholder() {
		AFKProvider afk;
		if (Bukkit.getPluginManager().isPluginEnabled("xAntiAFK")) {
			afk = new xAntiAFK();
		} else if (Bukkit.getPluginManager().isPluginEnabled("AFKPlus")) {
			afk = new AFKPlus();
		} else if (Bukkit.getPluginManager().isPluginEnabled("AutoAFK")) {
			afk = new AutoAFK();
		} else if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
			afk = new Essentials();
		} else if (Bukkit.getPluginManager().isPluginEnabled("AntiAFKPlus")) {
			afk = new AntiAFKPlus();
		} else {
			afk = new None();
		}
		((PlaceholderManager)Shared.featureManager.getFeature("placeholders")).setAFKProvider(afk);
		placeholders.add(new PlayerPlaceholder("%afk%", 500) {
			public String get(TabPlayer p) {
				try {
					return ((PlaceholderManager)Shared.featureManager.getFeature("placeholders")).getAFKProvider().isAFK(p) ? Configs.yesAfk : Configs.noAfk;
				} catch (Throwable t) {
					return Shared.errorManager.printError("", "Failed to check AFK status of " + p.getName() + " using " + ((PlaceholderManager)Shared.featureManager.getFeature("placeholders")).getAFKProvider().getClass().getSimpleName(), t);
				}
			}
			@Override
			public String[] getNestedStrings(){
				return new String[] {Configs.yesAfk, Configs.noAfk};
			}
		});
	}

	private void registerVaultPlaceholders() {
		if (Bukkit.getPluginManager().isPluginEnabled("Vault") && chat != null) {
			placeholders.add(new PlayerPlaceholder("%vault-prefix%", 500) {

				public String get(TabPlayer p) {
					String prefix = chat.getPlayerPrefix((Player) p.getPlayer());
					return prefix != null ? prefix : "";
				}
			});
			placeholders.add(new PlayerPlaceholder("%vault-suffix%", 500) {

				public String get(TabPlayer p) {
					String suffix = chat.getPlayerSuffix((Player) p.getPlayer());
					return suffix != null ? suffix : "";
				}
			});
		} else {
			placeholders.add(new ServerPlaceholder("%vault-prefix%", -1) {
				public String get() {
					return "";
				}
			});
			placeholders.add(new ServerPlaceholder("%vault-suffix%", -1) {
				public String get() {
					return "";
				}
			});
		}
	}

	private void registerPositionPlaceholders() {
		placeholders.add(new PlayerPlaceholder("%xPos%", 100) {
			public String get(TabPlayer p) {
				return ((Player) p.getPlayer()).getLocation().getBlockX()+"";
			}
		});
		placeholders.add(new PlayerPlaceholder("%yPos%", 100) {
			public String get(TabPlayer p) {
				return ((Player) p.getPlayer()).getLocation().getBlockY()+"";
			}
		});
		placeholders.add(new PlayerPlaceholder("%zPos%", 100) {
			public String get(TabPlayer p) {
				return ((Player) p.getPlayer()).getLocation().getBlockZ()+"";
			}
		});
	}

	private void registerEssentialsPlaceholders() {
		if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
			placeholders.add(new PlayerPlaceholder("%essentialsnick%", 1000) {
				public String get(TabPlayer p) {
					String name = null;
					if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
						name = ((com.earth2me.essentials.Essentials)Bukkit.getPluginManager().getPlugin("Essentials")).getUser((Player) p.getPlayer()).getNickname();
					}
					if (name == null || name.length() == 0) return p.getName();
					return Configs.getSecretOption("essentials-nickname-prefix", "") + name;
				}
			});
		} else {
			placeholders.add(new PlayerPlaceholder("%essentialsnick%", 999999) {
				public String get(TabPlayer p) {
					return p.getName();
				}
			});
		}
	}

	private void registerSyncPlaceholders() {
		for (String identifier : PlaceholderManager.allUsedPlaceholderIdentifiers) {
			if (identifier.startsWith("%sync:")) {
				PlaceholderManager pl = ((PlaceholderManager)Shared.featureManager.getFeature("placeholders"));
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
								String value = ((BukkitMethods) Shared.platform).setPlaceholders((Player) p.getPlayer(), "%" + syncedPlaceholder + "%");
								lastValue.put(p.getName(), value);
								if (!forceUpdate.contains(p.getName())) forceUpdate.add(p.getName());
								Shared.cpu.addPlaceholderTime(getIdentifier(), System.nanoTime()-time);
							}
						});
						return getLastValue(p);
					}
				});
			}
		}
	}
}