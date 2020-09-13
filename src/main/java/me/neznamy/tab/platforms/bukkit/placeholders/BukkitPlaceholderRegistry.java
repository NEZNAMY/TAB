package me.neznamy.tab.platforms.bukkit.placeholders;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.neznamy.tab.api.AFKProvider;
import me.neznamy.tab.platforms.bukkit.placeholders.afk.AFKPlus;
import me.neznamy.tab.platforms.bukkit.placeholders.afk.AntiAFKPlus;
import me.neznamy.tab.platforms.bukkit.placeholders.afk.AutoAFK;
import me.neznamy.tab.platforms.bukkit.placeholders.afk.Essentials;
import me.neznamy.tab.platforms.bukkit.placeholders.afk.None;
import me.neznamy.tab.platforms.bukkit.placeholders.afk.xAntiAFK;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerConstant;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

/**
 * Bukkit registry to register bukkit-only placeholders
 */
public class BukkitPlaceholderRegistry implements PlaceholderRegistry {

	private Economy economy;
	private Chat chat;

	public BukkitPlaceholderRegistry() {
		if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			RegisteredServiceProvider<Economy> rspEconomy = Bukkit.getServicesManager().getRegistration(Economy.class);
			if (rspEconomy != null) economy = rspEconomy.getProvider();
			RegisteredServiceProvider<Chat> rspChat = Bukkit.getServicesManager().getRegistration(Chat.class);
			if (rspChat != null) chat = rspChat.getProvider();
		}
	}

	@Override
	public void registerPlaceholders() {
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%money%", 1000) {
			public String get(ITabPlayer p) {
				if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) return Placeholders.decimal2.format(((com.earth2me.essentials.Essentials)Bukkit.getPluginManager().getPlugin("Essentials")).getUser((Player) p.getPlayer()).getMoney().doubleValue());
				if (economy != null) return Placeholders.decimal2.format(economy.getBalance((Player) p.getPlayer()));
				return "-";
			}
		});
		
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%displayname%", 500) {
			public String get(ITabPlayer p) {
				return ((Player) p.getPlayer()).getDisplayName();
			}
		});
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 7) Placeholders.registerPlaceholder(new PlayerPlaceholder("%deaths%", 1000) {
			public String get(ITabPlayer p) {
				return ((Player) p.getPlayer()).getStatistic(Statistic.DEATHS)+"";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%health%", 100) {
			public String get(ITabPlayer p) {
				return (int) Math.ceil(((Player) p.getPlayer()).getHealth())+"";
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%tps%", 1000) {
			public String get() {
				try {
					Object nmsServer = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
					double value = ((double[]) nmsServer.getClass().getField("recentTps").get(nmsServer))[0];
					return Placeholders.decimal2.format(Math.min(20, value));
				} catch (Throwable t) {
					return "-1";
				}
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%canseeonline%", 2000) {
			public String get(ITabPlayer p) {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if (((Player) p.getPlayer()).canSee((Player) all.getPlayer())) var++;
				}
				return var+"";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%canseestaffonline%", 2000) {
			public String get(ITabPlayer p) {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if (all.isStaff() && ((Player) p.getPlayer()).canSee((Player) all.getPlayer())) var++;
				}
				return var+"";
			}
		});
		Placeholders.registerPlaceholder(new ServerConstant("%maxplayers%") {
			public String get() {
				return Bukkit.getMaxPlayers()+"";
			}
		});
		registerAFKPlaceholder();
		registerVaultPlaceholders();
		registerPositionPlaceholders();
		registerEssentialsPlaceholders();
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
		PlaceholderManager.getInstance().setAFKProvider(afk);
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%afk%", 500) {
			public String get(ITabPlayer p) {
				try {
					return PlaceholderManager.getInstance().getAFKProvider().isAFK(p) ? Configs.yesAfk : Configs.noAfk;
				} catch (Throwable t) {
					return Shared.errorManager.printError("", "Failed to check AFK status of " + p.getName() + " using " + PlaceholderManager.getInstance().getAFKProvider().getClass().getSimpleName(), t);
				}
			}
			@Override
			public String[] getNestedPlaceholders(){
				return new String[] {Configs.yesAfk, Configs.noAfk};
			}
		});
	}

	private void registerVaultPlaceholders() {
		if (Bukkit.getPluginManager().isPluginEnabled("Vault") && chat != null) {
			Placeholders.registerPlaceholder(new PlayerPlaceholder("%vault-prefix%", 500) {

				public String get(ITabPlayer p) {
					String prefix = chat.getPlayerPrefix((Player) p.getPlayer());
					return prefix != null ? prefix : "";
				}
			});
			Placeholders.registerPlaceholder(new PlayerPlaceholder("%vault-suffix%", 500) {

				public String get(ITabPlayer p) {
					String suffix = chat.getPlayerSuffix((Player) p.getPlayer());
					return suffix != null ? suffix : "";
				}
			});
		} else {
			Placeholders.registerPlaceholder(new ServerPlaceholder("%vault-prefix%", -1) {
				public String get() {
					return "";
				}
			});
			Placeholders.registerPlaceholder(new ServerPlaceholder("%vault-suffix%", -1) {
				public String get() {
					return "";
				}
			});
		}
	}
	
	private void registerPositionPlaceholders() {
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%xPos%", 100) {
			public String get(ITabPlayer p) {
				return ((Player) p.getPlayer()).getLocation().getBlockX()+"";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%yPos%", 100) {
			public String get(ITabPlayer p) {
				return ((Player) p.getPlayer()).getLocation().getBlockY()+"";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%zPos%", 100) {
			public String get(ITabPlayer p) {
				return ((Player) p.getPlayer()).getLocation().getBlockZ()+"";
			}
		});
	}
	
	private void registerEssentialsPlaceholders() {
		if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
			Placeholders.registerPlaceholder(new PlayerPlaceholder("%essentialsnick%", 1000) {
				public String get(ITabPlayer p) {
					String name = null;
					if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
						name = ((com.earth2me.essentials.Essentials)Bukkit.getPluginManager().getPlugin("Essentials")).getUser((Player) p.getPlayer()).getNickname();
					}
					if (name == null || name.length() == 0) return p.getName();
					return Configs.SECRET_essentials_nickname_prefix + name;
				}
			});
		} else {
			Placeholders.registerPlaceholder(new PlayerPlaceholder("%essentialsnick%", 999999) {
				public String get(ITabPlayer p) {
					return p.getName();
				}
			});
		}
	}
}