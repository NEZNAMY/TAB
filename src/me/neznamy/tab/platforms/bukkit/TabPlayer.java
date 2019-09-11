package me.neznamy.tab.platforms.bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.disguise.DisguiseAPI;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LocalizedNode;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.platforms.bukkit.unlimitedtags.NameTagLineManager;
import me.neznamy.tab.platforms.bukkit.unlimitedtags.NameTagX;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.NameTag16;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import protocolsupport.api.ProtocolSupportAPI;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import us.myles.ViaVersion.api.Via;

@SuppressWarnings("deprecation")
public class TabPlayer extends ITabPlayer{
	
	public Player player;
	private String money = "-";
	private long lastRefreshMoney;

	public TabPlayer(Player p) throws Exception {
		player = p;
		world = p.getWorld().getName();
		channel = MethodAPI.getInstance().getChannel((Player) player);
		init(p.getName(), p.getUniqueId());
		try {
			int version;
			if (Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport")){
				version = ProtocolSupportAPI.getProtocolVersion(player).getId();
				if (version > 0) this.version = ProtocolVersion.fromNumber(version);
			} else if (Bukkit.getPluginManager().isPluginEnabled("ViaVersion")){
				version = Via.getAPI().getPlayerVersion(getUniqueId());
				if (version > 0) this.version = ProtocolVersion.fromNumber(version);
			}
		} catch (Throwable e) {
			Shared.error("An error occured when getting version of " + getName(), e);
		}
		if (NameTagX.enable || NameTag16.enable) {
			nameTagVisible = !player.hasPotionEffect(PotionEffectType.INVISIBILITY);
		}
		if (NameTagX.enable) {
			if (player.getVehicle() != null) {
				Entity vehicle = player.getVehicle();
				List<Integer> list = new ArrayList<Integer>();
				for (Entity e : NameTagX.getPassengers(vehicle)) {
					list.add(e.getEntityId());
				}
				NameTagX.vehicles.put(vehicle.getEntityId(), list);
			}
			loadArmorStands();
		}
		PerWorldPlayerlist.trigger(player);
	}
	public String getGroupFromPermPlugin() {
		try {
			if (Main.luckPerms) return LuckPerms.getApi().getUser(player.getUniqueId()).getPrimaryGroup();
			if (Main.pex) return PermissionsEx.getUser(player).getGroupNames()[0];
			if (Main.groupManager != null) return Main.groupManager.getWorldsHolder().getWorldPermissions(player).getGroup(getName());
			if (Main.perm != null && !Main.perm.getName().equals("SuperPerms")) return Main.perm.getPrimaryGroup(player);
		} catch (Throwable ex) {
			Shared.error("Failed to get permission group of " + player.getName() + " (permission plugin: " + Shared.mainClass.getPermissionPlugin() + ")", ex);
		}
		return null;
	}
	public String[] getGroupsFromPermPlugin() {
		try {
			if (Main.luckPerms) {
				List<String> groups = new ArrayList<String>();
				for (LocalizedNode node : LuckPerms.getApi().getUser(player.getUniqueId()).getAllNodes()) if (node.isGroupNode()) groups.add(node.getGroupName());
				return groups.toArray(new String[0]);
			}
			if (Main.pex) return PermissionsEx.getUser(player).getGroupNames();
			if (Main.groupManager != null) return Main.groupManager.getWorldsHolder().getWorldPermissions(player).getGroups(getName());
			if (Main.perm != null && !Main.perm.getName().equals("SuperPerms")) return Main.perm.getPlayerGroups(player);
		} catch (Throwable ex) {
			Shared.error("Failed to get permission group of " + player.getName() + " (permission plugin: " + Shared.mainClass.getPermissionPlugin() + ")", ex);
		}
		return null;
	}
	public String getMoney() {
		if (System.currentTimeMillis() - lastRefreshMoney > 1000L) {
			lastRefreshMoney = System.currentTimeMillis();
			if (Main.essentials != null) money = Shared.round(Main.essentials.getUser(player).getMoney().doubleValue());
			if (Main.economy != null) money = Shared.round(Main.economy.getBalance(player));
		}
		return money;
	}
	public void setTeamVisible(boolean visible) {
		if (nameTagVisible != visible) {
			nameTagVisible = visible;
			updateTeam();
		}
	}
	public String getNickname() {
		String name = null;
		if (Main.essentials != null && Main.essentials.getUser(player) != null) {
			name = Main.essentials.getUser(player).getNickname();
		}
		if (name == null || name.length() == 0) name = getName();
		return name;
	}
	public void restartArmorStands() {
		NameTagLineManager.destroy(this);
		armorStands.clear();
		loadArmorStands();
		for (Player w : player.getWorld().getPlayers()) {
			ITabPlayer wPlayer = Shared.getPlayer(w.getUniqueId());
			if (wPlayer == null) {
				Shared.error("Data of " + w.getName() + " don't exist ?");
				continue;
			}
			if (w == wPlayer) continue;
			if (w.getName().equals(getName())) continue;
			NameTagLineManager.spawnArmorStand(this, wPlayer, true);
		}
	}
	public void loadArmorStands() {
		float height = -0.22F;
		for (String line : Premium.dynamicLines) {
			Property p = properties.get(line);
			if (p == null || p.get().length() == 0) continue;
			String value = p.getCurrentRawValue();
			NameTagLineManager.bindLine(this, value, height+=0.22F, line+"");
		}
		for (Entry<String, Double> line : Premium.staticLines.entrySet()) {
			Property p = properties.get(line.getKey());
			if (p == null || p.get().length() == 0) continue;
			String value = p.getCurrentRawValue();
			NameTagLineManager.bindLine(this, value, Double.parseDouble(line.getValue()+""), line.getKey());
		}
	}
	public boolean hasPermission(String permission) {
		return player.hasPermission(permission);
	}
	public Integer getEntityId() {
		return player.getEntityId();
	}
	public long getPing() {
		int ping = MethodAPI.getInstance().getPing(player);
		if (ping > 10000 || ping < 0) ping = -1;
		return ping;
	}
	public int getHealth() {
		return (int) Math.ceil(player.getHealth());
	}
	public void sendPacket(Object nmsPacket) {
		if (nmsPacket != null) MethodAPI.getInstance().sendPacket(player, nmsPacket);
	}
	public void setPlayerListName() {
		player.setPlayerListName(player.getPlayerListName());
	}
	public void sendMessage(String message) {
		if (message == null || message.length() == 0) return;
		player.sendMessage(message);
	}
	public boolean hasInvisibility() {
		return player.hasPotionEffect(PotionEffectType.INVISIBILITY);
	}
	public boolean getTeamPush() {
		if (Main.libsdisguises && DisguiseAPI.isDisguised(player)) return false;
		if (Main.idisguise != null && Main.idisguise.isDisguised(player)) return false; 
		return Configs.collision;
	}
}