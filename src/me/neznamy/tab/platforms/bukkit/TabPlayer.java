package me.neznamy.tab.platforms.bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.platforms.bukkit.unlimitedtags.NameTagLineManager;
import me.neznamy.tab.platforms.bukkit.unlimitedtags.NameTagX;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.NameTag16;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;

public class TabPlayer extends ITabPlayer{

	public Player player;
	private String money = "-";
	private long lastRefreshMoney;

	public TabPlayer(Player p) throws Exception {
		player = p;
		world = p.getWorld().getName();
		channel = MethodAPI.getInstance().getChannel(player);
		tablistId = p.getUniqueId();
		init(p.getName(), p.getUniqueId());
		int version;
		if (Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport")){
			version = PluginHooks.ProtocolSupportAPI_getProtocolVersionId(this);
			if (version > 0) this.version = ProtocolVersion.fromNumber(version);
		} else if (Bukkit.getPluginManager().isPluginEnabled("ViaVersion")){
			version = PluginHooks.ViaVersion_getPlayerVersion(this);
			if (version > 0) this.version = ProtocolVersion.fromNumber(version);
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
			if (PluginHooks.luckPerms) return PluginHooks.LuckPerms_getPrimaryGroup(this);
			if (PluginHooks.permissionsEx) return PluginHooks.PermissionsEx_getGroupNames(this)[0];
			if (PluginHooks.groupManager != null) return PluginHooks.GroupManager_getGroup(this);
			if (PluginHooks.Vault_permission != null && !PluginHooks.Vault_permission.getName().equals("SuperPerms")) return PluginHooks.Vault_permission.getPrimaryGroup(player);
		} catch (Throwable ex) {
			Shared.error(null, "Failed to get permission group of " + player.getName() + " (permission plugin: " + Shared.mainClass.getPermissionPlugin() + ")", ex);
		}
		return null;
	}
	public String[] getGroupsFromPermPlugin() {
		try {
			if (PluginHooks.luckPerms) return PluginHooks.LuckPerms_getAllGroups(this);
			if (PluginHooks.permissionsEx) return PluginHooks.PermissionsEx_getGroupNames(this);
			if (PluginHooks.groupManager != null) return PluginHooks.GroupManager_getGroups(this);
			if (PluginHooks.Vault_permission != null && !PluginHooks.Vault_permission.getName().equals("SuperPerms")) return PluginHooks.Vault_permission.getPlayerGroups(player);
		} catch (Throwable ex) {
			Shared.error(null, "Failed to get permission group of " + player.getName() + " (permission plugin: " + Shared.mainClass.getPermissionPlugin() + ")", ex);
		}
		return null;
	}
	public String getMoney() {
		if (System.currentTimeMillis() - lastRefreshMoney > 1000L) {
			lastRefreshMoney = System.currentTimeMillis();
			if (PluginHooks.essentials != null) money = Shared.round(PluginHooks.essentials.getUser(player).getMoney().doubleValue());
			if (PluginHooks.Vault_economy != null) money = Shared.round(PluginHooks.Vault_economy.getBalance(player));
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
		if (PluginHooks.essentials != null && PluginHooks.essentials.getUser(player) != null) {
			name = PluginHooks.essentials.getUser(player).getNickname();
		}
		if (name == null || name.length() == 0) name = getName();
		return name;
	}
	public void restartArmorStands() {
		NameTagLineManager.destroy(this);
		if (previewingNametag) NameTagLineManager.destroy(this, this);
		armorStands.clear();
		loadArmorStands();
		for (Player w : player.getWorld().getPlayers()) {
			ITabPlayer wPlayer = Shared.getPlayer(w.getUniqueId());
			if (wPlayer == null) {
				Shared.error(null, "Data of " + w.getName() + " don't exist ?");
				continue;
			}
			if (w == player) continue;
			NameTagLineManager.spawnArmorStand(this, wPlayer, true);
		}
		if (previewingNametag) NameTagLineManager.spawnArmorStand(this, this, false);
	}
	public void loadArmorStands() {
		double height = -Configs.NTX_SPACE;
		for (String line : Premium.dynamicLines) {
			Property p = properties.get(line);
			if (p == null || p.get().length() == 0) continue;
			String value = p.getCurrentRawValue();
			NameTagLineManager.bindLine(this, value, height+=Configs.NTX_SPACE, line);
		}
		for (Entry<String, Double> line : Premium.staticLines.entrySet()) {
			Property p = properties.get(line.getKey());
			if (p == null || p.get().length() == 0) continue;
			String value = p.getCurrentRawValue();
			NameTagLineManager.bindLine(this, value, line.getValue(), line.getKey());
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
		if (PluginHooks.libsDisguises && PluginHooks.LibsDisguises_isDisguised(this)) return false;
		if (PluginHooks.idisguise != null && PluginHooks.iDisguise_isDisguised(this)) return false; 
		return Configs.collision;
	}
}