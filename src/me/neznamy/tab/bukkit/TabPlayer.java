package me.neznamy.tab.bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.earth2me.essentials.api.Economy;
import com.github.cheesesoftware.PowerfulPermsAPI.Group;

import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LocalizedNode;
import me.neznamy.tab.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.NameTag16;
import me.neznamy.tab.shared.Shared;
import ru.tehkode.permissions.bukkit.PermissionsEx;

@SuppressWarnings("deprecation")
public class TabPlayer extends ITabPlayer{
	
	private String money = "-";
	private long lastRefreshMoney;
	private List<Object> queuedPackets = new ArrayList<Object>();

	public TabPlayer(Player p) {
		super(p);
	}
	public void onJoin() throws Exception {
		version = Placeholders.getVersion(this);
		disabledHeaderFooter = Configs.disabledHeaderFooter.contains(getWorldName());
		disabledTablistNames = Configs.disabledTablistNames.contains(getWorldName());
		disabledNametag = Configs.disabledNametag.contains(getWorldName());
		disabledTablistObjective = Configs.disabledTablistObjective.contains(getWorldName());
		disabledBossbar = Configs.disabledBossbar.contains(getWorldName());
		ipAddress = getPlayer().getAddress().getAddress().getHostAddress();
		if (Shared.mainClass.listNames()) updatePlayerListName(false);
		if (NameTagX.enable || NameTag16.enable) {
			nameTagVisible = !getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY);
		}
		if (NameTagX.enable) {
			if (getPlayer().getVehicle() != null) {
				Entity vehicle = getPlayer().getVehicle();
				List<Integer> list = new ArrayList<Integer>();
				for (Entity e : NameTagX.getPassengers(vehicle)) {
					list.add(e.getEntityId());
				}
				NameTagX.vehicles.put(vehicle.getEntityId(), list);
			}
			loadArmorStands();
		}
		PerWorldPlayerlist.trigger(getPlayer());
		for (Object packet : queuedPackets) sendPacket(packet);
		queuedPackets.clear();
	}
	public String getGroupFromPermPlugin() {
		try {
			if (Main.luckPerms) return LuckPerms.getApi().getUser(getPlayer().getUniqueId()).getPrimaryGroup();
			if (Main.pex) return PermissionsEx.getUser(getPlayer()).getGroupNames()[0];
			if (Main.groupManager != null) return Main.groupManager.getWorldsHolder().getWorldPermissions(getPlayer()).getGroup(getName());
			if (Main.powerfulPerms != null) return Main.powerfulPerms.getPermissionManager().getPermissionPlayer(getPlayer().getUniqueId()).getPrimaryGroup().getName();
			try {
				if (Placeholders.perm != null) return Placeholders.perm.getPrimaryGroup(getPlayer());
			} catch (UnsupportedOperationException e) {
				// "SuperPerms no group permissions."
			}
		} catch (Exception ex) {
			Shared.error("Failed to get permission group of " + getPlayer().getName() + " (permission plugin: " + Shared.mainClass.getPermissionPlugin() + ")", ex);
		}
		return null;
	}
	public String[] getGroupsFromPermPlugin() {
		try {
			if (Main.luckPerms) {
				List<String> groups = new ArrayList<String>();
				for (LocalizedNode node : LuckPerms.getApi().getUser(getPlayer().getUniqueId()).getAllNodes()) if (node.isGroupNode()) groups.add(node.getGroupName());
				return groups.toArray(new String[0]);
			}
			if (Main.pex) return PermissionsEx.getUser(getPlayer()).getGroupNames();
			if (Main.groupManager != null) return Main.groupManager.getWorldsHolder().getWorldPermissions(getPlayer()).getGroups(getName());
			if (Main.powerfulPerms != null) {
				List<String> groups = new ArrayList<String>();
				for (Group g : Main.powerfulPerms.getPermissionManager().getPermissionPlayer(getPlayer().getUniqueId()).getGroups()) groups.add(g.getName());
				return groups.toArray(new String[0]);
			}
			try {
				if (Placeholders.perm != null) return Placeholders.perm.getPlayerGroups(getPlayer());
			} catch (UnsupportedOperationException e) {
				// "SuperPerms no group permissions."
			}
		} catch (Exception ex) {
			Shared.error("Failed to get permission group of " + getPlayer().getName() + " (permission plugin: " + Shared.mainClass.getPermissionPlugin() + ")", ex);
		}
		return null;
	}
	public String getMoney() {
		if (System.currentTimeMillis() - lastRefreshMoney > 1000L) {
			lastRefreshMoney = System.currentTimeMillis();
			money = refreshMoney();
		}
		return money;
	}
	private String refreshMoney() {
		try {
			String money = null;
			if (Placeholders.essentials != null) money = Shared.round(Economy.getMoneyExact(getName()).doubleValue());
			if (Placeholders.economy != null) money = Shared.round(Placeholders.economy.getBalance(getPlayer()));
			if (money == null) money = "-";
			return money;
		} catch (Exception e) {
			Shared.error("Failed to get money of " + getName(), e);
			return "-";
		}
	}
	public Player getPlayer() {
		return (Player) player;
	}
	public void setTeamVisible(boolean visible) {
		if (nameTagVisible != visible) {
			nameTagVisible = visible;
			updateTeam();
		}
	}
	public String getNickname() {
		String name = null;
		if (Placeholders.essentials != null && Placeholders.essentials.getUser(getPlayer()) != null) {
			name = Placeholders.essentials.getUser(getPlayer()).getNickname();
		}
		if (name == null || name.length() == 0) name = getName();
		return name;
	}
	public void restartArmorStands() {
		NameTagLineManager.destroy(this);
		armorStands.clear();
		loadArmorStands();
		for (Player all : getPlayer().getWorld().getPlayers()) {
			if (all.getName().equals(getName())) continue;
			NameTagLineManager.spawnArmorStand(this, Shared.getPlayer(all.getUniqueId()), true);
		}
	}
	public void loadArmorStands() {
		float height = -0.22F;
		for (Object line : Premium.dynamicLines) {
			String value = getActiveProperty(line);
			if (value == null || value.length() == 0) continue;
			NameTagLineManager.bindLine(this, value, height+=0.22F, line+"");
		}
		for (Entry<String, Double> line : Premium.staticLines.entrySet()) {
			String value = getActiveProperty(line.getKey());
			if (value == null || value.length() == 0) continue;
			NameTagLineManager.bindLine(this, value, Double.parseDouble(line.getValue()+""), line.getKey());
		}
	}
	public String getName() {
		return getPlayer().getName();
	}
	public String getWorldName() {
		return getPlayer().getWorld().getName();
	}
	public UUID getUniqueId() {
		return getPlayer().getUniqueId();
	}
	public boolean hasPermission(String permission) {
		return getPlayer().hasPermission(permission);
	}
	public Integer getEntityId() {
		return getPlayer().getEntityId();
	}
	public long getPing() {
		int ping = MethodAPI.getInstance().getPing(getPlayer());
		if (ping > 10000 || ping < 0) ping = -1;
		return ping;
	}
	public int getHealth() {
		return (int) getPlayer().getHealth();
	}
	public void sendPacket(Object nmsPacket) {
		try {
			MethodAPI.getInstance().sendPacket(getPlayer(), nmsPacket);
//			System.out.println(getName() + " - " + nmsPacket.getClass().getSimpleName());
		} catch (NullPointerException e) {
			queuedPackets.add(nmsPacket);
		}
	}
	public void setPlayerListName() {
		getPlayer().setPlayerListName(getPlayer().getPlayerListName());
	}
	public void sendMessage(String message) {
		if (message == null || message.length() == 0) return;
		getPlayer().sendMessage(message);
	}
	protected void loadChannel() {
		try {
			channel = MethodAPI.getInstance().getChannel((Player) player);
		} catch (Exception e) {
			Shared.error("Failed to get channel of " + getName(), e);
		}
	}
}