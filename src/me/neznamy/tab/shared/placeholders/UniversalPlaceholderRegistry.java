package me.neznamy.tab.shared.placeholders;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map.Entry;

import me.lucko.luckperms.LuckPerms;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

public class UniversalPlaceholderRegistry implements PlaceholderRegistry {

	private final DecimalFormat decimal2 = new DecimalFormat("#.##");
	
	@Override
	public void registerPlaceholders() {
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%rank%", 1000) {
			public String get(ITabPlayer p) {
				Object rank = null;
				for (Entry<Object, Object> entry : Configs.rankAliases.entrySet()) {
					if (String.valueOf(entry.getKey()).equalsIgnoreCase(p.getGroup())) {
						rank = entry.getValue();
						break;
					}
				}
				if (rank == null) rank = Configs.rankAliases.get("_OTHER_");
				if (rank == null) rank = p.getGroup();
				return String.valueOf(rank);
			}
			@Override
			public String[] getChilds(){
				return Configs.rankAliases.values().toArray(new String[0]);
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%staffonline%", 2000) {
			public String get() {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if (all.isStaff()) var++;
				}
				return var+"";
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%nonstaffonline%", 2000) {
			public String get() {
				int var = Shared.getPlayers().size();
				for (ITabPlayer all : Shared.getPlayers()){
					if (all.isStaff()) var--;
				}
				return var+"";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%"+Shared.platform.getSeparatorType()+"%", 1000) {
			public String get(ITabPlayer p) {
				if (Configs.serverAliases != null && Configs.serverAliases.containsKey(p.getWorldName())) return Configs.serverAliases.get(p.getWorldName())+""; //bungee only
				return p.getWorldName();
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%"+Shared.platform.getSeparatorType()+"online%", 1000) {
			public String get(ITabPlayer p) {
				int var = 0;
				for (ITabPlayer all : Shared.getPlayers()){
					if (p.getWorldName().equals(all.getWorldName())) var++;
				}
				return var+"";
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%memory-used%", 200) {
			public String get() {
				return ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + "");
			}
		});
		Placeholders.registerPlaceholder(new ServerConstant("%memory-max%") {
			public String get() {
				return ((int) (Runtime.getRuntime().maxMemory() / 1048576))+"";
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%memory-used-gb%", 200) {
			public String get() {
				return (decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024) + "");
			}
		});
		Placeholders.registerPlaceholder(new ServerConstant("%memory-max-gb%") {
			public String get() {
				return (decimal2.format((float)Runtime.getRuntime().maxMemory() /1024/1024/1024))+"";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%nick%", 999999999) {
			public String get(ITabPlayer p) {
				return p.getName();
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%time%", 900) {
			public String get() {
				return Configs.timeFormat.format(new Date(System.currentTimeMillis() + (int)(Configs.timeOffset*3600000)));
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%date%", 60000) {
			public String get() {
				return Configs.dateFormat.format(new Date(System.currentTimeMillis() + (int)(Configs.timeOffset*3600000)));
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%online%", 1000) {
			public String get() {
				return Shared.getPlayers().size()+"";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%ping%", 2000) {
			public String get(ITabPlayer p) {
				return p.getPing()+"";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%player-version%", 999999999) {
			public String get(ITabPlayer p) {
				return p.getVersion().getFriendlyName();
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%luckperms-prefix%", 500) {
			public String get(ITabPlayer p) {
				String prefix;
				try {
					//LuckPerms API v5
					User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
					if (user == null) {
						Shared.errorManager.printError("LuckPerms returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getPrefix)");
						return "";
					}
					prefix = user.getCachedData().getMetaData(LuckPermsProvider.get().getContextManager().getQueryOptions(user).get()).getPrefix();
				} catch (NoClassDefFoundError e) {
					//LuckPerms API v4
					me.lucko.luckperms.api.User user = LuckPerms.getApi().getUser(p.getUniqueId());
					if (user == null) {
						Shared.errorManager.printError("LuckPerms returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getPrefix)");
						return "";
					}
					prefix = user.getCachedData().getMetaData(LuckPerms.getApi().getContextManager().getApplicableContexts(p instanceof me.neznamy.tab.platforms.bukkit.TabPlayer ? p.getBukkitEntity() : p.getBungeeEntity())).getPrefix();
				}
				return prefix == null ? "" : prefix;
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%luckperms-suffix%", 500) {
			public String get(ITabPlayer p) {
				String suffix;
				try {
					//LuckPerms API v5
					User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
					if (user == null) {
						Shared.errorManager.printError("LuckPerms returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getSuffix)");
						return "";
					}
					suffix = user.getCachedData().getMetaData(LuckPermsProvider.get().getContextManager().getQueryOptions(user).get()).getSuffix();
				} catch (NoClassDefFoundError e) {
					//LuckPerms API v4
					me.lucko.luckperms.api.User user = LuckPerms.getApi().getUser(p.getUniqueId());
					if (user == null) {
						Shared.errorManager.printError("LuckPerms returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getSuffix)");
						return "";
					}
					suffix = user.getCachedData().getMetaData(LuckPerms.getApi().getContextManager().getApplicableContexts(p instanceof me.neznamy.tab.platforms.bukkit.TabPlayer ? p.getBukkitEntity() : p.getBungeeEntity())).getSuffix();
				}
				return suffix == null ? "" : suffix;
			}
		});
	}
}