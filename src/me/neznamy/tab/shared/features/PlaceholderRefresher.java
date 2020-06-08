package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerConstant;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;

public class PlaceholderRefresher {

	//for metrics
	public static List<String> unknownPlaceholders = new ArrayList<String>();
	
	public static Map<String, Integer> serverPlaceholders = new HashMap<String, Integer>();
	public static List<String> serverConstants = new ArrayList<String>();
	public static Map<String, Integer> playerPlaceholders = new HashMap<String, Integer>();
	
	static {
		serverPlaceholders.put("%bungee_total%", 1000);
		serverPlaceholders.put("%server_online%", 1000); 		//%online%
		serverPlaceholders.put("%server_uptime%", 1000);
		serverPlaceholders.put("%server_tps_1_colored%", 1000);
		serverPlaceholders.put("%supervanish_playercount%", 1000); //%canseeonline%
		
		serverConstants.add("%server_max_players%");			//%maxplayers%
		
		playerPlaceholders.put("%luckperms_primary_group_name%", 1000); //%rank%
		playerPlaceholders.put("%cmi_user_afk_symbol%", 500); 	//%afk%
		playerPlaceholders.put("%cmi_user_afk%", 500); 			//%afk%
		playerPlaceholders.put("%luckperms_prefix%", 1000); 	//%luckperms-prefix%
		playerPlaceholders.put("%luckperms_suffix%", 1000); 	//%luckperms-suffix%
		playerPlaceholders.put("%player_name%", 10000); //nick plugins changing player name, so not a constant
		playerPlaceholders.put("%vault_prefix%", 1000);			//%vault-prefix%
		playerPlaceholders.put("%vault_suffix%", 1000);			//%vault-prefix%
		playerPlaceholders.put("%vault_rank%", 1000);			//%rank%
		playerPlaceholders.put("%player_displayname%", 1000);	//%displayname%
		playerPlaceholders.put("%deluxetags_tag%", 1000);		//%deluxetag%
		playerPlaceholders.put("%player_ping%", 1000);			//%ping%
		playerPlaceholders.put("%eglow_glowcolor%", 100);
		playerPlaceholders.put("%factionsuuid_faction_name%", 1000);
		playerPlaceholders.put("%vault_eco_balance_formatted%", 1000);
		playerPlaceholders.put("%vault_eco_balance_commas%", 1000);
		playerPlaceholders.put("%vault_eco_balance_fixed%", 1000);
		playerPlaceholders.put("%cmi_user_vanished_symbol%", 1000);
		playerPlaceholders.put("%player_colored_ping%", 1000);
		playerPlaceholders.put("%cmi_user_display_name%", 1000);
		playerPlaceholders.put("%factionsuuid_faction_name%", 1000);
		playerPlaceholders.put("%multiverse_world_alias%", 2000);
	}
	
	public static void registerPlaceholder(String identifier) {
		if (serverPlaceholders.containsKey(identifier)) {
			int cooldown = Configs.getSecretOption("papi-placeholder-cooldowns.server." + identifier, serverPlaceholders.get(identifier));
			Shared.debug("Registering SERVER PlaceholderAPI placeholder " + identifier + " with cooldown " + cooldown);
			Placeholders.registerPlaceholder(new ServerPlaceholder(identifier, cooldown){
				public String get() {
					return PluginHooks.PlaceholderAPI_setPlaceholders((UUID)null, identifier);
				}
			});
			return;
		}
		if (serverConstants.contains(identifier)) {
			Shared.debug("Registering SERVER PlaceholderAPI constant " + identifier);
			Placeholders.registerPlaceholder(new ServerConstant(identifier){
				public String get() {
					return PluginHooks.PlaceholderAPI_setPlaceholders((UUID)null, identifier);
				}
			});
			return;
		}
		if (playerPlaceholders.containsKey(identifier)) {
			int cooldown = Configs.getSecretOption("papi-placeholder-cooldowns.player." + identifier, playerPlaceholders.get(identifier));
			Shared.debug("Registering PLAYER PlaceholderAPI placeholder " + identifier + " with cooldown " + cooldown);
			Placeholders.registerPlaceholder(new PlayerPlaceholder(identifier, cooldown){
				public String get(ITabPlayer p) {
					return PluginHooks.PlaceholderAPI_setPlaceholders(p.getBukkitEntity(), identifier);
				}
			});
			return;
		}
		unknownPlaceholders.add(identifier);
		int cooldown = Configs.getSecretOption("papi-placeholder-cooldowns.server." + identifier, -1);
		if (cooldown != -1) {
			Shared.debug("Registering SERVER PlaceholderAPI placeholder " + identifier + " with cooldown " + cooldown);
			Placeholders.registerPlaceholder(new ServerPlaceholder(identifier, cooldown){
				public String get() {
					return PluginHooks.PlaceholderAPI_setPlaceholders((UUID)null, identifier);
				}
			});
			return;
		}
		cooldown = Configs.getSecretOption("papi-placeholder-cooldowns.player." + identifier, 50);
		Shared.debug("Registering PLAYER PlaceholderAPI placeholder " + identifier + " with cooldown " + cooldown);
		Placeholders.registerPlaceholder(new PlayerPlaceholder(identifier, cooldown){
			public String get(ITabPlayer p) {
				return PluginHooks.PlaceholderAPI_setPlaceholders(p == null ? null : p.getBukkitEntity(), identifier);
			}
		});
	}
}