package me.neznamy.tab.platforms.bukkit.placeholders.afk;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.bukkit.Bukkit;

import me.neznamy.tab.shared.ITabPlayer;

/**
 * Can't find plugin link. Was it removed?
 */
public class AutoAFK implements AFKProvider {

	@Override
	public boolean isAFK(ITabPlayer p) throws Exception {
		Object plugin = Bukkit.getPluginManager().getPlugin("AutoAFK");
		Field f = plugin.getClass().getDeclaredField("afkList");
		f.setAccessible(true);
		HashMap<?, ?> map = (HashMap<?, ?>) f.get(plugin);
		return map.containsKey(p.getBukkitEntity());
	}
}