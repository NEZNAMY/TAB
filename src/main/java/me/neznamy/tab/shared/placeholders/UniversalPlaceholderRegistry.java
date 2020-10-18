package me.neznamy.tab.shared.placeholders;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.permission.LuckPerms;

/**
 * An implementation of PlaceholderRegistry for universal placeholders
 */
public class UniversalPlaceholderRegistry implements PlaceholderRegistry {

	//decimal formatter for 2 decimal numbers
	private final DecimalFormat decimal2 = new DecimalFormat("#.##");
	
	@Override
	public void registerPlaceholders() {
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%"+Shared.platform.getSeparatorType()+"%", 1000) {
			public String get(TabPlayer p) {
				if (Configs.serverAliases != null && Configs.serverAliases.containsKey(p.getWorldName())) return Configs.serverAliases.get(p.getWorldName())+""; //bungee only
				return p.getWorldName();
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%"+Shared.platform.getSeparatorType()+"online%", 1000) {
			public String get(TabPlayer p) {
				int var = 0;
				for (TabPlayer all : Shared.getPlayers()){
					if (p.getWorldName().equals(all.getWorldName())) var++;
				}
				return var+"";
			}
		});
		
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%nick%", 999999999) {
			public String get(TabPlayer p) {
				return p.getName();
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%player%", 999999999) {
			public String get(TabPlayer p) {
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
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%ping%", 500) {
			public String get(TabPlayer p) {
				return p.getPing()+"";
			}
		});
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%player-version%", 999999999) {
			public String get(TabPlayer p) {
				return p.getVersion().getFriendlyName();
			}
		});
		registerLuckPermsPlaceholders();
		registerMemoryPlaceholders();
		registerStaffPlaceholders();
		registerRankPlaceholder();
	}
	
	private void registerMemoryPlaceholders() {
		Placeholders.registerPlaceholder(new ServerPlaceholder("%memory-used%", 200) {
			public String get() {
				return ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + "");
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%memory-max%", -1) {
			public String get() {
				return ((int) (Runtime.getRuntime().maxMemory() / 1048576))+"";
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%memory-used-gb%", 200) {
			public String get() {
				return (decimal2.format((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024) + "");
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%memory-max-gb%", -1) {
			public String get() {
				return (decimal2.format((float)Runtime.getRuntime().maxMemory() /1024/1024/1024))+"";
			}
		});
	}
	
	private void registerLuckPermsPlaceholders() {
		if (Shared.permissionPlugin instanceof LuckPerms) {
			Placeholders.registerPlaceholder(new PlayerPlaceholder("%luckperms-prefix%", 500) {
				public String get(TabPlayer p) {
					return ((LuckPerms)Shared.permissionPlugin).getPrefix(p);
				}
			});
			Placeholders.registerPlaceholder(new PlayerPlaceholder("%luckperms-suffix%", 500) {
				public String get(TabPlayer p) {
					return ((LuckPerms)Shared.permissionPlugin).getSuffix(p);
				}
			});
		}
	}
	
	private void registerStaffPlaceholders() {
		Placeholders.registerPlaceholder(new ServerPlaceholder("%staffonline%", 2000) {
			public String get() {
				int var = 0;
				for (TabPlayer all : Shared.getPlayers()){
					if (all.isStaff()) var++;
				}
				return var+"";
			}
		});
		Placeholders.registerPlaceholder(new ServerPlaceholder("%nonstaffonline%", 2000) {
			public String get() {
				int var = Shared.getPlayers().size();
				for (TabPlayer all : Shared.getPlayers()){
					if (all.isStaff()) var--;
				}
				return var+"";
			}
		});
	}
	
	private void registerRankPlaceholder() {
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%rank%", 1000) {
			public String get(TabPlayer p) {
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
			public String[] getNestedPlaceholders(){
				List<String> list = new ArrayList<String>();
				for (Object value : Configs.rankAliases.values()) {
					list.add(String.valueOf(value));
				}
				return list.toArray(new String[0]);
			}
		});
	}
}