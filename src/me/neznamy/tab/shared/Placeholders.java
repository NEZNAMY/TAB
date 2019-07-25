package me.neznamy.tab.shared;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import me.neznamy.tab.shared.Shared.ServerType;

public class Placeholders {
	
	public static ConcurrentHashMap<String, Integer> online = new ConcurrentHashMap<String, Integer>();
	public static boolean relationalPlaceholders;
	public static int maxPlayers;
	
	public static void recalculateOnlineVersions() {
		online.put("1-14-x", 0);
		online.put("1-13-x", 0);
		online.put("1-12-x", 0);
		online.put("1-11-x", 0);
		online.put("1-10-x", 0);
		online.put("1-9-x", 0);
		online.put("1-8-x", 0);
		online.put("1-7-x", 0);
		online.put("1-6-x", 0);
		online.put("1-5-x", 0);
		online.put("other", 0);
		for (ITabPlayer p : Shared.getPlayers()){
			int version = p.getVersion();
			if (version == 60 || version == 61) online.put("1-5-x", online.get("1-5-x")+1);
			else if (version >= 73 && version <= 78) online.put("1-6-x", online.get("1-6-x")+1);
			else if (version <= 0) online.put("other", online.get("other")+1);
			else if (version <= 5) online.put("1-7-x", online.get("1-7-x")+1);
			else if (version <= 47) online.put("1-8-x", online.get("1-8-x")+1);
			else if (version <= 110) online.put("1-9-x", online.get("1-9-x")+1);
			else if (version <= 210) online.put("1-10-x", online.get("1-10-x")+1);
			else if (version <= 316) online.put("1-11-x", online.get("1-11-x")+1);
			else if (version <= 340) online.put("1-12-x", online.get("1-12-x")+1);
			else if (version <= 404) online.put("1-13-x", online.get("1-13-x")+1);
			else if (version <= 498) online.put("1-14-x", online.get("1-14-x")+1);
			else online.put("1-14-x", online.get("1-14-x")+1); //current newest one
		}
	}
	
	public static String getTime() {
		return new SimpleDateFormat(Configs.timeFormat).format(new Date(System.currentTimeMillis() + (int)Configs.timeOffset*3600000));
	}
	public static String getDate() {
		return new SimpleDateFormat(Configs.dateFormat).format(new Date());
	}
	public static String setAnimations(String s) {
		if (s.contains("animat")) {
			for (Animation a : Configs.animations) {
				s = s.replace("%animated-object:" + a.getName() + "%", a.getMessage());
				s = s.replace("%animation:" + a.getName() + "%", a.getMessage());
				s = s.replace("{animated-object:" + a.getName() + "}", a.getMessage());
				s = s.replace("{animation:" + a.getName() + "}", a.getMessage());
			}
		}
		return s;
	}
	public static String color(String s) {
		return s.replace("&0", "§0").replace("&1", "§1").replace("&2", "§2").replace("&3", "§3").replace("&4", "§4")
				.replace("&5", "§5").replace("&6", "§6").replace("&7", "§7").replace("&8", "§8").replace("&9", "§9")
				.replace("&a", "§a").replace("&b", "§b").replace("&c", "§c").replace("&d", "§d").replace("&e", "§e")
				.replace("&f", "§f").replace("&k", "§k").replace("&l", "§l").replace("&m", "§m").replace("&n", "§n")
				.replace("&o", "§o").replace("&r", "§r");
	}
	public static String[] replaceMultiple(ITabPlayer p, String... args) {
		String string = "";
        int i = 0;
        while (i < args.length) {
        	if (i>0) {
            	string += "@@@###@@@";
            }
            string += "|||@@@|||" + args[i];
            ++i;
        }
        string = replace(string, p);
        String[] arr = string.split("@@@###@@@");
        for (int j=0; j<arr.length; j++) {
        	arr[j] = arr[j].replace("|||@@@|||", "");
        }
        return arr;
	}
	public static String replace(String string, ITabPlayer p) {
		if (!string.contains("%") && !string.contains("{")) return color(string);
		string = setAnimations(string);
		if (string.contains("%rank%")) string = string.replace("%rank%", p.getRank());
		if (Shared.servertype == ServerType.BUKKIT) {
        	string = me.neznamy.tab.bukkit.Placeholders.replace(string, p);
		}
		if (string.contains("%memory-used%")) string = string.replace("%memory-used%", ((int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + ""));
		if (string.contains("%memory-max%")) string = string.replace("%memory-max%", ((int) (Runtime.getRuntime().maxMemory() / 1048576))+"");
		if (string.contains("%memory-used-gb%")) string = string.replace("%memory-used-gb%", (Shared.round((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /1024/1024/1024) + ""));
		if (string.contains("%memory-max-gb%")) string = string.replace("%memory-max-gb%", (Shared.round((float)Runtime.getRuntime().maxMemory() /1024/1024/1024))+"");
		if (string.contains("%nick%")) string = string.replace("%nick%", p.getName());
		if (string.contains("%time%")) string = string.replace("%time%", getTime());
		if (string.contains("%date%")) string = string.replace("%date%", getDate());
		if (string.contains("%IP%")) string = string.replace("%IP%", p.getIPAddress());
		if (string.contains("%ip%")) string = string.replace("%ip%", p.getIPAddress());
		if (string.contains("%maxplayers%")) string = string.replace("%maxplayers%", maxPlayers+"");
		if (string.contains("%online%")) string = string.replace("%online%", Shared.getPlayers().size()+"");
		if (string.contains("%ping%")) string = string.replace("%ping%", p.getPing()+"");
		if (string.contains("%"+Shared.mainClass.getSeparatorType()+"%")) string = string.replace("%"+Shared.mainClass.getSeparatorType()+"%", p.getWorldName());
		if (string.contains("%"+Shared.mainClass.getSeparatorType()+"online%")){
			int var = 0;
			for (ITabPlayer all : Shared.getPlayers()){
				if (p.getWorldName().equals(all.getWorldName())) var++;
			}
			string = string.replace("%"+Shared.mainClass.getSeparatorType()+"online%", var+"");
		}
		if (string.contains("%staffonline%")){
			int var = 0;
			for (ITabPlayer all : Shared.getPlayers()){
				if (all.isStaff()) var++;
			}
			string = string.replace("%staffonline%", var+"");
		}
		return color(string);
	}
}